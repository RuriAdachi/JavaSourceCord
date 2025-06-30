package com.example.demo.Service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {
	//UserDetailsService を実装することで、Spring Securityのログイン認証処理に組み込まれる

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ ログイン時に呼ばれるメソッド
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    //loadUserByUsername(String username) は、Spring Security が ログイン認証時に自動で呼び出すメソッド
    //ログイン画面でユーザー名を入力 →Spring Securityがこのメソッドを呼び出して →
    //DBにそのユーザーがいるかどうかをチェック →結果をもとにログインの可否を決定。
    //UserDetails：Spring Security が使う「ログインユーザー情報を持つ」インターフェース。
        User user = userRepository.findByUsername(username);
     //入力された username を使って、DBからユーザー情報を取り出します   

        if (user == null) {
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + username);
     //UsernameNotFoundException はSpring Securityに「このユーザーいません」と知らせる特別な例外
        }

        return new org.springframework.security.core.userdetails.User(
        // Spring Securityが要求する「認証に必要な情報の形式（UserDetails）」が備わっていない。
        //そのため、Springが理解できる形（org.springframework.security.core.userdetails.User）に変換して返している.
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            //usernameとpassword、そしてユーザーの権限（ロール）を一つだけ指定しています。この例では "ROLE_USER"
 
            //この「Spring SecurityのUserクラスに変換して返す」ログイン認証の内部処理は、
            //Spring Securityがログインユーザーを認証するために絶対に必要な情報を返している
        );
    }

    // ✅ 新規ユーザー登録処理
    @Transactional
    public User registerNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        /*user.getPassword() で生のパスワードを取り出す。
		passwordEncoder.encode(...) でパスワードをハッシュ化（セキュリティ対策）。
		userRepository.save(user) でデータベースに登録*/
        return userRepository.save(user);
        
    }

    // ✅ ユーザー検索
    //username（例：ログインID）で、ユーザー情報をデータベースから探して返す
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    // ✅ ユーザー存在確認
    //指定されたユーザー名が既にデータベースに存在しているかを true / false で返す
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    
}





