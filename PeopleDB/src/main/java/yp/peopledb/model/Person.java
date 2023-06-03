package yp.peopledb.model;

import yp.peopledb.annotation.Id;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public class Person {
    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private ZonedDateTime dob;
    private BigDecimal salary = new BigDecimal("0");
    private String email;
    private Optional<Address> homeAddress = Optional.empty();
    private Optional<Address> businessAddress = Optional.empty();;

    public Person(String firstName, String lastName, ZonedDateTime dob) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
    }

    public Person(Long id, String firstName, String lastName, ZonedDateTime dob) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
    }

    public Person(Long id, String firstName, String lastName, ZonedDateTime dob, BigDecimal salary) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.salary = salary;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ZonedDateTime getDob() {
        return dob;
    }

    public void setDob(ZonedDateTime dob) {
        this.dob = dob;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dob=" + dob +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        System.out.println("object: " + getDob().withZoneSameInstant(ZoneId.of("UTC")));
        System.out.println("person: " + person.getDob().withZoneSameInstant(ZoneId.of("UTC")));

        return Objects.equals(
                getId(), person.getId()) &&
                getFirstName().equals(person.getFirstName()) &&
                getLastName().equals(person.getLastName()) &&
                getDob().withZoneSameInstant(ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS)
                        .equals(person.getDob().withZoneSameInstant(ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstName(), getLastName(),
                getDob().withZoneSameInstant(ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS));
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = Optional.ofNullable(homeAddress);
    }

    public Optional<Address> getHomeAddress() {
        return homeAddress;
    }

    public void setBusinessAddress(Address businessAddress) {
        this.businessAddress = Optional.ofNullable(businessAddress);
    }
    

    public Optional<Address> getBusinessAddress() {
        return businessAddress;
    }
}
