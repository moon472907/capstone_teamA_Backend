package com.back.domain.member.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberStatistic;
import com.back.domain.member.repository.MemberStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberStatisticService {
    private final MemberStatisticRepository memberStatisticRepository;

    public MemberStatistic create(Member member) {
        return memberStatisticRepository.save(new MemberStatistic(member));
    }

    public void delete(Member member) {
        memberStatisticRepository.delete(member.getStatistic());
    }
}
