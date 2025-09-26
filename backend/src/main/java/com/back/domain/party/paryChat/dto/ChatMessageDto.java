package com.back.domain.party.paryChat.dto;

import com.back.domain.party.paryChat.entity.ChatMessage;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageDto {
    private Integer id;
    private String senderEmail;
    @Size(max = 1000, message = "메시지 내용은 1000자를 초과할 수 없습니다.")
    private String content;
    private Integer partyId;

    public ChatMessageDto(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.partyId = chatMessage.getParty().getId();
        this.senderEmail = chatMessage.getSender().getEmail();
        this.content = chatMessage.getContent();
    }
}