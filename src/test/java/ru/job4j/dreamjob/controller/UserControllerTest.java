package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserService userService;
    private UserController userController;
    private MultipartFile multipartFile;
    private MockHttpSession session;
    private MockHttpServletRequest request;
    private Model model;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
        multipartFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
        session = new MockHttpSession();
        model = new ConcurrentModel();
        request = new MockHttpServletRequest();
    }

    @Test
    public void whenGetRegistrationPageThenReturnRegistrationView() {
        String view = userController.getRegistrationPage(model);
        assertThat(view).isEqualTo("users/register");
        assertThat(model.getAttribute("user")).isNotNull();
    }

    @Test
    public void whenRegisterUserThenRedirectToVacancies() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        when(userService.save(user)).thenReturn(Optional.of(user));

        String view = userController.register(model, user);
        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    public void whenRegisterUserWithExistingEmailThenShowErrorMessage() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        when(userService.save(user)).thenReturn(Optional.empty());

        String view = userController.register(model, user);
        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Пользователь с такой почтой уже существует");
    }

    @Test
    public void whenGetLoginPageThenReturnLoginView() {
        String view = userController.getLoginPage(model);
        assertThat(view).isEqualTo("users/login");
        assertThat(model.getAttribute("user")).isNotNull();
    }

    @Test
    public void whenLoginUserWithValidCredentialsThenRedirectToVacancies() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        User validUser  = new User();
        validUser .setEmail("test@example.com");
        validUser .setPassword("password");

        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword())).thenReturn(Optional.of(validUser));
        String view = userController.loginUser(user, model, request);
        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(request.getSession().getAttribute("user")).isEqualTo(validUser);
    }

    @Test
    public void whenLoginUserWithInvalidCredentialsThenReturnLoginViewWithError() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("wrongpassword");

        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword())).thenReturn(Optional.empty());

        String view = userController.loginUser(user, model, request);
        assertThat(view).isEqualTo("users/login");
        assertThat(model.getAttribute("error")).isEqualTo("Почта или пароль введены неверно");
    }
}
