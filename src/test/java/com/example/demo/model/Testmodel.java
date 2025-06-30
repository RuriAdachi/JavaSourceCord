package com.example.demo.model;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.repository.CallLogsRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;
//実際のリポジトリとエンティティを使用して、
//本物のデータベースに保存する処理を行っているため、**「テスト用の実データを作成して登録する補助クラス」

@Component
public class Testmodel {
	


    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CallLogsRepository callLogsRepository;

    public Item createTestItem() {
        Item item = new Item();
        item.setItemName("テスト商品");
        item.setDescription("テスト用の商品説明");
        return itemRepository.save(item);
    }

    public User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("{noop}password");
        user.setRole(1);
        return userRepository.save(user);
    }

    public CallLogs createTestCallLog(Item item) {
        CallLogs log = new CallLogs();
        log.setCalldate(LocalDate.now());
        log.setFilename("test_call.mp4");
        log.setFilepath("uploads/test_call.mp4");
        log.setCreatedAt(LocalDate.now());
        log.setItem(item); 
        log.setStatus(1);
        log.setConfirmed(false);
        log.setAdminConfirmed(false);
        return callLogsRepository.save(log);
    }

}