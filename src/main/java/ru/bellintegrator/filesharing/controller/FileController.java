package ru.bellintegrator.filesharing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import ru.bellintegrator.filesharing.model.UserFile;
import ru.bellintegrator.filesharing.service.FileService;

import java.util.List;

/**
 * Контроллер файлов
 */
@Controller
@RequestMapping("/files")
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Отображает все файлы в системе
     *
     * @param model модель
     * @return страница со списком файлов
     */
    @GetMapping
    public String showAllFiles(Model model) {
        List<UserFile> files = fileService.findAllFiles();

        model.addAttribute("files", files);

        return "files";
    }

    /**
     * Добавляет файл в систему
     *
     * @param file файл
     * @return страница со списком файлов
     */
    @PostMapping
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        fileService.uploadFile(file);
        return "redirect:/files";
    }

    /**
     * Загружает файл из системы
     *
     * @param fileId id файла
     * @return ResponseEntity сформированный ответ контроллера
     */
    @GetMapping("/{fileId}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile (@PathVariable(value ="fileId") String fileId) {
        Resource file = fileService.downloadFile(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

    /**
     * Удаляет файл из системы
     *
     * @param fileId id файла
     * @return страница со списком файлов
     */
    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public String deleteFile(@PathVariable(value ="fileId") String fileId) {
        fileService.deleteFile(fileId);
        return "redirect:/files";
    }
}
