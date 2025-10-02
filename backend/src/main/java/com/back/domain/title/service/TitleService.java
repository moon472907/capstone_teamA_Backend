package com.back.domain.title.service;


import com.back.domain.title.dto.CreateTitleDto;
import com.back.domain.title.dto.TitleDto;
import com.back.domain.title.entity.Title;
import com.back.domain.title.repository.TitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TitleService {

    private final TitleRepository titleRepository;

    public TitleDto  createTitle( CreateTitleDto createTitleDto)
    {
        return new TitleDto(titleRepository.save(new Title(
                createTitleDto.content(),
                createTitleDto.achiveRequire()
        )));
    }

    public List<TitleDto> findAll()
    {
        return titleRepository.findAll().stream().map(TitleDto::new).toList();
    }

    public TitleDto findById(int id) {
        return new TitleDto(titleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Title not found with id: " + id)));
    }

    public TitleDto updateTitle(int id, TitleDto content) {
        Title title = titleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Title not found with id: " + id));
        title.setContent(content.contents());
        return new TitleDto(titleRepository.save(title));
    }


    public void deleteTitle(int id) {
        Title title = titleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Title not found with id: " + id));
        titleRepository.delete(title);
    }


    public int count() {
        return (int)titleRepository.count();
    }
}
