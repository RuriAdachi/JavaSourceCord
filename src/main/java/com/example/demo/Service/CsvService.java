package com.example.demo.Service;

import java.io.Writer;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.CallLogs;
import com.example.demo.model.Csvhistories;
import com.example.demo.repository.CallLogsRepository;
import com.example.demo.repository.CsvhistoriesRepository;
import com.opencsv.CSVWriter;

@Service
public class CsvService {

    @Autowired
    private CallLogsRepository callLogsRepository;
    @Autowired
	private CsvhistoriesRepository csvHistoryRepository;

    public void createCsvFile(Writer writer, String yearMonth,Integer userId,Integer roleId) {
    //下記
    	//CSVWriterを呼び出している
    	//CSVWriter（OpenCSVライブラリ）は、CSVファイルに行を書き込むためのツールです。
    	//try-with-resources を使って、自動で csvWriter をクローズします
        List<CallLogs> callLog = callLogsRepository.findByYearMonthAndAdminConfirmed(yearMonth,true);
        //findByYearMonthAndAdminConfirmedは年月と管理者確認済みを同時に検索する為に作ったメソッド

        //CalllogsリポジトリーでCallLogを一つずつ呼び出している
        //CSVを作成するメソッド　通話日付、ファイル名、ログ作成日
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
        	//CSV書き出し文
            csvWriter.writeNext(new String[] { "通話日付","ファイル名", "通話ログ作成日時"});
			for (CallLogs c : callLog) {
                csvWriter.writeNext(new String[] {
                    String.valueOf(c.getCalldate()),
                    c.getFilename(),
                    String.valueOf(c.getCreatedAt()),
             //通話日付、ファイル名、作成日時を1行ずつCSVに出力しています。
                });
                //CSVを書き出すたびに、Csvhistories テーブルに履歴を保存します。
                //CSVファイルを生成するたびに、どの通話ログがCSVに書き込まれたのかを追跡するために使用
                //実際にかき出した各通話ログについてCSV生成履歴をデータベースに保存
                saveCsvHistory(LocalDate.now(), c.getCalldate(), userId,roleId);
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV書き込み中にエラーが発生しました。", e);
        }
  }

    
    //	書き出した各通話ログについてCSV生成履歴をデータベースに保存する処理の中身
    //CSVファイルに書き出された各通話ログごとに、履歴（ログ）を保存することで
    //「誰が」「いつ」「どの通話ログを」CSVに出力したのかを記録しておくための処理
        public void saveCsvHistory(LocalDate localDate, LocalDate callDate, Integer userId,Integer roleId) {
            Csvhistories history = new Csvhistories();
            history.setCreatedDate(LocalDate.now());
            //history.setCreatedDate(LocalDate.now());	CSV出力日を「今日の日付」にセット
            history.setCallDate(callDate);  // calldateを保存
            history.setUserId(userId);
            history.setRoleId(roleId);
            csvHistoryRepository.save(history);
        }
    }
/*| 引数          | 説明                       |
| ----------- | ------------------------ |
| `writer`    | CSVファイルへの出力ストリーム（ファイルなど） |
| `yearMonth` | 対象の年月（例："2025-05"）       |
| `userId`    | 誰がCSVを作ったかのユーザーID        |
| `roleId`    | 役割ID（一般/確認者/管理者など）       |
*/

    

