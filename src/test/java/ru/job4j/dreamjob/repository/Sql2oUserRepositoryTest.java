package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class Sql2oUserRepositoryTest {

    private static Sql2oUserRepository sql2oUserRepository;
    private static Sql2o sql2o;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oUserRepositoryTest.class.getClassLoader()
                .getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        sql2o = configuration.databaseClient(datasource);
        try (var connection = sql2o.open()) {
            connection.createQuery(
                    "CREATE TABLE IF NOT EXISTS users ("
                            + "id SERIAL PRIMARY KEY,"
                            + "email VARCHAR(255) UNIQUE,"
                            + "name VARCHAR(255),"
                            + "password VARCHAR(255)"
                            + ")"
            ).executeUpdate();
        }
        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void clearUsers() {
        try (var connection = sql2o.open()) {
            connection.createQuery("DELETE FROM users").executeUpdate();
        }
    }

    @Test
    public void whenSaveThenGetSame() {
        var user = new User(0, "test@yandex.ru", "test", "test");
        var savedUser  = sql2oUserRepository.save(user);
        assertThat(savedUser.get()).usingRecursiveAssertion().isEqualTo(user);
    }

    @Test
    public void whenSaveSeveralThenGetByEmailAndPassword() {
        var user1 = sql2oUserRepository.save(new User(0, "test1@yandex.ru", "test1", "test1"));
        var user2 = sql2oUserRepository.save(new User(0, "test2@yandex.ru", "test2", "test2"));
        var user3 = sql2oUserRepository.save(new User(0, "test3@yandex.ru", "test3", "test3"));
        var savedUser1 = sql2oUserRepository.findByEmailAndPassword(user1.get().getEmail(), user1.get().getPassword()).get();
        var savedUser2 = sql2oUserRepository.findByEmailAndPassword(user2.get().getEmail(), user2.get().getPassword()).get();
        var savedUser3 = sql2oUserRepository.findByEmailAndPassword(user3.get().getEmail(), user3.get().getPassword()).get();
        assertThat(savedUser1).usingRecursiveAssertion().isEqualTo(user1.get());
        assertThat(savedUser2).usingRecursiveAssertion().isEqualTo(user2.get());
        assertThat(savedUser3).usingRecursiveAssertion().isEqualTo(user3.get());
    }

    @Test
    public void whenSaveSameUsersThenError() {
        sql2oUserRepository.save(new User(0, "test@yandex.ru", "test", "test"));
        assertThat(sql2oUserRepository.save(new User(0, "test@yandex.ru", "test1", "test1")).isEmpty());
    }
}