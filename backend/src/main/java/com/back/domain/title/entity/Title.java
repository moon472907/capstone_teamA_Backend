package com.back.domain.title.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Title extends BaseEntity {
    String content;
    String achieveRequire;


    public Title(String content , String achieveRequire) {
        this.content = content;
        this.achieveRequire = achieveRequire;
    }

}
