package yp.peopledb.repository;

import yp.peopledb.model.EntityDB;
import yp.peopledb.model.Person;
import yp.peopledb.repository.exeption.UnableToSaveException;

import java.sql.*;



abstract class CRUDRepository<T extends EntityDB> {

    protected Connection connection;

    public CRUDRepository(Connection connection) {
        this.connection = connection;
    }

    public T save(T entity) throws UnableToSaveException {

        try {
            PreparedStatement ps = connection.prepareStatement(getSaveSql(), Statement.RETURN_GENERATED_KEYS);
            mapForSave(entity, ps);
            int recordsAffected = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            System.out.println("ResultSet : " + rs);
            while (rs.next()) {
                long id = rs.getLong(1);
                entity.setId(id);
                System.out.println(entity);
            }
            System.out.printf("recordsAffected: %d%n", recordsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new UnableToSaveException("tried to save person: " + entity);
        }
        return entity;
    }

    abstract void mapForSave(T entity, PreparedStatement ps) throws SQLException;

    abstract String getSaveSql();
}
