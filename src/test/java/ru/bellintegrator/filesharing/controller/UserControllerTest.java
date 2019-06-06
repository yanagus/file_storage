package ru.bellintegrator.filesharing.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.bellintegrator.filesharing.configuration.WebSecurityConfig;
import ru.bellintegrator.filesharing.exception.NotFoundException;
import ru.bellintegrator.filesharing.exception.ServiceException;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.service.UserService;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@Import(WebSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User user = new User(1, "Maria", "maria", "example@example.com", null, true);

    /**
     * Тест метода, возвращающего список пользователей
     */
    @Test
    public void showUserListTest() throws Exception {
        List<User> users = Collections.singletonList(user);

        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/users").with(user(user))
                .contentType(MediaType.TEXT_HTML))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("users", users));
    }

    /**
     * Тест метода, отображающего форму для регистрации нового пользователя
     */
    @Test
    public void showRegistrationFormTest() throws Exception {
        mockMvc.perform(get("/registration"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add new user")));
    }

    /**
     * Тест успешно пройденной регистрации
     */
    @Test
    public void registrationTest() throws Exception {
        User newUser = new User();
        newUser.setUsername("user");
        newUser.setPassword("password");
        newUser.setPassword2("password");
        newUser.setEmail("example@example.com");
        doNothing().when(userService).addUser(newUser);

        mockMvc.perform(post("/registration")
                .param("username", "user")
                .param("password", "password")
                .param("password2", "password")
                .param("email", "example@example.com")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Activate your account through the link from your e-mail"));
    }

    /**
     * Тест регистрации при отличающемся подтверждении пароля
     */
    @Test
    public void registrationWithDifferentPasswordTest() throws Exception {
        mockMvc.perform(post("/registration")
                .param("username", "user")
                .param("password", "password")
                .param("password2", "pass")
                .param("email", "example@example.com")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("passwordError", "Passwords are different!"));
    }

    /**
     * Тест регистрации нового пользователя при невведённых данных
     */
    @Test
    public void registrationWithEmptyFieldsTest() throws Exception {
        mockMvc.perform(post("/registration")
                .param("username", "")
                .param("password", "")
                .param("password2", "")
                .param("email", "")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("usernameError", "Enter the name!"))
                .andExpect(model().attribute("passwordError", "Enter the password!"))
                .andExpect(model().attribute("password2Error", "Password confirmation can not be empty!"))
                .andExpect(model().attribute("emailError", "Enter the e-mail!"));
    }

    /**
     * Тест регистрации нового пользователя с существующим именем пользователя в системе
     */
    @Test
    public void registrationByExistUsernameTest() throws Exception {
        User newUser = new User();
        newUser.setUsername("user");
        newUser.setPassword("password");
        newUser.setPassword2("password");
        newUser.setEmail("example@example.com");
        doThrow(new ServiceException("User exists!")).when(userService).addUser(newUser);

        mockMvc.perform(post("/registration")
                .param("username", "user")
                .param("password", "password")
                .param("password2", "password")
                .param("email", "example@example.com")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "User exists!"));
    }

    /**
     * Тест метода, отображающего форму для авторизации
     */
    @Test
    public void showLoginFormTest() throws Exception {
        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign In")));
    }

    /**
     * Тест неуспешной авторизации
     */
    @Test
    public void loginErrorTest() throws Exception {
        mockMvc.perform(formLogin().user("user").password("invalid"))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    /**
     * Тест выхода из системы
     */
    @Test
    public void logoutTest() throws Exception {
        mockMvc.perform(logout())
                .andExpect(unauthenticated());
    }

    /**
     * Тест метода, активирующего нового пользователя
     */
    @Test
    public void activateUserTest() throws Exception {
        User newUser = new User(3, "user", "password",
                "example@example.com", "b9ffdee1-d74e-4324-863f-2d86324faa19", false);
        newUser.setRegistrationDate(new Date());

        doNothing().when(userService).activateUser(newUser.getActivationCode());
        mockMvc.perform(get("/activate/b9ffdee1-d74e-4324-863f-2d86324faa19"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "User successfully activated"));
    }

    /**
     * Тест метода, активирующего нового пользователя
     * Выбрасывает ошибку, когда пользователь не найден по коду активации
     */
    @Test
    public void activateUserWithWrongCodeTest() throws Exception {
        doThrow(new NotFoundException("Activation code has not found!")).when(userService).activateUser("b9ffdee1-d74e-4324-863f-2d86324faa19");
        mockMvc.perform(get("/activate/b9ffdee1-d74e-4324-863f-2d86324faa19"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "Activation code has not found!"));
    }

    /**
     * Тест метода, активирующего нового пользователя
     * Выбрасывает ошибку, когда код активации просрочен
     */
    @Test
    public void activateUserWithWrongCodeTest2() throws Exception {
        doThrow(new ServiceException("Activation link has expired! The new one has been sent"))
                .when(userService).activateUser("b9ffdee1-d74e-4324-863f-2d86324faa19");
        mockMvc.perform(get("/activate/b9ffdee1-d74e-4324-863f-2d86324faa19"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Activation link has expired! The new one has been sent"));
    }
}
