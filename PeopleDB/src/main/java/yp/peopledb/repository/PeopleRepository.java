package yp.peopledb.repository;

import yp.peopledb.annotation.SQL;
import yp.peopledb.model.Address;
import yp.peopledb.model.CrudOperation;
import yp.peopledb.model.Person;
import yp.peopledb.model.Region;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class PeopleRepository extends CRUDRepository <Person>{
    private AddressRepository addressRepository = null;

    public static final String SAVE_PERSON_SQL = """
            INSERT INTO PEOPLE
            (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    public static final String FIND_All_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB, SALARY FROM PEOPLE";
    public static final String FIND_BY_ID_SQL = """
            SELECT PERSON.ID, PERSON.FIRST_NAME, PERSON.LAST_NAME, PERSON.DOB, PERSON.SALARY, PERSON.HOME_ADDRESS,
                   ADDRESS.ID, ADDRESS.STREET_ADRESS, ADDRESS.ADRESS_2,CITY, ADDRESS.STATE, ADDRESS.POSTECODE, ADDRESS.COUNTY, ADDRESS.REGION, ADDRESS.COUNTRY
            FROM PEOPLE AS PERSON
            LEFT OUTER JOIN ADRESSES AS ADDRESS
            ON PERSON.HOME_ADDRESS = ADDRESS.ID
            WHERE PERSON.ID=?""";
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

        if (entity.getHomeAddress().isPresent()) {
            savedAddress = addressRepository.save(entity.getHomeAddress().get());
            ps.setLong(6, savedAddress.id());
        } else {
            ps.setObject(6, null);
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
        long personID = rs.getLong("ID");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = rs.getBigDecimal("SALARY");
        long homeAddressID = rs.getLong("HOME_ADDRESS");

        Person person = new Person(personID, firstName, lastName, dob, salary);

        if (homeAddressID != 0) {
            Address address = extractedAddress(rs);
            person.setHomeAddress(address);
        }
        return person;
    }

    private static Address extractedAddress(ResultSet rs) throws SQLException {
        long addressID = rs.getLong("ID");
        String streetAddress = rs.getString("STREET_ADRESS");
        String address2 = rs.getString("ADRESS_2");
        String city = rs.getString("CITY");
        String state = rs.getString("STATE");
        String postcode = rs.getString("POSTECODE");
        String county = rs.getString("COUNTY");
        String regionString = rs.getString("REGION");
        Region region = Region.valueOf(regionString.toUpperCase());
        String country = rs.getString("COUNTRY");
        Address address = new Address(addressID, streetAddress, address2, city, state, postcode, county, country, region);
        return address;
    }

    private Timestamp convertDobToTimeStamp(ZonedDateTime dob) {
        return Timestamp.valueOf(dob.withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
    }
}

