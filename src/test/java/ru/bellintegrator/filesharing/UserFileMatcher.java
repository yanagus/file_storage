package ru.bellintegrator.filesharing;

import org.mockito.ArgumentMatcher;
import ru.bellintegrator.filesharing.model.UserFile;

/**
 * Кастомный ArgumentMatcher
 */
public class UserFileMatcher implements ArgumentMatcher<UserFile> {

    private UserFile left;

    public UserFileMatcher() {
    }

    public UserFileMatcher(UserFile left) {
        this.left = left;
    }

    @Override
    public boolean matches(UserFile right) {
        return  left.getId() == null &&
                left.getOriginalName().equals(right.getOriginalName()) &&
                left.getFileName().endsWith("test2.txt") &&
                left.getDownloadCount().equals(right.getDownloadCount());
    }
}
