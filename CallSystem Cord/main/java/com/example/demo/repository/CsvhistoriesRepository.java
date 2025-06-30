package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.Csvhistories;

public interface CsvhistoriesRepository extends JpaRepository<Csvhistories, Integer> {
	//CsvHistoriesのUserIdとCallDateを同時に呼び出す
	//existsByUserIdAndCallDateBetweenは自動でクエリを生成し、ユーザーのCSV出力履歴を日付範囲で検索し、既存データの有無を確認する
	/*boolean existsByUserIdAndCallDateBetween(Integer userId, LocalDate startDate, LocalDate endDate);
	//callDate の年月だけを抽出し、それが指定された年月と一致するデータを探す*/
	
	@Query("SELECT c FROM Csvhistories c WHERE FUNCTION('DATE_FORMAT', c.callDate, '%Y-%m') = :yearMonth")
    List<Csvhistories> findByCallMonth(String yearMonth);
	//指定された年月（例："2025-05"）に該当する通話履歴（Csvhistories）をデータベースから取得するためのクエリ
	//

}

