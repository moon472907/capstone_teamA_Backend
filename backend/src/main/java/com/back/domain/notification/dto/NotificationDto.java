package com.back.domain.notification.dto;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record NotificationDto(
        @NotNull int id,
        @NotNull int memberID,
        @NotNull NotificationType type,
        @NotBlank  String message,
        @NotNull Boolean isRead,
        @NotNull LocalDateTime createDate,
        @NotNull LocalDateTime modifyDate

) {

    public NotificationDto(Notification notification)
    {
        this(
                notification.getId(),
                notification.getMemberId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreateDate(),
                notification.getModifyDate()
        );
    }
}
