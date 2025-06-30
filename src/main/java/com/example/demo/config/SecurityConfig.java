package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.Service.UserService;

@Configuration
@EnableWebSecurity
//@EnableWebSecurity：Spring Securityの設定を有効にするアノテーション
public class SecurityConfig {
	@Autowired
    private UserService userService;  
//Spring Securityはこれを使ってログイン時にIDとパスワードを検証

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	//filterchain はSpring Security がリクエストを処理する際に通す「セキュリティのチェックルールの集合
    	//HttpSecurityを使って、ログイン画面やアクセス制限などを設定
        http
            
            .authorizeHttpRequests(authorize -> authorize
           /*authorizeHttpRequests(...) メソッドは、リクエストに対するアクセス制御の設定を受け取るメソッド。
		authorize -> authorize... というラムダ式で、「authorize」という設定用のオブジェクトが渡され、
		その中で .requestMatchers(...) や .authenticated() などの処理を連鎖して記述している。*/
           
            	    .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/signup", "/errors").permitAll()
            //.permitAll()：ログインしていなくてもアクセスできるURL
            	    .requestMatchers("/call-logs", "/call-check").authenticated()
           //.authenticated()：ログイン必須のページ
            	    .anyRequest().authenticated()
           //.anyRequest().authenticated()：他のすべてのURLもログイン必須に
            	)

            .formLogin(form -> form
                .loginPage("/login") 
                // ログイン画面として使うパスを指定している
                .defaultSuccessUrl("/call-logs", true) 
                // ログイン成功後に /calllogへ強制遷移
                .permitAll()
                //ログインページは未ログインでも表示できるように
            )
            .logout(logout -> logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
                )
            //ログアウトすると /login?logout にリダイレクト
                .userDetailsService(userService);  
        //ログインの際に使うユーザー情報の取得先（UserService）をここで指定

            return http.build();
        //httpというのは、httpsecutrityという設定用のオブジェクト
        //ログインログアウトなど様々なセキュリティ設定を記述するための物
        //.buildはsecurityfilterchainという実行用のオブジェクトを作成
        //return でその SecurityFilterChain を**Springの中に登録（返却）**して、セキュリティが動くようになる
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
