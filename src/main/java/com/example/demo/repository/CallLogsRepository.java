package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.CallLogs;

public interface CallLogsRepository extends JpaRepository<CallLogs, Integer> {
	//指定された日付にcalldate一致するログ検索
    List<CallLogs> findByCalldate(LocalDate calldate);  
    
    //通話ログのIDを検索し、1件だけ取り出す
    Optional<CallLogs> findById(Integer id);  
    
    //SQLなどのDBで DATE_FORMAT(date, '%Y-%m') を使って年月（例：2025-05）単位でフィルタ
    //①年月だけ指定してログを取得する
    @Query("SELECT log FROM CallLogs log WHERE FUNCTION('DATE_FORMAT', log.calldate, '%Y-%m') = :yearMonth")
    List<CallLogs> findByYearMonth(@Param("yearMonth") String yearMonth);
    
    //②管理者によって「確認済み」かどうかでログを絞り込むメソッド
	List<CallLogs> findByAdminConfirmed(boolean adminConfirmed);
	
	//③最後に年月とadminConfirmed（管理者承認済か）で絞り込み。両方の条件に合致するものを絞り込んでいる
	@Query("SELECT log FROM CallLogs log WHERE log.adminConfirmed=:adminConfirmed AND FUNCTION('DATE_FORMAT', log.calldate, '%Y-%m') = :yearMonth")
	List<CallLogs> findByYearMonthAndAdminConfirmed(@Param("yearMonth")String yearMonth, boolean adminConfirmed);


}
