package ru.bellintegrator.filesharing.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.bellintegrator.filesharing.configuration.WebSecurityConfig;
import ru.bellintegrator.filesharing.exception.NotFoundException;
import ru.bellintegrator.filesharing.model.Access;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.model.UserFile;
import ru.bellintegrator.filesharing.service.AccessService;
import ru.bellintegrator.filesharing.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit-тест контроллера доступа
 */
@RunWith(SpringRunner.class)
@WebMvcTest(AccessController.class)
@Import(WebSecurityConfig.class)
public class AccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccessService accessService;

    @MockBean
    private UserService userService;

    private User fileOwner = new User(1, "Maria", "maria", "example@example.com", null, true);
    private User notOwner = new User(2, "John", "john", "example@example.com", null, true);

    /**
     * Тест запроса на чтение
     */
    @Test
    public void askReadTest() throws Exception {
        doNothing().when(accessService).saveRequestToRead("1", notOwner);

        mockMvc.perform(post("/askread/1").with(user(notOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Request to read has been successfully sent!"));
    }

    /**
     * Тест запроса на чтение
     * Выбрасывается ошибка, если такой владелец файла не найден
     */
    @Test
    public void askReadFailTest() throws Exception {
        doThrow(new NotFoundException("No user or subscriber")).when(accessService).saveRequestToRead("3", notOwner);

        mockMvc.perform(post("/askread/3").with(user(notOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "No user or subscriber"));
    }

    /**
     * Тест запроса на чтение
     * Выбрасывается ошибка о том, что максимальный доступ (на скачивание) уже был предоставлен
     */
    @Test
    public void askReadFailTest2() throws Exception {
        doThrow(new NotFoundException("You already have the permission to access!")).when(accessService).saveRequestToRead("1", notOwner);

        mockMvc.perform(post("/askread/1").with(user(notOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "You already have the permission to access!"));
    }

    /**
     * Тест запроса на чтение
     * Выбрасывается ошибка о том, что доступ на чтение уже был предоставлен
     */
    @Test
    public void askReadFailTest3() throws Exception {
        doThrow(new NotFoundException("You already have the permission to read!")).when(accessService).saveRequestToRead("1", notOwner);

        mockMvc.perform(post("/askread/1").with(user(notOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "You already have the permission to read!"));
    }

    /**
     * Тест запроса на скачивание
     */
    @Test
    public void askDownloadTest() throws Exception {
        doNothing().when(accessService).saveRequestToDownload("1", notOwner);

        mockMvc.perform(post("/askdownload/1").with(user(notOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", "Request to download has been successfully sent!"));
    }

    /**
     * Тест запроса на скачивание
     * Выбрасывается ошибка, если такой владелец файла не найден
     */
    @Test
    public void askDownloadFailTest() throws Exception {
        doThrow(new NotFoundException("No user or subscriber")).when(accessService).saveRequestToDownload("3", notOwner);

        mockMvc.perform(post("/askdownload/3").with(user(notOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "No user or subscriber"));
    }

    /**
     * Тест запроса на скачивание
     * Выбрасывается ошибка о том, что доступ на скачивание уже был предоставлен
     */
    @Test
    public void askDownloadFailTest2() throws Exception {
        doThrow(new NotFoundException("You already have the permission to download!")).when(accessService).saveRequestToDownload("1", notOwner);

        mockMvc.perform(post("/askdownload/1").with(user(notOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "You already have the permission to download!"));
    }

    /**
     * Тест метода, отображающего список подписчиков, запрашивающих доступ
     */
    @Test
    public void showRequestingSubscribersTest() throws Exception {
        Access access = new Access(fileOwner, notOwner);
        access.setReadRequest(true);
        access.setReadAccess(true);
        List<Access> accessList = Collections.singletonList(access);

        when(accessService.getRequestingAccesses(fileOwner)).thenReturn(accessList);

        mockMvc.perform(get("/subscribers").with(user(fileOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("accesses", accessList));
    }

    /**
     * Тест метода, отображающего список подписчиков, если таковые не найдены
     */
    @Test
    public void showRequestingSubscribersTest2() throws Exception {
        when(accessService.getRequestingAccesses(fileOwner)).thenReturn(Collections.EMPTY_LIST);

        mockMvc.perform(get("/subscribers").with(user(fileOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("accesses", Collections.EMPTY_LIST));
    }

    /**
     * Тест метода, дающего разрешение на чтение
     */
    @Test
    public void allowReadTest() throws Exception {
        doNothing().when(accessService).allowRead(fileOwner, "2");

        mockMvc.perform(post("/allowread/2").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/subscribers"))
                .andExpect(redirectedUrl("/subscribers"));
    }

    /**
     * Тест метода, дающего разрешение на чтение
     * Выбрасывается ошибка, если такой подписчик не найден
     */
    @Test
    public void allowReadFailTest() throws Exception {
        doThrow(new NotFoundException("No user or subscriber")).when(accessService).allowRead(fileOwner, "3");

        mockMvc.perform(post("/allowread/3").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "No user or subscriber"));
    }

    /**
     * Тест метода, дающего разрешение на чтение
     * Выбрасывается ошибка о том, что запрос на доступ не найден
     */
    @Test
    public void allowReadFailTest2() throws Exception {
        doThrow(new NotFoundException("There is no requesting access")).when(accessService).allowRead(fileOwner, "1");

        mockMvc.perform(post("/allowread/1").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "There is no requesting access"));
    }

    /**
     * Тест метода, дающего разрешение на скачивание
     */
    @Test
    public void allowDownloadTest() throws Exception {
        doNothing().when(accessService).allowDownload(fileOwner, "2");

        mockMvc.perform(post("/allowdownload/2").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/subscribers"))
                .andExpect(redirectedUrl("/subscribers"));
    }

    /**
     * Тест метода, дающего разрешение на скачивание
     * Выбрасывается ошибка, если такой подписчик не найден
     */
    @Test
    public void allowDownloadFailTest() throws Exception {
        doThrow(new NotFoundException("No user or subscriber")).when(accessService).allowDownload(fileOwner, "3");

        mockMvc.perform(post("/allowdownload/3").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "No user or subscriber"));
    }

    /**
     * Тест метода, дающего разрешение на скачивание
     * Выбрасывается ошибка о том, что запрос на доступ не найден
     */
    @Test
    public void allowDownloadFailTest2() throws Exception {
        doThrow(new NotFoundException("There is no requesting access")).when(accessService).allowDownload(fileOwner, "1");

        mockMvc.perform(post("/allowdownload/1").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "There is no requesting access"));
    }

    /**
     * Тест метода, который отображает файлы пользователя, если нет какого-либо доступа
     */
    @Test
    public void showUserFilesWithNoAccessTest() throws Exception {
        when(accessService.findUserById("1")).thenReturn(fileOwner);
        when(accessService.findAccess("1", notOwner)).thenReturn(null);

        mockMvc.perform(get("/1/files").with(user(notOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("files", Collections.EMPTY_SET))
                .andExpect(model().attribute("fileOwner", fileOwner))
                .andExpect(model().attribute("isFileOwner", false))
                .andExpect(model().attribute("info", "No files available for you"));
    }

    /**
     * Тест метода, который отображает файлы пользователя, если доступ на чтение был запрошен
     */
    @Test
    public void showUserFilesWithNoAccessTest2() throws Exception {
        when(accessService.findUserById("1")).thenReturn(fileOwner);
        Access access = new Access(fileOwner, notOwner);
        access.setReadAccess(true);
        access.setReadRequest(true);
        when(accessService.findAccess("1", notOwner)).thenReturn(access);

        mockMvc.perform(get("/1/files").with(user(notOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("files", Collections.EMPTY_SET))
                .andExpect(model().attribute("fileOwner", fileOwner))
                .andExpect(model().attribute("isFileOwner", false))
                .andExpect(model().attribute("info", "No files available for you"));
    }

    /**
     * Тест метода, который отображает файлы пользователя, если доступ на скачивание был запрошен
     */
    @Test
    public void showUserFilesWithNoAccessTest3() throws Exception {
        when(accessService.findUserById("1")).thenReturn(fileOwner);
        Access access = new Access(fileOwner, notOwner);
        access.setDownloadAccess(true);
        access.setDownloadRequest(true);
        when(accessService.findAccess("1", notOwner)).thenReturn(access);

        mockMvc.perform(get("/1/files").with(user(notOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("files", Collections.EMPTY_SET))
                .andExpect(model().attribute("fileOwner", fileOwner))
                .andExpect(model().attribute("isFileOwner", false))
                .andExpect(model().attribute("info", "No files available for you"));
    }

    /**
     * Тест метода, который отображает файлы пользователя, если нет какого-либо доступа
     */
    @Test
    public void showUserFilesWithNoAccessTest4() throws Exception {
        when(accessService.findUserById("1")).thenReturn(fileOwner);
        Access access = new Access(fileOwner, notOwner);
        when(accessService.findAccess("1", notOwner)).thenReturn(access);

        mockMvc.perform(get("/1/files").with(user(notOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("files", Collections.EMPTY_SET))
                .andExpect(model().attribute("fileOwner", fileOwner))
                .andExpect(model().attribute("isFileOwner", false))
                .andExpect(model().attribute("info", "No files available for you"));
    }

    /**
     * Тест метода, который отображает файлы пользователя с доступом на чтение
     */
    @Test
    public void showUserFilesWithReadAccessTest() throws Exception {
        when(accessService.findUserById("1")).thenReturn(fileOwner);

        UserFile file = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        file.setUser(fileOwner);
        Set<UserFile> files = Collections.singleton(file);
        fileOwner.setFiles(files);
        Access access = new Access(fileOwner, notOwner);
        access.setReadAccess(true);
        when(accessService.findAccess("1", notOwner)).thenReturn(access);

        mockMvc.perform(get("/1/files").with(user(notOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("readAccess", true))
                .andExpect(model().attribute("files", files))
                .andExpect(model().attribute("fileOwner", fileOwner))
                .andExpect(model().attribute("isFileOwner", false));
    }

    /**
     * Тест метода, который отображает файлы пользователя с доступом на чтение и запросом на скачивание
     */
    @Test
    public void showUserFilesWithReadAccessTest2() throws Exception {
        when(accessService.findUserById("1")).thenReturn(fileOwner);

        UserFile file = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        file.setUser(fileOwner);
        Set<UserFile> files = Collections.singleton(file);
        fileOwner.setFiles(files);
        Access access = new Access(fileOwner, notOwner);
        access.setReadAccess(true);
        access.setDownloadAccess(true);
        access.setDownloadRequest(true);
        when(accessService.findAccess("1", notOwner)).thenReturn(access);

        mockMvc.perform(get("/1/files").with(user(notOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("readAccess", true))
                .andExpect(model().attribute("files", files))
                .andExpect(model().attribute("fileOwner", fileOwner))
                .andExpect(model().attribute("isFileOwner", false));
    }

    /**
     * Тест метода, который отображает файлы пользователя с доступом на скачивание
     */
    @Test
    public void showUserFilesWithDownloadAccessTest() throws Exception {
        when(accessService.findUserById("1")).thenReturn(fileOwner);

        UserFile file = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        file.setUser(fileOwner);
        Set<UserFile> files = Collections.singleton(file);
        fileOwner.setFiles(files);
        Access access = new Access(fileOwner, notOwner);
        access.setDownloadAccess(true);
        when(accessService.findAccess("1", notOwner)).thenReturn(access);

        mockMvc.perform(get("/1/files").with(user(notOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("readAccess", false))
                .andExpect(model().attribute("files", files))
                .andExpect(model().attribute("fileOwner", fileOwner))
                .andExpect(model().attribute("isFileOwner", false));
    }

    /**
     * Тест метода, который отображает файлы пользователя при запросе от владельца файлов
     */
    @Test
    public void showUserFilesTest() throws Exception {
        when(accessService.findUserById("1")).thenReturn(fileOwner);

        UserFile file = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        file.setUser(fileOwner);
        Set<UserFile> files = Collections.singleton(file);
        fileOwner.setFiles(files);

        mockMvc.perform(get("/1/files").with(user(fileOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("readAccess", false))
                .andExpect(model().attribute("files", files))
                .andExpect(model().attribute("fileOwner", fileOwner))
                .andExpect(model().attribute("isFileOwner", true));
    }
}
