package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndexControllerTest {

    private IndexController indexController;
    private Model model;
    private HttpSession session;

    @BeforeEach
    public void initServices() {
        indexController = new IndexController();
        model = new ConcurrentModel();
        session = mock(HttpSession.class);
    }

    @Test
    public void whenGetIndexWithoutUserThenAddGuestToModel() {
        when(session.getAttribute("user")).thenReturn(null);
        String view = indexController.getIndex(model, session);
        assertThat(view).isEqualTo("index");
        assertThat(model.getAttribute("user")).isNotNull();
        assertThat(model.getAttribute("user")).isInstanceOf(User.class);
        User user = (User) model.getAttribute("user");
        assertThat(user.getName()).isEqualTo("Гость");
    }

    @Test
    public void whenGetIndexWithUserThenAddUserToModel() {
        User user = new User();
        user.setName("Пользователь");
        when(session.getAttribute("user")).thenReturn(user);
        String view = indexController.getIndex(model, session);
        assertThat(view).isEqualTo("index");
        assertThat(model.getAttribute("user")).isNotNull();
        assertThat(model.getAttribute("user")).isInstanceOf(User.class);
        User retrievedUser  = (User) model.getAttribute("user");
        assertThat(retrievedUser.getName()).isEqualTo("Пользователь");
    }
}
