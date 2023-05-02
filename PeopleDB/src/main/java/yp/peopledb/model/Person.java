package yp.peopledb.model;

import java.time.ZonedDateTime;

public class Person {
    private Long id;

    public Person(String firstName, String lastName, ZonedDateTime dob) {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return 1L;
    }
}
