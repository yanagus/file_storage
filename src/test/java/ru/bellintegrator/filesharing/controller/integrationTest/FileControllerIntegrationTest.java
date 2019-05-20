package ru.bellintegrator.filesharing.controller.integrationTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import ru.bellintegrator.filesharing.model.UserFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тест контроллера файлов
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = {"/files-list-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/files-list-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class FileControllerIntegrationTest {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private MockMvc mockMvc;

    /**
     * Тест метода, возвращающего список файлов
     *
     * @throws Exception
     */
    @Test
    public void showAllFilesTest() throws Exception {
        List<UserFile> files = new ArrayList<>();
        Collections.addAll(files,
                new UserFile(1,
                        "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0),
                new UserFile(2,
                        "2a761259-f65c-4d58-a209-249be96d7a55.test2.txt", "test2.txt", 0));
        mockMvc.perform(get("/files"))
                .andDo(print())
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
        mockMvc.perform(multipart("/files").file(multipartFile))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/files"))
                .andExpect(redirectedUrl("/files"));
        deleteCreatedFile(multipartFile.getOriginalFilename());
    }

    /**
     * Тест метода сохранения непереданного файла
     * @throws Exception
     */
    @Test
    public void missingUploadFileTest() throws Exception {
        MockHttpServletRequestBuilder multipart = multipart("/files")
                .file("file", null);
        mockMvc.perform(multipart)
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "Select file!"));
    }

    /**
     * Тест метода загрузки файла
     *
     * @throws Exception
     */
    @Test
    public void downloadFileTest() throws Exception {
        mockMvc.perform(get("/files/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt\""))
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Length", "4"))
                .andExpect(content().string("test"));
    }

    /**
     * Тест метода загрузки файла, который отсутствует в базе данных
     *
     * @throws Exception
     */
    @Test
    public void downloadMissingFileTest() throws Exception {
        mockMvc.perform(get("/files/15"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "There is no file with id 15"));
    }

    /**
     * Тест метода загрузки файла, который отсутствует на диске
     *
     * @throws Exception
     */
    @Test
    public void downloadMissingFileTest2() throws Exception {
        mockMvc.perform(get("/files/2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error",
                        "Could not read file: 2a761259-f65c-4d58-a209-249be96d7a55.test2.txt"));
    }

    /**
     * Тест метода удаления файла
     */
    @Test
    public void deleteFileTest() throws Exception {
        File file = new File(uploadPath + "/2a761259-f65c-4d58-a209-249be96d7a55.test2.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        Assert.assertTrue(file.exists());

        mockMvc.perform(delete("/files/2"))
                .andDo(print())
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/files"));

        Assert.assertFalse(file.exists());
    }

    /**
     * Тест метода удаления файла с несуществующим Id
     *
     * @throws Exception
     */
    @Test
    public void deleteNotExistFileFailTest() throws Exception {
        mockMvc.perform(delete("/files/15"))
                .andDo(print())
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
        mockMvc.perform(delete("/files/b"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "The file id must not be null or character!"));
    }

    /**
     * Тест метода удаления файла, который отсутствует на диске
     */
    @Test
    public void deleteNotExistFileFailTest3() throws Exception {
        File file = new File(uploadPath + "/2a761259-f65c-4d58-a209-249be96d7a55.test2.txt");
        Assert.assertFalse(file.exists());

        mockMvc.perform(delete("/files/2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("error", "The file was not deleted!"));
    }

    /**
     * Удалить созданный файл
     */
    private void deleteCreatedFile(String endFilename) {
        File dir = new File(uploadPath);
        File file = Arrays.stream(dir.listFiles())
                .filter(f -> f.getName().endsWith(endFilename))
                .findFirst()
                .orElse(null);
        if (file != null) {
            file.delete();
        }
    }

}
