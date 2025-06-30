package com.example.demo.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.example.demo.model.Item;
import com.example.demo.repository.ItemRepository;

public class ExistsCheckProcessor implements ItemProcessor<Item, Item> {
    //Spring Batchの中で「ItemProcessor」として使われるクラスで、「すでにデータベースに存在しているかをチェックする処理」を担う
	//CSVファイルなどから大量のデータを登録する際に、すでに登録済みのデータ（重複）をスキップ
	
    private static final Logger log = (Logger) LoggerFactory.getLogger(ExistsCheckProcessor.class);
    //何か処理の記録を取りたいときに log.info(...)ログ出力する
    
    private final ItemRepository itemRepository;
    
    public ExistsCheckProcessor(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    //データベース参照できる
    //ExistsCheckProcessor クラスのオブジェクト（インスタンス）を生成する際に ItemRepository を外部から渡して、
    //クラスの中で使えるように設定している.
 
    @Override
    public Item process(Item item) throws Exception {
    	boolean exists = itemRepository.existsByItemName(item.getItemName()); 
    //itemRepository.existsByItemName(...) で、その商品名がデータベースに存在するかをチェック。
    //item.getItemName() でCSVから読み取った商品名を取得。
    	if (exists) {
    		log.info("そのデータは既に存在しております: {}", item.getItemName()); 
    		//•すでに存在する場合はログを出して null を返し、スキップ
    		return null;
    		//nullでItemProcessor で返した場合、そのデータ（アイテム）は後続の ItemWriter に渡されず、書き込み処理がスキップされる.
    		
    	} 
    	return item;
    		//	存在しない場合は item をそのまま返し、データベースに保存される。（次の処理へ進む）。
    	}
  }


/* private static final Logger log = (Logger) LoggerFactory.getLogger(ExistsCheckProcessor.class);
 * static final	一度だけ初期化され、変更されない共有の定数的なログ変数として使われます。
（全インスタンス間で共通、かつ値を変えない）
Logger	ログ出力用のインターフェース（SLF4Jが提供するもの）。この変数を通じてinfoやwarn、errorなどのログ出力を行います。
LoggerFactory.getLogger(ExistsCheckProcessor.class)	LoggerFactoryはSLF4Jのクラスで、
この ExistsCheckProcessor.class にひもづく Logger を生成します。
つまり このクラス専用のログ出力装置 を作っているということになります。

結果として何ができるのか？

log.info("処理を開始します");
log.warn("データが欠損しています");
log.error("エラーが発生しました", e);
これにより、実行中の状態やエラー、スキップされたデータなどを記録（ログ）として残すことが可能になります。
バッチ処理はバックグラウンドで行われるので、きちんと処理されたかを確認するため*/

