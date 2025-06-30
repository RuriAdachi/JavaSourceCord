
package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.Service.UserService;
import com.example.demo.model.User;

@Controller
public class SignupController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ユーザーが /signup にアクセスした時の処理です。
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        //空の User オブジェクトをHTMLに渡して、Thymeleafでフォームに使えるようにします。
        //これはHTMLに「User オブジェクトの username に入力された値を入れてね」と指示を出している
        return "signup"; 
        // signup.html へ遷移
    }

   
 // 新規登録を処理
    @PostMapping("/signup")
    public String registerUser(@Validated User user, Model model) {
        // パスワード確認
        if (user.getPassword() != null && !user.getPassword().equals(user.getConfirmPassword())) {
        	//パスワードと確認用パスワードが一致するか確認。一致しなければエラーメッセージを出す
            model.addAttribute("ErrorMessage", "パスワードが一致しません。");
            return "signup";
        }

        // ユーザー名重複チェック（emailでも可）
        if (userService.existsByUsername(user.getUsername())) {
        	//UserService→UserRepositoryで、データベースを参照し、既に使われた名前でないか確認
            model.addAttribute("ErrorMessage", "このユーザー名は既に使用されています。");
            return "signup";
        }

        // ユーザー登録
        userService.registerNewUser(user);

        return "redirect:/login";
    }

}
// パスワード確認が一致しているか確認/
//•	ユーザーがフォームに入力して「登録」ボタンを押すと呼ばれる。
