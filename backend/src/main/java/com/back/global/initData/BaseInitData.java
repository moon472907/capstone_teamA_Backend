package com.back.global.initData;

import com.back.domain.item.dto.CreateItemDto;
import com.back.domain.item.entity.ItemType;
import com.back.domain.item.service.ItemService;
import com.back.domain.level.entity.LevelXP;
import com.back.domain.level.repository.LevelXPRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.reward.entity.ContentType;
import com.back.domain.reward.entity.RewardContent;
import com.back.domain.reward.entity.RewardType;
import com.back.domain.reward.repository.RewardRepository;
import com.back.domain.reward.service.RewardService;
import com.back.domain.title.dto.CreateTitleDto;
import com.back.domain.title.service.TitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    private final MemberService memberService;
    private final ItemService itemService;
    private final LevelXPRepository levelXPRepository;
    private final TitleService titleService;
    private final RewardService rewardService;
    private final RewardRepository rewardRepository;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {

        return args -> {
            self.initAllData();
        };
    }

    @Transactional
    public void initAllData() {

    }





}
