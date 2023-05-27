package yp.peopledb.repository;

import yp.peopledb.annotation.Id;
import yp.peopledb.annotation.SQL;
import yp.peopledb.model.Address;
import yp.peopledb.model.CrudOperation;
import yp.peopledb.model.Region;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressRepository extends CRUDRepository<Address>{
    public AddressRepository(Connection connection) {
        super(connection);
    }

    @Override
    @SQL(operationType = CrudOperation.SAVE, value = """
            INSERT INTO ADRESSES 
            (STREET_ADRESS, ADRESS_2,CITY, STATE, POSTECODE, COUNTY, REGION, COUNTRY)
            VALUES(?, ?, ?, ?, ?, ?, ?, ?)
            """)
    void mapForSave(Address entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.streetAddress());
        ps.setString(2, entity.address2());
        ps.setString(3, entity.city());
        ps.setString(4, entity.state());
        ps.setString(5, entity.zip());
        ps.setString(6, entity.county());
        ps.setString(7, entity.region().toString());
        ps.setString(8, entity.streetAddress());
    }

    @Override
    void mapForUpdate(Address entity, PreparedStatement ps) throws SQLException {

    }

    @Override
    @SQL(operationType = CrudOperation.FIND_BY_ID, value = """
            SELECT
            ID, STREET_ADRESS, ADRESS_2,CITY, STATE, POSTECODE, COUNTY, REGION, COUNTRY
            FROM ADRESSES WHERE ID = ?
            """)
    Address extractEntityFromResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("ID");
        String streetAddress = rs.getString("STREET_ADRESS");
        String address2 = rs.getString("ADRESS_2");
        String city = rs.getString("CITY");
        String state = rs.getString("STATE");
        String postcode = rs.getString("POSTECODE");
        String county = rs.getString("COUNTY");
        String regionString = rs.getString("REGION");
        Region region = Region.valueOf(regionString.toUpperCase());
        String country = rs.getString("COUNTRY");

        return new Address(id, streetAddress, address2, city, state ,postcode, country, country, region);
      }
}
