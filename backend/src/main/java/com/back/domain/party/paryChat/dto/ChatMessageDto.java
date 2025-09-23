package com.back.domain.party.paryChat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageDto {
    private String senderEmail;
    private String content;
    private Integer partyId;
}