package ru.bellintegrator.filesharing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.bellintegrator.filesharing.model.Access;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.model.UserFile;
import ru.bellintegrator.filesharing.service.AccessService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Контроллер, обрабатывающий запросы на доступ к файлам
 */
@Controller
public class AccessController {

    private final AccessService accessService;

    @Autowired
    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    /**
     * Запрашивает доступ у другого пользователя на просмотр списка его файлов
     *
     * @param currentUser текущий пользователь
     * @param userId id пользователя, у которого запрашивается доступ
     * @return страница со списком файлов пользователя
     */
    @PostMapping("/askread/{userId}")
    public String askRead(@AuthenticationPrincipal User currentUser,
                          @PathVariable(value ="userId") String userId,
                          Model model) {
        accessService.saveRequestToRead(userId, currentUser);
        model.addAttribute("info", "Request to read has been successfully sent!");
        return "info";
    }

    /**
     * Запрашивает доступ у другого пользователя на скачивание его файлов
     *
     * @param currentUser текущий пользователь
     * @param userId id пользователя, у которого запрашивается доступ
     * @return страница со списком файлов пользователя
     */
    @PostMapping("/askdownload/{userId}")
    public String askDownload(@AuthenticationPrincipal User currentUser,
                              @PathVariable(value ="userId") String userId,
                              Model model) {
        accessService.saveRequestToDownload(userId, currentUser);
        model.addAttribute("info", "Request to download has been successfully sent!");
        return "info";
    }

    /**
     * Показывает список подписчиков, запрашивающих доступ
     *
     * @param currentUser текущий пользователь
     * @param model модель
     * @return список подписчиков, запрашивающих доступ
     */
    @GetMapping("/subscribers")
    public String showRequestingSubscribers(@AuthenticationPrincipal User currentUser,
                                  Model model) {
        List<Access> accesses = accessService.getRequestingAccesses(currentUser);
        model.addAttribute("accesses", accesses);
        return "subscribers";
    }

    /**
     * Разрешает просмотр файлов пользователю
     *
     * @param currentUser текущий пользователь
     * @param subscriberId id пользователя, запрашивающий доступ
     * @return список подписчиков, запрашивающих доступ
     */
    @PostMapping("/allowread/{subscriberId}")
    public String allowRead(@AuthenticationPrincipal User currentUser,
                            @PathVariable(value ="subscriberId") String subscriberId) {
        accessService.allowRead(currentUser, subscriberId);
        return "redirect:/subscribers";
    }

    /**
     * Разрешает скачивание файлов пользователю
     *
     * @param currentUser текущий пользователь
     * @param subscriberId id пользователя, запрашивающий доступ
     * @return список подписчиков, запрашивающих доступ
     */
    @PostMapping("/allowdownload/{subscriberId}")
    public String allowDownload(@AuthenticationPrincipal User currentUser,
                                @PathVariable(value ="subscriberId") String subscriberId) {
        accessService.allowDownload(currentUser, subscriberId);
        return "redirect:/subscribers";
    }

    /**
     * Отображает файлы пользователя
     *
     * @param currentUser текущий пользователь
     * @param fileOwnerId id владелеца файлов
     * @param model модель
     * @return страница со списком файлов запрашиваемого пользователя
     */
    @GetMapping("/{fileOwnerId}/files")
    public String showUserFiles(
            @AuthenticationPrincipal User currentUser,
            @PathVariable(value ="fileOwnerId") String fileOwnerId,
            Model model
    ) {
        User fileOwner = accessService.findUserById(fileOwnerId);

        if(isFileOwner(currentUser, fileOwner)) {
            model.addAttribute("readAccess", false);
            return addAttributes(model, fileOwner.getFiles(), fileOwner, true,null);
        }

        Access access = accessService.findAccess(fileOwnerId, currentUser);
        if (access != null){
            if(!access.getDownloadRequest() && access.getDownloadAccess()) {
                model.addAttribute("readAccess", false);
                return addAttributes(model, access.getUser().getFiles(), access.getUser(),false,null);
            }
            if(!access.getReadRequest() && access.getReadAccess()) {
                if ((!access.getDownloadRequest() && !access.getDownloadAccess()) || access.getDownloadRequest()) {
                    model.addAttribute("readAccess", true);
                    return addAttributes(model, access.getUser().getFiles(), access.getUser(), false,null);
                }
            } else {
                return addAttributes(model, Collections.EMPTY_SET, access.getUser(), false,
                        "No files available for you");
            }
        }
        return addAttributes(model, Collections.EMPTY_SET, fileOwner, false,
                "No files available for you");
    }

    /**
     * Добавляет атрибуты в модель
     *
     * @param model модель
     * @param files список файлов
     * @param fileOwner владелец файлов
     * //@param file выбранный файл
     * @param isFileOwner true, если пользователь - владелец файлов
     * @return страница со списком файлов запрашиваемого пользователя
     */
    private String addAttributes(Model model, Set<UserFile> files, User fileOwner, boolean isFileOwner, String info) {
        model.addAttribute("files", files);
        model.addAttribute("fileOwner", fileOwner);
        model.addAttribute("isFileOwner", isFileOwner);
        model.addAttribute("info", info);
        return "userFiles";
    }

    /**
     * Определяет является ли текущий пользователь владельцем файлов
     *
     * @param currentUser текущий пользователь
     * @param fileOwner владелец файлов
     * @return true, если пользователь один и тот же
     */
    private boolean isFileOwner(User currentUser, User fileOwner) {
        return currentUser.getId().equals(fileOwner.getId());
    }
}
