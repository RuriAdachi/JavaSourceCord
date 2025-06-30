package com.example.demo.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Item;
import com.example.demo.repository.CallLogsRepository;
import com.example.demo.repository.ItemRepository;



//Spring Batchのジョブを実行するためのサービスクラス
@Service
@Transactional
//@Transactional: メソッド内の処理が1つのトランザクションでまとめて実行されるようにします。
//失敗したらロールバックされ、整合性が保たれます。
public class ItemService {
	private JobLauncher joblauncher;
	// Spring Batch のジョブを起動するためのバッチジョブの実行専用コンポーネント。
	private Job importItemJob;
	//実行するジョブ。Spring Batchで定義されている「Item登録ジョブ」です。
	private final ItemRepository itemRepository;
	
	private CallLogsRepository callLogsRepository;
	//ItemRepositoryは、データベースのItemテーブルに対してCRUD操作（作成・読み込み・更新・削除）のためのインターフェース
	//finalを付けることで、この変数は一度だけ初期化されて以降、変更されない

	public ItemService(JobLauncher joblauncher, Job importItemJob, ItemRepository itemRepository, CallLogsRepository callLogsRepository) {
        this.joblauncher = joblauncher;
        this.importItemJob = importItemJob;
        this.itemRepository = itemRepository;
        this.callLogsRepository = callLogsRepository;
    }
	
//CallSystemApplicationで起動したrunJobMethodを呼び出している
//ItemService に処理を委任することで、パラメータの生成などのロジックを一箇所に集約
@Transactional(propagation = Propagation.NOT_SUPPORTED) 
//@Transactional...→トランザクションを使わない設定。バッチ処理は独立して動くため、分離されている。
//通常のトランザクションが有効だと、ジョブ内部のステップのトランザクション制御と競合する可能性がある。
//外側からのトランザクションを無効にしておくことがベスト
  public void runJobMethod() throws Exception {
      String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
      //ジョブの識別に使うためのタイムスタンプ
      //DateTimeFormatterクラスは、オブジェクト生成時にnew演算子を使用するのではなく、ofPatternメソッドで日付パターンを指定して生成する必要がある
      //同じ日にジョブを何度も実行したいときに「すでに同じパラメータで実行されたジョブ」とみなされないように、LocalDateTimeで取得
      long dateTimeLong = Long.parseLong(dateTime);
      //Long.parseLong は、文字列（String）を数値の long 型に変換するメソッド
      JobParameters parameters = new JobParametersBuilder()
     //ジョブに渡すパラメータを組み立てるためのビルダー
    //Spring Batchでジョブ（バッチ処理）を実行するときに一緒に渡す「入力値（引数）」
   //ジョブに指示を与えるための情報 を渡す手段が ジョブパラメータ 
              .addLong("runId", dateTimeLong)
     //runIdという文字列パラメータに、dateTime の値をセット
     //Spring Batchは、同じパラメータでのジョブ再実行を拒否する仕組みがあるため、この runIdを変えることで再実行できるようにしている
              .toJobParameters();
     //ビルダーで設定した値を最終的に JobParameters オブジェクトとして生成
      //.toJobParameters() は「run() メソッドに渡すためのJobParametersを完成させるための最終ステップ
      joblauncher.run(importItemJob, parameters);
      //parametersからtoJobParametersとimportItemJobを受け取ってジョブ開始
      
  }

  // 商品一覧取得
  public List<Item> findAll() {
      return itemRepository.findAll();
 //全ての商品を取得して、List<Item>として返します。
  }

} 
  
  /*.addLong() や .addString() で「設定」を追加
.toJobParameters() で「完成」
このように、設定を少しずつ積み上げていく＝ビルダーの特徴。*/

/*joblauncher は、ジョブを実際に起動するエンジン（実行装置）。
importItemJob は、事前に設定された実行したいジョブ。
parameters は、ジョブに渡す引数（設定情報）。*/
   
