package com.example.demo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.demo.Service.ItemService;
//Applicationクラスはアプリケーションの起動管理のみを担う
//CallsystemApplication クラスは Spring Boot アプリのエントリーポイント
@SpringBootApplication
@EnableBatchProcessing  
public class CallsystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CallsystemApplication.class, args);
    }

     
    @Bean
    // バッチ処理を実行するためのBeanで、 アプリ起動時にItemServiceのジョブを実行する為に作成
    public CommandLineRunner  runJobMethod(final ItemService itemService) {
    	//CommandLineRunnerはアプリが実行した直後にバッチや初期処理などを自動実行させたいときに使うインターフェース
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
         //run(String... args) メソッドがアプリ起動直後に一度だけ呼び出される。
                itemService.runJobMethod(); 
                // runIdを含めたJobParametersでバッチを起動
                //アプリケーション起動時に、このバッチ処理が一度だけ実行される
            }
        };
        
 //①アプリ起動時に CallsystemApplication の CommandLineRunner が呼ばれる。
 //②その中で ItemService.runJobMethod() が一度だけ実行される。したがって、起動時に1回限りバッチ処理が行われる
        
 //↓解説↓
 //CommandLineRunner は通常、実装クラスを作ってから使うものであるが、
 //ここでは「クラス名なしで」直接インスタンス化し、その場で run メソッドをオーバーライドして中身の処理を書いている。
 //public void run(String... args)…は無名クラス。Spring Bootアプリケーションが起動した直後に一度だけ自動で実行される 特別なメソッド
    }

}        
    
