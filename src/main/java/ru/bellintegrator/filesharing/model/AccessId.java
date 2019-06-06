package ru.bellintegrator.filesharing.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Первичный ключ к таблице доступа к файлам
 */
@Embeddable
public class AccessId implements Serializable {

    /**
     * Уникальный идентификатор пользователя, внешний ключ
     */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /**
     * Уникальный идентификатор пользователя, запрашивающего доступ к файлу, внешний ключ
     */
    @Column(name = "subscriber_id", nullable = false)
    private Integer subscriberId;

    public AccessId() {
    }

    public AccessId(Integer userId, Integer subscriberId) {
        this.userId = userId;
        this.subscriberId = subscriberId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Integer subscriberId) {
        this.subscriberId = subscriberId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessId accessId = (AccessId) o;
        return Objects.equals(userId, accessId.userId) &&
                Objects.equals(subscriberId, accessId.subscriberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, subscriberId);
    }

    @Override
    public String toString() {
        return "AccessId{" +
                "userId=" + userId +
                ", subscriberId=" + subscriberId +
                '}';
    }
}
