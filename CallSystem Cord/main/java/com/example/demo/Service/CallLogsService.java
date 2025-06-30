package com.example.demo.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.CallLogs;
import com.example.demo.repository.CallLogsRepository;

@Service
public class CallLogsService {
    @Autowired
    private CallLogsRepository callLogsRepository;
    

    public List<CallLogs> getCallLogsByDate(LocalDate calldate) {  
    	//•	引数 calldate に一致する通話ログだけを検索
        return callLogsRepository.findByCalldate(calldate);  
    }

    public List<CallLogs> getAllCallLogs() {
    	//データベースにあるすべての通話ログを取得
        return callLogsRepository.findAll();
    }

    @Transactional
    public void saveCallLog(CallLogs log) {
    	//CallLogs エンティティを保存
    	//Saveメソッドは新規データの挿入や既存データの更新などを行う
        callLogsRepository.save(log);
        System.out.println("Saving log: " + log);
    }

    
    public CallLogs getCallLogById(Integer id) {
        return callLogsRepository.findById(id).orElse(null);
        //引数として渡されたID (id) に基づいて通話ログを取得するメソッド
        //返り値はOptional型でデータがあればCallLogを返し、データがなければnullを返す
    }

	public List<CallLogs> findByYearMonth(String yearMonth) {
		return callLogsRepository.findByYearMonth(yearMonth);
		//年月（yearMonth）で通話ログを検索するメソッド
	}
    
}
