package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
//@Controller は、HTMLテンプレートを返すクラスであることをSpringに伝えるアノテーション

public class LoginController {

  @GetMapping("/login")//HTTPのGETリクエスト（＝URLを開いたとき）に対応。
 
  public String showLoginPage(Model model) {
        return "login";//login.htmlに対応
    }
/*/login というURLにアクセスがあったときにこのメソッドが呼び出される。
 * Model model は、画面（Thymeleafなど）にデータを渡すための入れ物。
今回は特にデータを渡さず、テンプレート名 "login" を返すだけ。*/
  
  @GetMapping("/error")
  public String showLoginErrorPage(Model model, @RequestParam(value = "error", required = false) String error) {
      if (error != null) {
          model.addAttribute("ErrorMessage", "*ユーザー名もしくはパスワードが一致しません");
      }
      return "login";//login.htmlに対応
      //errorページに行くのではなく、ログインページ上でエラーが出るように設定
  }

}
//URLが /error?error=true のような時です。（ ユ ーザーが /error?error=true にアクセスすると、@RequestParam(...) で、URLパラメータの error を受け取ります。
//@RequestParam("error") によって "true" という文字列が error に入ります。 if (error != null) が true になるので、 
//model.addAttribute(...) でエラーメッセージをログイン画面に表示させます。
/*/
処理の流れ：
error パラメータが存在していれば、
model.addAttribute(...) により "ErrorMessage" という名前でエラーメッセージを渡す。
login.html に戻りますが、今度は ErrorMessage が埋め込まれて表示される。
*/
