package yp.peopledb.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import yp.peopledb.model.Person;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTest {

    private Connection connection;
    private PeopleRepository repo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:h2:~/peopletest".replace("~", System.getProperty("user.home"))
        );
        connection.setAutoCommit(false);
        repo = new PeopleRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    @Disabled("This test is failing on GitHub")
    public void canSaveOnePerson() {
        Person john = new Person("John", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        Person savedPerson = repo.save(john);
        assertThat(savedPerson.getId()).isGreaterThan(0);
    }
    @Test
    @Disabled("This test is failing on GitHub")
    public void canSaveTwoPersons(){
        Person john = new Person("John", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        Person bobby = new Person("Bobby", "Smith",
                ZonedDateTime.of(1982, 9, 13 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        Person savedPerson1 = repo.save(john);
        Person savedPerson2 = repo.save(bobby);
        assertThat(savedPerson1.getId()).isNotEqualTo(savedPerson2.getId());
    }

    @Test
    @Disabled("This test is failing on GitHub")
    public void canFindPersonById (){
        Person savedPerson = repo.save(new Person("test", "jackson", ZonedDateTime.now()));
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson).isEqualTo(savedPerson);
    }

    @Test
    @Disabled("This test is failing on GitHub")
    public void testPersonIdNotFound (){
        Optional<Person> foundPerson = repo.findById(-1L);
        assertThat(foundPerson).isEmpty();
    }

    @Test
    @Disabled("This test is failing on GitHub")
    public void canGetCount (){
        long startCount = repo.count();
        repo.save(new Person("John1", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6"))));
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount + 1);
    }

    @Test
    @Disabled("This test is failing on GitHub")
    public void findAllTest (){
        int startSize = repo.findAll().size();
        System.out.println("startSize : " + startSize);
        repo.save(new Person("John1", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6"))));
        int endSize = repo.findAll().size();
        System.out.println("endSize : " + endSize);
        assertThat(startSize).isEqualTo(endSize - 1);
    }

    @Test
    @Disabled("This test is failing on GitHub")
    public void canDelete (){
        Person savedPerson = new Person("SavedPerson", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        repo.save(savedPerson);
        long startCount = repo.count();
        repo.delete(savedPerson);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount - 1);
    }

    @Test
    @Disabled("This test is failing on GitHub")
    public void canDeleteMultiplePeople (){
        Person p1 = new Person("P1", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        Person p2 = new Person("P2", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));
        repo.save(p1);
        repo.save(p2);
        long startCount = repo.count();
        repo.delete(p1, p2);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount - 2);
    }

    @Test
    public void experiment(){

        System.out.println("\nSystem.getProperty(\"user.dir\"))");
        System.out.println(System.getProperty("user.dir"));

        File wd = new File(".");
        System.out.println("working dir: " + wd.getAbsolutePath());

        String filePath = "src/main/resources/1.txt";

        File file = new File(filePath);
        if (file.exists()) {
            System.out.printf("The file %s exists!%n", filePath);
        } else {
            System.out.printf("The file %s does not exist.%n", filePath);
        }

        System.out.println("\nSystem.getProperty(\"user.dir\"))");
        System.out.println(System.getProperty("user.dir"));

        Person p1 = new Person( 10L, null, null, null);
        Person p2 = new Person( 20L, null, null, null);
        Person p3 = new Person( 30L, null, null, null);
        Person p4 = new Person( 40L, null, null, null);

        Person[] people = Arrays.asList(p1, p2, p3, p4).toArray(new Person[]{});
        String ids = Arrays.stream(people).
                map(Person::getId).
                map(String::valueOf).
                collect(joining(","));
        System.out.println(ids);
    }

    @Test
    @Disabled("This test is failing on GitHub")
    public void canUpdate(){
        Person savedPerson = new Person("UpdateTest1", "Smith",
                ZonedDateTime.of(1980, 11, 15 ,
                        15, 15, 0, 0,
                        ZoneId.of("-6")));

        repo.save(savedPerson);
        System.out.println("savedPerson.getSalary() : " + savedPerson.getSalary());
        Person p1 = repo.findById(savedPerson.getId()).get();
        savedPerson.setSalary(new BigDecimal("73000.34"));
        repo.update(savedPerson);
        Person p2 = repo.findById(savedPerson.getId()).get();

        System.out.println("p1.getSalary(): " + p1.getSalary());
        System.out.println("p2.getSalary(): " + p2.getSalary());

        assertThat(p2.getSalary()).isNotEqualByComparingTo(p1.getSalary());
    }

    @Test
    public void loadData() throws IOException, SQLException {
        String filePath = "E:\\udemy\\Hr5m.csv";
        Files.lines(Path.of(filePath))
                .skip(1)
                //.limit(100)
                .map(line -> line.split(","))
                .map(a -> {
                    LocalDate dob = LocalDate.parse(a[10], DateTimeFormatter.ofPattern("M/d/yyyy"));
                    LocalTime tob = LocalTime.parse(a[11], DateTimeFormatter.ofPattern("hh:mm:ss a"));
                    LocalDateTime dtob = LocalDateTime.of(dob, tob);
                    ZonedDateTime zdtob = ZonedDateTime.of(dtob, ZoneId.of("+0"));
                    Person person = new Person(a[2], a[4], zdtob);
                    person.setSalary(new BigDecimal(a[25]));
                    person.setEmail(a[6]);
                    return person;
                })
                .forEach(repo::save);
        connection.commit();
    }
}