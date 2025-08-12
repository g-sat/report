package com.example.report.service;

import com.example.report.model.Item;
import com.example.report.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    public void initializeSampleData() {
        if (itemRepository.count() == 0) {
            itemRepository.save(new Item("Apple", 3, 1.50));
            itemRepository.save(new Item("Banana", 5, 0.80));
            itemRepository.save(new Item("Orange", 2, 1.20));
            itemRepository.save(new Item("Mango", 4, 2.30));
            itemRepository.save(new Item("Grapes", 6, 3.50));
            itemRepository.save(new Item("Pineapple", 1, 4.00));
        }
    }
}
