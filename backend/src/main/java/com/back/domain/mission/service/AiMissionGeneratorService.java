package com.back.domain.mission.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.dto.ai.AiMissionResult;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;


@Service
@RequiredArgsConstructor
// AI 목업 서비스
public class AiMissionGeneratorService {

    private final ChatClient chatClient;
    private final MemberRepository memberRepository;

    public AiMissionResult generateMission(String rawGoal, int weeks, Integer memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MissionException((MissionErrorCode.MEMBER_FORBIDDEN)));

        int age = Period.between(member.getBirth(), LocalDate.now()).getYears();
        String gender = member.getGender().name(); // "MALE", "FEMALE", "NONE"


        String promptText = """
            너는 미션 플래너야.
            사용자가 목표와 기간을 입력하면 반드시 아래 JSON 구조를 채워서 출력해라.
            설명은 하지 말고 JSON만 반환해라.
            무조건 한국어로만 생성해라
            그리고 제목은 간결하게 정리해서 반환해줘.
            그리고 나이나 성별을 고려할 수 있다면 고려해서 만들어줘.
            JSON 형식:
            {{
              "goal": "짧고 간결한 목표 문장",
              "category": "EXERCISE | HABIT | MENTAL | LEARNING | CUSTOM",
              "weeklyPlans": [
                {{
                  "weekNum": 1,
                  "title": "1주차: 주차별 목표",
                  "dailyTasks": [
                    {{ "dayNum": 1, "title": "Day1 활동" }},
                    {{ "dayNum": 2, "title": "Day2 활동" }}
                  ]
                }}
              ]
            }}

            사용자 입력:
            목표: %s
            기간: %d주
            나이 : %d세
            성별 : %s
            """.formatted(rawGoal, weeks, age, gender);

        Prompt prompt = new Prompt(promptText);

        return chatClient.prompt(prompt)
                .call()
                .entity(AiMissionResult.class);
    }
}