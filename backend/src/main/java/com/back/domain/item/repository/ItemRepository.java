package com.back.domain.item.repository;

import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {


    public List<Item> findByType(ItemType type);

}
