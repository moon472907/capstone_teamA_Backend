package com.back.domain.mission.scheduler;

import com.back.domain.mission.entity.Task;
import com.back.domain.mission.repository.TaskRepository;
import com.back.domain.mission.service.TaskAutoFailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAutoFailScheduler {

    private final TaskRepository taskRepository;
    private final TaskAutoFailService taskAutoFailService;

    //매일 자정 1분에 실행 / 어제 날짜의 미완료 Task를 자동으로 SKIPPED 처리
    @Scheduled(cron = "0 1 0 * * *")
    public void autoFailExpiredTasks() {
        log.info("자동 실패 처리 스케줄러 시작");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        int yesterdayDayOfWeek = yesterday.getDayOfWeek().getValue();

        log.info("처리 대상 날짜: {}, 요일: {}", yesterday, yesterdayDayOfWeek);

        int pageNumber = 0;
        int pageSize = 100;
        int totalProcessed = 0;

        while (true) {
            Slice<Task> taskSlice = taskRepository.findExpiredTasksSlice(
                    yesterday,
                    yesterdayDayOfWeek,
                    PageRequest.of(pageNumber, pageSize)
            );

            if (taskSlice.isEmpty()) {
                break;
            }
            int processed = taskAutoFailService.processExpiredTasks(
                    taskSlice.getContent(),
                    yesterday
            );
            totalProcessed += processed;

            log.info("{}번째 배치 처리 완료: {}건", pageNumber + 1, processed);

            if (!taskSlice.hasNext()) {
                break;
            }
            pageNumber++;
        }
        log.info("자동 실패 처리 완료: 총 {}건", totalProcessed);
    }


}