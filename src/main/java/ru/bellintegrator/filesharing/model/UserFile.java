package ru.bellintegrator.filesharing.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;
import java.util.Objects;

/**
 * Файл
 */
@Entity
@Table(name = "file")
public class UserFile implements Serializable {

    /**
     * Уникальный идентификатор файла
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * Служебное поле Hibernate
     */
    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * Название файла
     */
    @Column(name = "file_name", length = 200)
    private String fileName;

    /**
     * Оригинальное название файла
     */
    @Column(name = "original_name", length = 200)
    private String originalName;

    /**
     * Количество скачиваний
     */
    @Column(name = "download_count")
    private Integer downloadCount;

    public UserFile() {
    }

    public UserFile(Integer id, String fileName, String originalName, Integer downloadCount) {
        this.id = id;
        this.fileName = fileName;
        this.originalName = originalName;
        this.downloadCount = downloadCount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFile userFile = (UserFile) o;
        return Objects.equals(id, userFile.id) &&
                Objects.equals(fileName, userFile.fileName) &&
                Objects.equals(originalName, userFile.originalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName, originalName);
    }

    @Override
    public String toString() {
        return "UserFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", originalName='" + originalName + '\'' +
                ", downloadCount=" + downloadCount +
                '}';
    }
}
