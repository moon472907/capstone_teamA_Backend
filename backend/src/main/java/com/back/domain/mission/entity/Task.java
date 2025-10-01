package com.back.domain.mission.entity;

import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_goal_id", nullable = false)
    private SubGoal subGoal;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int dayNum;  // 1=월, 2=화, ... 7=일

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasBeenEdited = false;  // Task별 수정 여부

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TaskLog> taskLogs = new ArrayList<>();

    /*
      수정 가능 여부 판단
     - 이미 수정했으면 불가
     - 1주 미션: 생성 당일만
     - 2주+ 미션: 해당 주차 금요일까지
     */
    public boolean canEdit() {
        // 1. 이미 수정했으면 불가
        if (Boolean.TRUE.equals(hasBeenEdited)) {
            return false;
        }

        SubGoal week = this.subGoal;
        LocalDate today = LocalDate.now();

        // 2. 1주 미션의 특별 규칙
        if (week.getOrderNum() == 1) {
            LocalDate weekStart = week.getStartDate();

            // 미션 시작 전
            if (today.isBefore(weekStart)) {
                // 이번 주 금요일까지 수정 가능
                LocalDate friday = weekStart.plusDays(4);
                return !today.isAfter(friday);
            }
            // 미션 시작일(월요일)
            else if (today.equals(weekStart)) {
                // 월요일에 생성했으면 당일만
                return true;
            }
            // 화요일부터는 수정 불가
            else {
                return false;
            }
        }

        // 3. 2주+ 미션은 해당 주차 금요일까지
        LocalDate friday = week.getStartDate().plusDays(4);
        return !today.isAfter(friday);
    }

    /*
      수정 마감일
      - 1주 미션:  시작 전이면 금요일, 시작 후면 당일
      - 2주+ 미션: 해당 주차 금요일
     */
    public LocalDate getEditDeadline() {
        SubGoal week = this.subGoal;

        // 1주차
        if (week.getOrderNum() == 1) {
            LocalDate today = LocalDate.now();
            LocalDate weekStart = week.getStartDate();

            if (today.isBefore(weekStart)) {
                // 미션 시작 전: 금요일까지
                return weekStart.plusDays(4);
            } else {
                // 미션 시작 후: 월요일 당일만
                return weekStart;
            }
        }

        // 2주차부터: 해당 주차 금요일
        return week.getStartDate().plusDays(4);
    }

    // 태스크 수정
    public void updateContent(String newTitle) {
        // 수정 가능 체크
        if (!canEdit()) {
            throw new MissionException(
                    hasBeenEdited ? MissionErrorCode.TASK_ALREADY_EDITED : MissionErrorCode.NOT_EDITABLE
            );
        }
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new MissionException(MissionErrorCode.TASK_TITLE_REQUIRED);
        }
        this.title = newTitle.trim();

        this.hasBeenEdited = true;  // 수정 완료 표시
    }
}