package com.back.domain.item.service;


import com.back.domain.item.dto.ItemDto;
import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;
import com.back.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemDto createItem(ItemDto createItemDto)
    {
        Item item = new Item(
                createItemDto.itemType(),
                createItemDto.name(),
                createItemDto.img()
        );
        return new ItemDto(itemRepository.save(item));
    }
     public List<ItemDto>  ReadAllItem()
     {
            return itemRepository.findAll().stream().map(ItemDto::new).toList();
     }

     public ItemDto ReadItemById(int id)
     {
         return new ItemDto(itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id)));
     }

    public List<ItemDto> ReadItemByItemType(ItemType type)
    {
        //ItemType itemType =ItemType.valueOf(type.toUpperCase());
        //System.out.println(type);
        return itemRepository.findByType(type).stream().map(ItemDto::new).toList();
    }


    public ItemDto UpdateItem(int id, ItemDto itemDto)
    {
        Item item = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        item.setName(itemDto.name());
        item.setImg(itemDto.img());
        item.setType(itemDto.itemType());
        itemRepository.save(item);
        return new ItemDto(item);
    }

    public void DeleteItem(int id)
    {
        Item item = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        itemRepository.delete(item);
    }

}
