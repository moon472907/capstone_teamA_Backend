package com.back.domain.notification.service;


import com.back.domain.notification.dto.CreateNotificationDto;
import com.back.domain.notification.dto.ModifyNotificationDto;
import com.back.domain.notification.dto.NotificationDto;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

private final NotificationRepository notificationRepository ;

    public NotificationDto createNotification(CreateNotificationDto CreateNotificationDto) {
        Notification notification = new Notification(
                CreateNotificationDto.memberId(),
                CreateNotificationDto.type(),
                CreateNotificationDto.message()
        );
        Notification tmp = notificationRepository.save(notification);
        System.out.println("Created Notification: " + tmp);
        return new NotificationDto(tmp);
    }

    public NotificationDto findById(int id) {
        return new NotificationDto(notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id)));

    }

    public List<NotificationDto> findAll() {
        return notificationRepository.findAll().stream().map(NotificationDto::new).toList();
    }

    public void deleteNotification(int id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
        notificationRepository.delete(notification);
    }

    public NotificationDto updateNotification(int id, ModifyNotificationDto notificationDto)
    {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
        notification.setMemberId(notificationDto.memberId());
        notification.setMessage(notificationDto.message());
        notification.setType(notificationDto.type());
        notificationRepository.save(notification);
        return new NotificationDto(notification);

    }

    public NotificationDto markAsRead(int id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
        notification.setRead(true);
        notificationRepository.save(notification);
        return new NotificationDto(notification);
    }

    public List<NotificationDto> findByUserId(int userId) {
        return notificationRepository.findByMemberId(userId)
                .stream()
                .map(NotificationDto::new) // 생성자 참조
                .toList();

    }

    public List<NotificationDto> findByUserIdAndIsRead(int userId, boolean isRead) {
        return notificationRepository.findByMemberIdAndIsRead(userId, isRead)
                .stream()
                .map(NotificationDto::new) // 생성자 참조
                .toList();
    }


}
