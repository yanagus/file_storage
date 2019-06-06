package ru.bellintegrator.filesharing.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.bellintegrator.filesharing.configuration.WebSecurityConfig;
import ru.bellintegrator.filesharing.exception.AccessException;
import ru.bellintegrator.filesharing.exception.NotFoundException;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.model.UserFile;
import ru.bellintegrator.filesharing.service.FileService;
import ru.bellintegrator.filesharing.service.UserService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

/**
 * Unit-тест контроллера файлов
 */
@RunWith(SpringRunner.class)
@WebMvcTest(FileController.class)
@Import(WebSecurityConfig.class)
public class FileControllerTest {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @MockBean
    private UserService userService;

    private User fileOwner = new User(1, "Maria", "maria", "example@example.com", null, true);
    private User notOwner = new User(2, "John", "john", "example@example.com", null, true);

    /**
     * Тест метода, возвращающего список файлов
     */
    @Test
    public void showAllFilesTest() throws Exception {
        UserFile file = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        file.setUser(fileOwner);
        List<UserFile> files = Collections.singletonList(file);

        when(fileService.findAllFiles()).thenReturn(files);

        mockMvc.perform(get("/files").with(user(fileOwner))
                .contentType(MediaType.TEXT_HTML))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("files", files));
    }

    /**
     * Тест метода сохранения файлов
     *
     * @throws Exception
     */
    @Test
    public void uploadFileTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test2.txt",
                "text/plain", "test2".getBytes());
        doNothing().when(fileService).uploadFile(fileOwner, multipartFile);
        mockMvc.perform(multipart("/1/files").file(multipartFile).with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/1/files"))
                .andExpect(redirectedUrl("/1/files"));
        verify(fileService).uploadFile(fileOwner, multipartFile);
    }

    /**
     * Тест метода сохранения непереданного файла
     *
     * @throws Exception
     */
    @Test
    public void missingUploadFileTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", ".txt",
                "text/plain", "".getBytes());
        doThrow(new NotFoundException("Select file!")).when(fileService).uploadFile(fileOwner, multipartFile);
        mockMvc.perform(multipart("/1/files").file(multipartFile).with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "Select file!"));
        verify(fileService, times(1)).uploadFile(fileOwner, multipartFile);
    }

    /**
     * Тест метода сохранения файла с выбрасыванием ошибки при преобразовании MultipartFile в File
     *
     * @throws Exception
     */
    @Test
    public void missingPathUploadFileTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test2.txt",
                "text/plain", "test2".getBytes());
        doThrow(new NotFoundException("The file or path was not found!")).when(fileService).uploadFile(fileOwner, multipartFile);
        mockMvc.perform(multipart("/1/files").file(multipartFile).with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "The file or path was not found!"));
        verify(fileService, times(1)).uploadFile(fileOwner, multipartFile);
    }

    /**
     * Тест метода загрузки файла
     *
     * @throws Exception
     */
    @Test
    public void downloadFileTest() throws Exception {
        UserFile userFile = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        userFile.setUser(fileOwner);
        Path rootLocation = Paths.get(uploadPath);
        Path file = rootLocation.resolve(userFile.getFileName());
        Resource resource = new UrlResource(file.toUri());
        when(fileService.downloadFile(fileOwner, "5")).thenReturn(resource);
        mockMvc.perform(get("/files/5").with(user(fileOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt\""))
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Length", "4"))
                .andExpect(content().string("test"));
    }

    /**
     * Тест метода загрузки файла пользователем-не владельцем и не имеющим доступа
     *
     * @throws Exception
     */
    @Test
    public void downloadFileByNotOwnerTest() throws Exception {
        when(fileService.downloadFile(notOwner, "5")).thenThrow(new AccessException("You need permission to perform this action!"));

        mockMvc.perform(get("/files/5").with(user(notOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("error", "You need permission to perform this action!"));
    }

    /**
     * Тест метода загрузки файла, который отсутствует в базе данных
     *
     * @throws Exception
     */
    @Test
    public void downloadMissingFileTest() throws Exception {
        when(fileService.downloadFile(fileOwner, "15")).thenThrow(new NotFoundException("There is no file with id 15"));

        mockMvc.perform(get("/files/15").with(user(fileOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "There is no file with id 15"));
    }

    /**
     * Тест метода загрузки файла, который отсутствует в базе данных
     *
     * @throws Exception
     */
    @Test
    public void downloadMissingFileTest2() throws Exception {
        when(fileService.downloadFile(fileOwner, "15"))
                .thenThrow(new NotFoundException("Could not read file: 47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt"));

        mockMvc.perform(get("/files/15").with(user(fileOwner)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "Could not read file: 47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt"));
    }

    /**
     * Тест метода удаления файла
     */
    @Test
    public void deleteFileTest() throws Exception {
        doNothing().when(fileService).deleteFile(fileOwner,"2");

        mockMvc.perform(delete("/1/files/2").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/1/files"));
    }

    /**
     * Тест метода удаления файла пользователем-не владельцем файла
     *
     */
    @Test
    public void deleteFileByNotOwnerTest() throws Exception {
        doThrow(new AccessException("You can not delete not your file!")).when(fileService).deleteFile(notOwner,"2");

        mockMvc.perform(delete("/1/files/2").with(user(notOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isForbidden())
                .andExpect(model().attribute("error", "You can not delete not your file!"));
    }

    /**
     * Тест метода удаления файла с несуществующим Id
     *
     * @throws Exception
     */
    @Test
    public void deleteNotExistFileFailTest() throws Exception {
        doThrow(new NotFoundException("There is no file with id 15")).when(fileService).deleteFile(fileOwner, "15");

        mockMvc.perform(delete("/1/files/15").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "There is no file with id 15"));
    }

    /**
     * Тест метода удаления файла, если id - не цифра
     *
     * @throws Exception
     */
    @Test
    public void deleteNotExistFileFailTest2() throws Exception {
        doThrow(new NotFoundException("The file id must not be null or character!")).when(fileService).deleteFile(fileOwner, "b");

        mockMvc.perform(delete("/1/files/b").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "The file id must not be null or character!"));
    }

    /**
     * Тест метода удаления файла, который отсутствует на диске
     */
    @Test
    public void deleteNotExistFileFailTest3() throws Exception {
        doThrow(new NotFoundException("The file was not deleted!")).when(fileService).deleteFile(fileOwner,"2");

        mockMvc.perform(delete("/1/files/2").with(user(fileOwner)).with(csrf()))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "The file was not deleted!"));
    }
}
