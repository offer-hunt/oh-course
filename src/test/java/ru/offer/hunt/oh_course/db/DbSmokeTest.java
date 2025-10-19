package ru.offer.hunt.oh_course.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        properties = {
                "spring.flyway.enabled=true",
                "spring.flyway.schemas=learning",
                "spring.flyway.baseline-on-migrate=true"
        })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DbSmokeTest {

    static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("course_db")
                    .withUsername("course_user")
                    .withPassword("course_password");

    static {
        PG.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", PG::getJdbcUrl);
        r.add("spring.datasource.username", PG::getUsername);
        r.add("spring.datasource.password", PG::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        r.add("spring.flyway.enabled", () -> true);
        r.add("spring.flyway.schemas", () -> "course");
    }

    @Autowired private JdbcTemplate jdbc;

    @Test
    void schema() {
        Integer cnt =
                jdbc.queryForObject(
                        "select count(*) from information_schema.tables where table_schema = 'course' and table_name='__migration_probe'",
                        Integer.class);
        assertThat(cnt).isNotNull().isGreaterThanOrEqualTo(1);

        Long id =
                jdbc.queryForObject(
                        "insert into course.__migration_probe default values returning id", Long.class);
        assertThat(id).isNotNull().isPositive();
    }
}