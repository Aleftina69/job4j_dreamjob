package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.model.User;

import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Sql2oUserRepositoryTest {

    private static Sql2o sql2o;
    private static Sql2oUserRepository sql2oUserRepository;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oUserRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        sql2o = new Sql2o(url, username, password);
        sql2oUserRepository = new Sql2oUserRepository(sql2o);

        try (var connection = sql2o.open()) {
            String sql = """
                    CREATE TABLE users (
                        id SERIAL PRIMARY KEY,
                        email VARCHAR(255) UNIQUE,
                        name VARCHAR(255),
                        password VARCHAR(255)
                    );
                    """;
            connection.createQuery(sql).executeUpdate();
        }
    }

    @AfterAll
    public static void cleanUp() {
        try (var connection = sql2o.open()) {
            connection.createQuery("DROP TABLE users").executeUpdate();
        }
    }

    @Test
    void whenSaveUserThenUserIsSaved() {
        User user = new User(0, "user@example.com", "User", "pass");
        Optional<User> saved = sql2oUserRepository.save(user);

        assertThat(saved).isPresent();
        assertThat(saved.get().getId()).isPositive();
        assertThat(saved.get().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void whenSaveUsersWithSameEmailThenSecondAddFails() {
        User user1 = new User(0, "same@example.com", "User 1", "pass1");
        User user2 = new User(0, "same@example.com", "User 2", "pass2");

        Optional<User> saved1 = sql2oUserRepository.save(user1);
        assertThat(saved1).isPresent();

        assertThatThrownBy(() -> sql2oUserRepository.save(user2))
                .isInstanceOf(org.sql2o.Sql2oException.class)
                .hasMessageContaining("Нарушение уникального индекса или первичного ключа");
    }
}
    