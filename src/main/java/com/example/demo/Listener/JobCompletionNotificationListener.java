package com.example.demo.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;
@Component
//Spring がこのクラスを自動的に「Bean（＝インスタンス）」として管理できるようにします。classにつけるのがComponent,methodにつけるのがbean
public class JobCompletionNotificationListener implements JobExecutionListener {
	//このクラスは、Spring Batchのジョブ実行の「完了後（終了時）」の動作をカスタマイズするためのリスナー
	//jobでエラーが起こっていないか、完了後の実行処理を行うときに使われる
	//JobCompletionNotificationListener というクラスは、バッチジョブが完了したときに何か追加処理（今回はログ出力）を行うための「リスナー」である。
	 /*JobExecutionListener を実装し、
	かつSpringの管理するBean（今回の@Component）として登録しておくと、
	ジョブの開始直後・終了直後に「おまかせで」指定した処理が動いてくれる。*/
    //今回は終わったあとにログを残す処理を書いている
    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    //Logger を用意して、ジョブの状態や結果をログに出力します。
    @Override
    public void afterJob(JobExecution jobExecution) {
    	//afterJob メソッドは、ジョブの実行が終わった直後に呼ぶ
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
        	//jobがtだしく行われたかチェック
        //BatchStatus.COMPLETED は「ジョブが正常に完了した」状態です。
            log.info("正常に行われました");
         //成功ならinfoレベルのログを残します。
        } 
    }
}


