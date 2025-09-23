package com.back.domain.notification.dto;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationDto (
        @NotNull int  memberId,
        @NotBlank String message,
        @NotNull NotificationType type

){
    public  CreateNotificationDto(Notification notification)
    {
        this(
                notification.getMemberId()
                ,notification.getMessage()
                ,notification.getType()
        );
    }



}
