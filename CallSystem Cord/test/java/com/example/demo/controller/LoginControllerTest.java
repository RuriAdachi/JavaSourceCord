package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;
    //MockMvc は、Spring MVCのコントローラーを起動せずにWebリクエストを模倣できるテストツール
    //@SpringBootTest→ Spring Bootアプリ全体の文脈（コンテキスト）を使った統合テスト。
    //@AutoConfigureMockMvc→ MockMvc を自動で設定＆インジェクションできるようにするアノテーション

    @Test
    void testShowLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("ErrorMessage"));
    }
    /*.perform(get("/login")): 「/login に GET リクエストを送る」ことを意味します。
.andExpect(status().isOk()): HTTPステータスが 200 OK（＝正常）であることを期待。
.andExpect(view().name("login")): 返されるHTMLテンプレート名が "login" であること。
.andExpect(model().attributeDoesNotExist("ErrorMessage")): エラーメッセージが 表示されていないことを確認
'ErrorMessage' という名前のデータがモデルに存在しない状態です。
具体的には、ログイン画面を表示するときに、
model.addAttribute("ErrorMessage", "何かのメッセージ");
のように ErrorMessage をセットしていなければ、「含まれていない」と判断されます。*/
    
   
    @WithMockUser // 認証済みのユーザーとしてテスト実行
    @Test
    void testShowLoginErrorPage_withErrorParam() throws Exception {
        mockMvc.perform(get("/error").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("ErrorMessage"))
                .andExpect(model().attribute("ErrorMessage", "*ユーザー名もしくはパスワードが一致しません"));
    }
    
    /*GET /error?error=true にアクセスしたとき、
.param("error", "true")：クエリパラメータ error=true を付けてアクセス。
.attributeExists("ErrorMessage")：エラーメッセージがちゃんとモデルに渡されている。
.attribute("ErrorMessage", "*ユーザー名もしくはパスワードが一致しません")：メッセージの内容が合っているか確認。
error_pageではなく、ログイン画面内の表示されるか確認*/

    @WithMockUser
    @Test
    void testShowLoginErrorPage_withoutErrorParam() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("ErrorMessage"));
    }
    
    /*GET /error（エラーパラメータ無し）にアクセスしたとき、
?error=true を付けないで /error にアクセスした場合は、
エラーメッセージが表示されないことを確認。
.attributeDoesNotExist("ErrorMessage")：メッセージが 表示されないことを期待しています。*/


    @WithMockUser
    @Test
    void testLogout() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().isOk())
                .andExpect(view().name("logout"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", "ログアウトしました"));
    }
   /*GET /logout にアクセスしたとき、ステータスが200,logout ビューを返す
message モデル属性があり、「ログアウトしました」というメッセージである
.view().name("logout")：HTMLテンプレート logout.html が表示される。
.attribute("message", "ログアウトしました")：メッセージの内容を検証。
*/
}

/*mockMVC Perform
「HTTPリクエストを実行する」
つまり「URLにアクセスする」動作をシミュレートするためのメソッドである。
「/login に GET リクエストを送る」＝「ログイン画面を表示する動作を試してみる」
という意味です。実際のサーバを動かすわけではなく、テスト用に仮想的に動かします。*/

/*andExceptは：
「その結果がこうなっていることを期待する」
（expect＝「期待する」の意味）
は、「HTTPステータスコードが 200 OK であることを確認する」という意味で、
.andExpect(view().name("login"))は、
「表示されるビュー名（HTMLファイル名）が 'login' であることを確認する」ということです。*/




/*WIthMocker
 *これは**仮想的に「ログイン済みのユーザーとしてテストする」**ためのアノテーションです。

Spring Securityが有効になっている場合、ログインしていないとアクセスできないコントローラもあります。

このアノテーションで「ログインしている状態」をシミュレートできます。*/


