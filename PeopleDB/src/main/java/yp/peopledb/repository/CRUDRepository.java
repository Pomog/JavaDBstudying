package yp.peopledb.repository;

import yp.peopledb.annotation.Id;
import yp.peopledb.annotation.MultiSQL;
import yp.peopledb.annotation.SQL;
import yp.peopledb.model.CrudOperation;
import yp.peopledb.repository.exeption.DataException;
import yp.peopledb.repository.exeption.UnableToSaveException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;


abstract class CRUDRepository<T> {

    protected Connection connection;
    private PreparedStatement savePS;
    private PreparedStatement findByIdPS;

    public CRUDRepository(Connection connection) {
        try {
            this.connection = connection;
            savePS = connection.prepareStatement(getSqlByAnnotation(CrudOperation.SAVE, this::getSaveSql), Statement.RETURN_GENERATED_KEYS);
            findByIdPS = connection.prepareStatement(getSqlByAnnotation(CrudOperation.FIND_BY_ID, this::getFindByIdSql));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataException("Unable to create prepared statements for CRUDRepository", e);
        }
    }

    public T save(T entity) {
        try {
            mapForSave(entity, savePS);
            int recordsAffected = savePS.executeUpdate();
            System.out.println("recordsAffected = " + recordsAffected);
            ResultSet rs = savePS.getGeneratedKeys();
            System.out.println("savePS.getGeneratedKeys() = " + rs);
            while (rs.next()) {
                long id = rs.getLong(1);
                setIdByAnnotation(id, entity);
                postSave (entity, id);
                System.out.println("saved entity : \\n" +  entity);
            }
           // System.out.printf("save method, recordsAffected: %d%n", recordsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new UnableToSaveException("tried to save person: " + entity);
        }
        return entity;
    }

    public void update(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.UPDATE, this::getUpdateSql));
            mapForUpdate(entity, ps);
            ps.setLong(5, getIdByAnnotation(entity));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<T> findById(Long id) {
        T entity = null;
        try {
            findByIdPS.setLong(1, id);
            ResultSet rs = findByIdPS.executeQuery();
            while (rs.next()){
                entity = extractEntityFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(entity);
    }

    public List<T> findAll() {
        List<T> entities = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    getSqlByAnnotation(CrudOperation.FIND_ALL, this::getFindAllSql),
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                entities.add(extractEntityFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entities;
    }

    public long count() {
        long count = 0;
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.COUNT, this::getCountSql));
            ResultSet rs = ps.executeQuery();
            rs.next();
            count = rs.getLong(1);
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    public void delete(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.DELETE_ONE, this::getDeleteSql));
            ps.setLong(1, getIdByAnnotation(entity));
            int affectedRecordCount = ps.executeUpdate();
            System.out.println("affectedRecordCount on delete: " + affectedRecordCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    public final void delete(T... entities) {
        try {
            Statement stmt = connection.createStatement();
            String ids = Arrays.stream(entities).
                    map(this::getIdByAnnotation).
                    map(String::valueOf).
                    collect(joining(","));
            int affectedRecordsCount = stmt.executeUpdate(getSqlByAnnotation(CrudOperation.DELETE_MANY, this::getDeleteInSql)
                    .replace(":ids", ids));
            System.out.println("affectedRecordsCount on multi delete: " + affectedRecordsCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Long getIdByAnnotation(T entity){
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(field -> {
                    field.setAccessible(true);
                    Long id = null;
                    try {
                        id = (long)field.get(entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    return id;
                })
                .findFirst().orElseThrow(() -> new RuntimeException("No ID annotated field found"));
    }

    private void setIdByAnnotation (Long id,T entity) {
         Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(entity, id);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Unable to set ID value");
                    }
                });
    }

    protected String getDeleteSql(){
        throw new RuntimeException("SQL not defined.");
    };

    protected String getCountSql(){
        throw new RuntimeException("SQL not defined.");
    };

    protected String getSaveSql(){
        throw new RuntimeException("SQL not defined.");
    };

    protected void postSave(T entity, long id) { }

    protected String getUpdateSql(){
        throw new RuntimeException("SQL not defined.");
    };

    protected String getFindAllSql(){
        throw new RuntimeException("SQL not defined.");
    };

    private String getSqlByAnnotation(CrudOperation operationType, Supplier<String> sqlGetter){
        Stream<SQL> multiSqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(MultiSQL.class))
                .map(method -> method.getAnnotation(MultiSQL.class))
                .flatMap(msql -> Arrays.stream(msql.value()));

        Stream<SQL> sqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(SQL.class))
                .map(method -> method.getAnnotation(SQL.class));

        return Stream.concat(multiSqlStream, sqlStream)
                .filter(annotation -> annotation.operationType().equals(operationType))
                .map(SQL::value)
                .findFirst().orElseGet(sqlGetter);
    }

    /**
     * @return Returns a String that represent the SQL needed to
     * retrieve one entity.
     * Thy SQL must contain one SQL parameter,
     * i.e. "?",
     * hat will bind to the entity's ID
     */
    protected String getFindByIdSql(){
        throw new RuntimeException("SQL not defined.");
    };

    /**
     *
     * @return Should return a SQL string like:
     * "DELETE FROM PEOPLE WHERE ID IN (:ids)"
     * be sure to include the '(:ids)' named parameter & call it 'ids'
     */
    protected String getDeleteInSql(){
        throw new RuntimeException("SQL not defined.");
    };

    abstract void mapForSave(T entity, PreparedStatement ps) throws SQLException;
    abstract void mapForUpdate(T entity, PreparedStatement ps) throws SQLException;
    abstract T extractEntityFromResultSet (ResultSet rs) throws SQLException;


}
