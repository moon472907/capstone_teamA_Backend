package com.back.domain.notification.Controller;


import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.notification.dto.CreateNotificationDto;
import com.back.domain.notification.dto.ModifyNotificationDto;
import com.back.domain.notification.dto.NotificationDto;
import com.back.domain.notification.service.NotificationService;
import com.back.global.common.ApiResponse;
import com.back.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/notification")
@RequiredArgsConstructor
@Tag(name = "ApiV1NotificationController", description = "API 알림 컨트롤러")
public class ApiV1NotificationController {
    private final NotificationService notificationService;
    private final MemberService memberService;
    private final Rq rq;

    @PostMapping
    @Transactional
    @Operation(summary = "알림 생성")
    public ApiResponse<NotificationDto> createNotification(@Valid @RequestBody CreateNotificationDto rqData)
    {

        NotificationDto NotificationDto = notificationService.createNotification(rqData);
        if (NotificationDto == null) {
            throw new RuntimeException("알림 생성 실패");
        }
        return new ApiResponse<>("200-1", "알림이 생성되었습니다.", NotificationDto);
    }


    @GetMapping
    @Transactional
    @Operation(summary = "알림 전체 조회")
    public ApiResponse<List<NotificationDto>> findAllNotifications()
    {
        return new ApiResponse<>("200", "알림 전체 조회 성공", notificationService.findAll());
    }

    @GetMapping("/{id}")
    @Transactional
    @Operation(summary = "알림 단건 조회")
    public ApiResponse<NotificationDto> findNotifications(@PathVariable int id )
    {
        return new ApiResponse<>("200", "알림 전체 조회 성공", notificationService.findById(id));
    }





    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "알림 수정")
    public ApiResponse<NotificationDto> ModifyNotification(@PathVariable int id , @Valid @RequestBody ModifyNotificationDto rqData)
    {
        return new ApiResponse<>("200", "알림 전체 조회 성공", notificationService.updateNotification(id, rqData));
    }

    @PutMapping("/read/{id}")
    @Transactional
    @Operation(summary = "알림 읽음 처리")
    public ApiResponse<NotificationDto> ModifyNotification(@PathVariable int id)
    {
        return new ApiResponse<>("200", "알림 전체 조회 성공", notificationService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "알림 삭제")
    public ApiResponse<?> deleteNotification(@PathVariable int id)
    {
        notificationService.deleteNotification(id);
        return new ApiResponse<>("200", "알림 삭제 성공", null);
    }

    @GetMapping("/me")
    @Transactional
    @Operation
    public ApiResponse<List<NotificationDto>> findNotificationsByUserId(
    ) {
        Member member = rq.getActorFromDb();

        return new ApiResponse<>("200", "사용자 알림 조회 성공", notificationService.findByUserId(member.getId()));
    }
}
