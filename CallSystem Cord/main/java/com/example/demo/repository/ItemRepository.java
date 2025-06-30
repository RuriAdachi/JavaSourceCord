package com.example.demo.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Item;

//Item エンティティ（商品などのデータ）を対象に、データベース操作（CRUD） を簡単に行えるようにしているクラス
//データベースの操作を自動的に簡単に行うための入り口を作っている 
@Repository
public interface ItemRepository extends CrudRepository<Item, Integer> {
	//CrudRepository：Spring Data JPAが提供する、基本的な「CRUD操作（Create/Read/Update/Delete）」を行うためのインターフェース
	//save()：データの保存（新規または更新）,findById()：IDを使って検索,findAll()：全件取得,deleteById()：IDで削除
	    boolean existsByItemName(String itemName);
	//指定した itemName（商品名）のデータが 存在するか をチェックする
		 public List<Item> findAll();
	}	



