package yp.peopledb.repository;

import yp.peopledb.annotation.SQL;
import yp.peopledb.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class PeopleRepository extends CRUDRepository <Person>{
    private AddressRepository addressRepository = null;

    public static final String SAVE_PERSON_SQL = """
            INSERT INTO PEOPLE
            (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS, BUSINESS_ADDRESS, PARENT_ID)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    public static final String FIND_All_SQL = """
            SELECT
            PARENT.ID AS PARENT_ID,
            PARENT.FIRST_NAME AS PARENT_FIRST_NAME,
            PARENT.LAST_NAME AS PARENT_LAST_NAME,
            PARENT.DOB AS PARENT_DOB,
            PARENT.SALARY AS PARENT_SALARY,
            PARENT.EMAIL AS PARENT_EMAIL
            FROM PEOPLE AS PARENT
            """;
    public static final String FIND_BY_ID_SQL = """
            SELECT
            PARENT.ID AS PARENT_ID, PARENT.FIRST_NAME AS PARENT_FIRST_NAME, PARENT.LAST_NAME AS PARENT_LAST_NAME, PARENT.DOB AS PARENT_DOB, PARENT.SALARY AS PARENT_SALARY, PARENT.EMAIL AS PARENT_EMAIL,
            CHILD.ID AS CHILD_ID, CHILD.FIRST_NAME AS CHILD_FIRST_NAME, CHILD.LAST_NAME AS CHILD_LAST_NAME, CHILD.DOB AS CHILD_DOB, CHILD.SALARY AS CHILD_SALARY, CHILD.EMAIL AS CHILD_EMAIL,
                HOME.ID AS HOME_ID,
                HOME.STREET_ADRESS AS HOME_STREET_ADDRESS,
                HOME.CITY AS HOME_CITY,
                HOME.STATE AS HOME_STATE,
                HOME.POSTECODE AS HOME_POSTCODE,
                HOME.COUNTY AS HOME_COUNTY,
                HOME.REGION AS HOME_REGION,
                HOME.COUNTRY AS HOME_COUNTRY,
                BUSINESS.ID AS BUSINESS_ID,
                BUSINESS.STREET_ADRESS AS BUSINESS_STREET_ADDRESS,
                BUSINESS.ADRESS_2 AS BUSINESS_ADDRESS_2,
                BUSINESS.CITY AS BUSINESS_CITY,
                BUSINESS.STATE AS BUSINESS_STATE,
                BUSINESS.POSTECODE AS BUSINESS_POSTCODE,
                BUSINESS.COUNTY AS BUSINESS_COUNTY,
                BUSINESS.REGION AS BUSINESS_REGION,
                BUSINESS.COUNTRY AS BUSINESS_COUNTRY
            FROM PEOPLE AS PARENT
            LEFT OUTER JOIN PEOPLE AS CHILD ON PARENT.ID = CHILD.PARENT_ID
            LEFT OUTER JOIN ADRESSES AS HOME ON PARENT.HOME_ADDRESS = HOME.ID
            LEFT OUTER JOIN ADRESSES AS BUSINESS ON PARENT.BUSINESS_ADDRESS = BUSINESS.ID
            """;
    public static final String COUNT_SQL = "SELECT COUNT(*) FROM PEOPLE";
    public static final String DELETE_SQL = "DELETE FROM PEOPLE WHERE ID=?";
    public static final String DELETE_MULTIPLE_SQL = "DELETE FROM PEOPLE WHERE ID IN (:ids)";
    public static final String UPDATE_SQL = "UPDATE PEOPLE SET FIRST_NAME=?, LAST_NAME=?, DOB=?, SALARY=? WHERE ID=?";

    public PeopleRepository(Connection connection) {

        super(connection);
        addressRepository = new AddressRepository(connection);
    }

    @Override
    @SQL(value = SAVE_PERSON_SQL, operationType = CrudOperation.SAVE)
    void mapForSave(Person entity, PreparedStatement ps) throws SQLException {
        Address savedAddress = null;

        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, convertDobToTimeStamp(entity.getDob()));
        ps.setBigDecimal(4, entity.getSalary());
        ps.setString(5, entity.getEmail());

        associateAddressWithPerson(ps, entity.getHomeAddress(), 6);
        associateAddressWithPerson(ps, entity.getBusinessAddress(), 7);

        associateChildWithPerson(entity, ps);

    }

    private static void associateChildWithPerson(Person entity, PreparedStatement ps) throws SQLException {
        Optional<Person> parent = entity.getParent();
        if (parent.isPresent()){
            ps.setLong(8, parent.get().getId());
        } else {
            ps.setObject(8, null);
        }
    }

    @Override
    protected void postSave (Person entity, long id){
        entity.getChildren().stream()
                .forEach(this::save);
    }

    private void associateAddressWithPerson(PreparedStatement ps, Optional<Address> address, int parameterIndex) throws SQLException {
        Address savedAddress;
        if (address.isPresent()) {
            savedAddress = addressRepository.save(address.get());
            ps.setLong(parameterIndex, savedAddress.id());
        } else {
            ps.setObject(parameterIndex, null);
        }
    }

    @Override
    @SQL(value = UPDATE_SQL, operationType = CrudOperation.UPDATE)
    void mapForUpdate(Person entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, convertDobToTimeStamp(entity.getDob()));
        ps.setBigDecimal(4, entity.getSalary());
    }

    @Override
    @SQL(value = FIND_BY_ID_SQL, operationType = CrudOperation.FIND_BY_ID)
    @SQL(value = FIND_All_SQL, operationType = CrudOperation.FIND_ALL)
    @SQL(value = COUNT_SQL, operationType = CrudOperation.COUNT)
    @SQL(value = DELETE_SQL,operationType = CrudOperation.DELETE_ONE)
    @SQL(value = DELETE_MULTIPLE_SQL, operationType = CrudOperation.DELETE_MANY)
    Person extractEntityFromResultSet(ResultSet rs) throws SQLException{

//        printColumnNames(rs);

        Person finalParent = null;
        do {
            Person currentParent = extractPerson(rs, "PARENT_").get();
            if (finalParent == null){
                finalParent = currentParent;
            } if (!finalParent.equals(currentParent)) {
                rs.previous();
                break;
            }
            Optional<Person> child = extractPerson(rs, "CHILD_");
            child.ifPresent(finalParent::addChild);
        } while (rs.next());
//       System.out.println(finalParent);
        return finalParent;
    }

    private  Optional<Person> extractPerson(ResultSet rs, String aliasPrefix) throws SQLException {
//        System.out.println("working with aliasPrefix = " + aliasPrefix);

        Long personID = getValueByAlias(aliasPrefix + "ID", rs, Long.class);
        if (personID == null){ return  Optional.empty();}

        String firstName = getValueByAlias(aliasPrefix + "FIRST_NAME", rs, String.class);
        String lastName = getValueByAlias(aliasPrefix + "LAST_NAME", rs, String.class);



        ZonedDateTime dob = ZonedDateTime.of(getValueByAlias(aliasPrefix + "DOB", rs, Timestamp.class).toLocalDateTime(), ZoneId.of("+0"));


        BigDecimal salary = getValueByAlias(aliasPrefix + "SALARY", rs, BigDecimal.class);
        Person person = new Person(personID, firstName, lastName, dob, salary);
        Long homeAddressID = getValueByAlias("HOME_ID", rs, Long.class);
        Long businessAddressID = getValueByAlias("BUSINESS_ID", rs, Long.class);

//        System.out.println("TRY businessAddressID : " + businessAddressID);
        addAddressIfExist(rs, businessAddressID, person, AddressType.BUSINESS);

//        System.out.println("TRY homeAddressID : " + homeAddressID);
        addAddressIfExist(rs, homeAddressID, person, AddressType.HOME);

        return Optional.of(person);
    }

    private void addAddressIfExist(ResultSet rs, Long addressID, Person person, AddressType addressType) throws SQLException {
//        System.out.println("call addAddressIfExist with flag : " + addressType);
//        System.out.println("addressID for method: " + addressID);
        if (addressID != null) {
            System.out.println("EXTRACTION");

            switch (addressType) {
                case HOME -> {
                    Address address = extractedAddress(rs, "HOME_");
                    person.setHomeAddress(address);
                    System.out.println("EXTRACTED address : " + address);
                    System.out.println("person.setHomeAddress : " + address);}
                case BUSINESS -> {
                    Address address = extractedAddress(rs, "BUSINESS_");
                    person.setBusinessAddress(address);
                    System.out.println("EXTRACTED address : " + address);
                    System.out.println("person.setBusinessAddress : " + address);}
                        }
        }
    }
    private <T> T getValueByAlias (String alias, ResultSet rs, Class<T> clazz) throws SQLException{
        int columnCount = rs.getMetaData().getColumnCount();
        for (int colIdx = 1; colIdx <= columnCount; colIdx++){
            String columnLabel = rs.getMetaData().getColumnLabel(colIdx);
            if (alias.equals(columnLabel)){
                return rs.getObject(colIdx,clazz);
            }
        }
        return null;
    }
    private Address extractedAddress(ResultSet rs, String aliasPrefix) throws SQLException {
        long addressID = rs.getLong("ID");
        if (addressID == 0) return null;
        String streetAddress = getValueByAlias(aliasPrefix + "STREET_ADRESS", rs, String.class);
        getValueByAlias(aliasPrefix + "COUNTRY", rs, String.class);
        String address2 = getValueByAlias(aliasPrefix + "ADRESS_2", rs, String.class);
        String city = getValueByAlias(aliasPrefix + "CITY", rs, String.class);
        String state = getValueByAlias(aliasPrefix + "STATE", rs, String.class);
        String postcode = getValueByAlias(aliasPrefix + "POSTECODE", rs, String.class);
        String county = getValueByAlias(aliasPrefix + "COUNTY", rs, String.class);
        String regionString = getValueByAlias(aliasPrefix + "REGION", rs, String.class);
        Region region = Region.valueOf(regionString.toUpperCase());
        String country = getValueByAlias(aliasPrefix + "COUNTRY", rs, String.class);

        var address = new Address(addressID, streetAddress, address2, city, state, postcode, county, country, region);
//        System.out.println("extractedAddress returned : " + address);
        return address;
    }

    private Timestamp convertDobToTimeStamp(ZonedDateTime dob) {
        return Timestamp.valueOf(dob.withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
    }

    private static void printColumnNames (ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            System.out.println("Column Name: " + columnName);
        }
    }
}

