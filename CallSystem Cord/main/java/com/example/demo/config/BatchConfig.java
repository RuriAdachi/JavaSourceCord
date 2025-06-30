package com.example.demo.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.Listener.JobCompletionNotificationListener;
import com.example.demo.Processor.ExistsCheckProcessor;
import com.example.demo.model.Item;
import com.example.demo.repository.ItemRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig {
	
	@Autowired
	private ItemRepository itemRepository;

	
	@Bean
	//Spring にこのメソッドで作成されたオブジェクト（FlatFileItemReader<Item>）を DI対象の Bean として登録
    public FlatFileItemReader<Item> csvItemReader() {
		//FlatFileItemreader() はSpring Batch の中に用意されている ファイル読み取り専用のクラス(決まっている名前）。
		//CSVなどの 「1行ずつデータが並んでいる形式」のファイルを処理するために使う。
		//1行 → Javaオブジェクトに変換するためのリーダークラス
    	return new FlatFileItemReaderBuilder<Item>()
    		.name("csvItemReader")
    		//items.csv というファイルを src/main/resourcesから読み込む指定
    		//リーダーに名前をつけている。これはログやエラーハンドリング時に識別するためである
    		//.resource(new ClassPathResource("items.csv")) 
    	.resource(new ClassPathResource("items2.csv")) 
    		//相対パスで対象CSVファイルを指定
    		//ClassPathResource を使うことで、リソースフォルダに置かれたCSVを読み込むことができる。
    		.encoding("Shift_JIS") 
    		// 文字コード（エンコーディング）を指定
    		.linesToSkip(1) 
    		// 先頭行 ヘッダーをスキップ
    		.delimited() 
    		//「区切り付きファイル（CSVなど）」であることを指定
    		//「区切り文字（デリミタ）で構成されたテキストファイル」（つまりCSV）の読み込みであることを指定している。
    		//「,（カンマ）」がデリミタ（delimiter）
    		.names(new String[] {"Id","ItemName","Description"})
    		//CSVの列名とJavaのフィールド名を対応付け
    		//これは「1行のデータを Item クラスに変換してくれる」仕組み
    		//SpringがCSVの各カラムを、Item の各フィールドに自動でセットする
    		//ここで指定された名前は Item クラスのプロパティ名 と一致している必要がある。
    		.fieldSetMapper(new BeanWrapperFieldSetMapper<Item>() {{ 
    		//.fieldSetMapperはFlatFileItemReader の設定のひとつで、読み込んだ1行を Javaオブジェクトに変換する方法を指定する。
    		//読み取った1行を Item クラスに変換
    		//BeanWrapperFieldSetMapper は、リフレクションを使って自動でプロパティに値をセットしてくれる。
				setTargetType(Item.class);	
				//setTargetType(Item.class) で、変換対象のクラスを指定している。
				//「CSVの1行データを Item クラスに変換する」ための設定。
				//たとえば、CSVの列名と Item クラスのフィールド名が一致していれば、自動的にデータをセットしてくれます。
			}})
    		//完成した ItemReader<Item> オブジェクトを得ている
    		.build();
	}
	
	@Bean
	public ExistsCheckProcessor existsCheckProcessor() {
        return new ExistsCheckProcessor(itemRepository);
        //CSVからItemを読み込み、すでに存在するデータを除いて登録するバッチ処理
        //ExistsCheckProcessor という ItemProcessorの実装クラスのインスタンスを作って、Springの管理下に @Bean として登録しています。
        //itemRepository を引数に渡しているのは、すでにDBにあるかどうかを確認するため。
        //ExistsCheckProcessor は Spring BatchのItemProcessorとして作られた再利用可能な部品のため、別パッケージで作成済み。
    }
	

	@Bean
	//バッチ処理で読み込んだ Item データを、Spring Data JPA を使ってデータベースに保存するためのクラス
	public ItemWriter<Item> writer() {
		// Spring Data JPA を使ってItemをDBに保存するWriterを作成する
		//バッチ処理で読み込んだ Item データをデータベースに保存するための Writer を作成して、Spring の Bean として登録している処理
		//戻り値の型が ItemWriter<Item> となっており、これはSpring Batchで「処理対象のアイテムを書き出す役割」を担うインターフェース
		RepositoryItemWriter<Item> writer = new RepositoryItemWriter<>(); 
		//RepositoryItemWriter（Spring Data JPA） を使って、リポジトリ経由でデータベースに保存する Writer のインスタンスを作成している。
		//Spring Batchの中で「JPAリポジトリを使ってデータベースに書き込みたい」場合に使う定番のWriterクラス。
		//ItemWriter インターフェースを実装しているため、バッチの書き込み部分にそのまま組み込める。
		writer.setRepository(itemRepository); 
		//RepositoryItemWriterにセットして、実際の書き込み処理に使うように指定
		//書き込みに使用する JPAリポジトリ（ItemRepository）を指定している。
		//このリポジトリを通してデータベースへの保存処理が行われる
		writer.setMethodName("save"); 
		// saveメソッドを使って保存
		//itemsRepositoryのどのメソッドを呼び出してデータを保存するかを指定。
        //ここではsaveメソッドを使い、バッチ処理で渡されたItemを1件ずつリポジトリのsaveメソッドでデータベースに
		return writer;
		//RepositoryItemWriterのインスタンスを返し、SpringのBeanとして登録
	}
	
	@Bean
	//Job（ジョブ）：バッチ処理全体の流れを定義するもの
	public Job importItemJob(JobRepository jobRepository, Step step, JobCompletionNotificationListener listener) {
		//JobRepository：ジョブの実行状態などを管理・保存するための仕組み。	
		//Step：ジョブの中で実行する具体的な処理単位。
		//JobCompletionNotificationListener：ジョブ完了時に特別な処理を実行するためのリスナー。ジョブが 正常に終了したかどうか を監視します。
		return new JobBuilder("importItemJob", jobRepository) 
			//これはジョブの名前を "importItemJob" と指定して、ジョブを作るためのビルダーを用意
			//"importItemJob" はジョブの名前で、ジョブ管理の識別子として使う。
			//jobRepository はジョブの状態管理に必要。
			.incrementer(new RunIdIncrementer()) 
			//毎回異なるIDでジョブ実行（再実行可能に）
			//Spring Batch は 同じJob名・同じパラメータでジョブを再実行しようとすると「すでに完了している」とみなして実行しない。
			//RunIdIncrementer は、毎回異なる run.id を付与することで再実行を可能する
			.listener(listener) 
			//ジョブ完了時のリスナーを設定
			//これはジョブ実行のライフサイクルに合わせて処理を挟むためのもの。
			//JobCompletionNotificationListener が渡されており
			//ジョブの成功や失敗を検知し、通知をしたりログを書いたり、別の処理を行うのに使う。
			//このジョブにこのリスナーをセットしていることで、ジョブ終了時の特別な処理が実現されます。
			.start(step) 
			//実行するステップを設定（step() メソッドを呼び出してバッチ処理が行われるように指定）
			.build();
		//☆設定した内容をもとに ジョブオブジェクトを完成 させて返す
		//返す先はSpringがこのメソッドを呼び出して、戻り値である Job オブジェクトを受け取ります。
		//その受け取った Job オブジェクトを 「importItemJob」という名前のBeanとして管理・保持 します。
		//private final Job importItemJob;等と指定して呼び出されたときに使われる
	}
	
	@Bean
	//バッチ処理は「ジョブ（Job）」の中に複数の「ステップ（Step）」を持ちます。
	//ステップは、「読み込み（Reader）」→「加工（Processor）」→「書き込み（Writer）」の流れを持つ一連の処理単位
	public Step step(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<Item> reader,
            ItemProcessor<Item, Item> processor,
            ItemWriter<Item> writer) {
	  return new StepBuilder("step", jobRepository) 
	//新しいステップを作成。ステップの名前を "step" として設定
	    .<Item, Item> chunk(10, transactionManager) 
	   // ここでは10件ずつデータをまとめて処理し、まとめてトランザクション管理される。
	    //つまり10件分処理してから一度データベースに書き込みます。効率的かつ堅牢な処理が可能。
	    .reader(reader) 
	    //CSVファイルを読み込む。例えばCSVファイルからItemオブジェクトを1件ずつ読み込む役割。
	    .processor(processor) 
	    //1件ずつデータを加工。重複チェックを行い、不要なものは除外（nullを返す）する処理。
	    .writer(writer) 
	    //加工されたデータ（重複を除いたItem）をDBに保存
	    .build();
	  //の step() メソッドは、Springの @Bean 注釈がついている。
	  //Springのコンテナ（アプリケーションコンテキスト）が起動するときにこのメソッドが呼ばれ、
	  //返された Step オブジェクトは Springの管理するBeanとして登録されます。
	}
	/*Step step---
	 * JobRepository jobRepository
ジョブの状態を管理するリポジトリ。

PlatformTransactionManager transactionManager
トランザクション（処理のまとまり）の管理者。

FlatFileItemReader<Item> reader
ファイルからItemを読み込む処理をするオブジェクト。

ItemProcessor<Item, Item> processor
読み込んだItemを変換・加工する処理を行うオブジェクト。

ItemWriter<Item> writer
加工済みのItemを保存する処理を行うオブジェクト

*/
	 /*
	/*現在のバッチ処理の全体フロー
	CSVファイルの読み込み（Reader）
	BatchConfig の中で csvItemReader() が定義され、CSVファイル（items.csv）を Item オブジェクトに読み込みます。
	
	重複チェックの処理（Processor）
	existsCheckProcessor() で ExistsCheckProcessor を使い、既に同じ itemName のデータがDBにあるかをチェックし、存在する場合は null を返してスキップ。
	
	データベースへの保存（Writer）
	writer() で RepositoryItemWriter<Item> を使い、itemRepository.save() によりデータベースに保存。
	
	ジョブ完了時のログ出力（Listener）
	JobCompletionNotificationListener が afterJob() メソッドでログを出力します（成功・失敗の確認など）。*/
	
}
