package com.back.domain.notification.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Notification extends BaseEntity
{
    private int memberId;
    private NotificationType type;
    private String message;
    private boolean isRead;

    public Notification(int memberId, NotificationType type, String message)
    {
        this.memberId = memberId;
        this.type = type;
        this.message = message;
        this.isRead = false;
    }

}
