package com.example.demo.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
//Spring MVCのテストを行うためにMockMvcというモックのHTTPリクエスト環境を自動でセットアップします。
//実際にサーバーを起動しなくてもコントローラーを呼び出してテスト可能です。
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;
   // テスト対象のコントローラーをHTTPリクエスト風に操作できるオブジェクト。
    //これを使って、GETやPOSTリクエストを疑似的に送信し、レスポンスの状態や表示されるビュー、モデルの中身を検証します。

    @Test
    void testShowSignupForm() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("user"));
    }
///signup のGETリクエストを送信し、サインアップ画面が正常に表示されるかをテストします。
//status().isOk()：HTTPステータスコード200 OKを期待。
//view().name("signup")：返されたビュー名がsignupであることを検証。
//model().attributeExists("user")：モデルにuser属性が存在するか確認（通常はフォーム用の空オブジェクトなど）
  

    @Test
    void testRegisterUser_passwordsMatch() throws Exception {
        String uniqueUsername = "user_" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", uniqueUsername)
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())  
                .andExpect(redirectedUrl("/login"));
    }

//signup へPOSTリクエストを送信し、フォームで入力した内容で新規ユーザー登録を試みます。
//paramでフォームの各入力値を指定しています。
//usernameにはUUIDでランダムに生成した文字列を使い、テストを繰り返してもユーザー名の重複によるエラーを防いでいます。
//with(csrf())はSpring SecurityのCSRF対策のためのトークンを付与しています。
//status().is3xxRedirection()は、正常登録後にリダイレクト(3xx系)が発生することを期待しています。
//redirectedUrl("/login")はリダイレクト先が/loginであることを検証しています。


    @Test
    void testRegisterUser_passwordsDoNotMatch() throws Exception {
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "newuser")
                .param("password", "password123")
                .param("confirmPassword", "differentPassword")
                .param("role", "1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("ErrorMessage"))
                .andExpect(model().attribute("ErrorMessage", "パスワードが一致しません。"));
    }
    
    @Test
    void testRegisterUser_usernameAlreadyExists() throws Exception {
        String duplicateUsername = "existinguser_" + UUID.randomUUID().toString().substring(0, 5);
//テストが毎回成功するように、UUIDを使ってランダムな文字列で一意なユーザー名を生成しています。
 //  ①最初にユーザーを登録
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", duplicateUsername)
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        
///signup に対して、フォームの内容をPOSTで送信。
//csrf() トークンを付与（Spring SecurityがCSRF対策をしているため）。
//正常に登録されれば、ステータスコード 3xx（リダイレクト）となり、/login にリダイレクトされる。


//  ②同じユーザー名で再度登録 → エラーになるはず
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", duplicateUsername)
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("ErrorMessage"))
                .andExpect(model().attribute("ErrorMessage", "このユーザー名は既に使用されています。"));
    }
  /*
   * ステータスコードは 200 OK（画面は表示されるが、エラーを表示している状態）
再び signup.html を表示しているかチェック
model に ErrorMessage が存在しているかを確認
その内容が「このユーザー名は既に使用されています。」であるか検証
   */
    
    
//パスワードと確認パスワードが違う場合のテストです。
//status().isOk()を期待する理由は、パスワード不一致の場合は登録処理が失敗して再びサインアップ画面を同じURL(/signup)で表示し
 //HTTPステータスコードは200のままだからです。
//view().name("signup") で再びサインアップ画面が表示されているか確認。
//model().attributeExists("ErrorMessage") でエラーメッセージがセットされているかチェック。
//model().attribute("ErrorMessage", "パスワードが一致しません。") で期待したエラーメッセージがセットされているかを検証しています。
}


/*
isOk() (200 OK)
→ リクエストが正常に処理され、結果のHTMLやJSONがレスポンスボディに含まれている状態。
→ 例：フォーム画面を表示したり、エラーがあって再度フォーム画面を返したりするとき。

is3xxRedirection() (3xx リダイレクト)
→ リクエストは成功したが、レスポンスヘッダーにリダイレクト先のURLが含まれている状態。
→ 例：ユーザー登録が成功したあとにログイン画面など別ページへ飛ばすとき。

*/