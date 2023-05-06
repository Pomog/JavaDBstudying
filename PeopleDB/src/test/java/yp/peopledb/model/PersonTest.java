package yp.peopledb.model;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;


class PersonTest {

    @Test
    public void testForEquality(){
        Person p1 = new Person("John", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        Person p2 = new Person("John", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void testForInequality(){
        Person p1 = new Person("John", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        Person p2 = new Person("John2", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        assertThat(p1).isNotEqualTo(p2);
    }

}