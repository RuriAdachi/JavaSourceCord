package com.example.demo.controller;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Service.CallLogsService;
import com.example.demo.Service.CsvService;
import com.example.demo.Service.UserService;
import com.example.demo.model.CallLogs;
import com.example.demo.model.Item;
import com.example.demo.model.User;
import com.example.demo.repository.CallLogsRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;


@AutoConfigureMockMvc
@Transactional
//@Rollback(false)
//	通常@Transactionalだとテスト後にロールバックされますが、
//これでロールバックしないようにしています。テスト中に実際にDBへ保存される。
@SpringBootTest
@ActiveProfiles("test")
//application-test.ymlなどのテスト用設定ファイルを使ってテストします。test_db.sqlに接続。

public class CallLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CallLogsRepository callLogsRepository;


    private Item testItem;
    
    private User testUser;
    
    
    @Autowired
    private CsvService csvService;
    
    @Autowired
    private CallLogsService callLogsService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    
    private User confirmUser;
    private User adminUser;
    private CallLogs testCallLog;
    private String testYearMonth;

    private String uuidUsername;
    
    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        // 1. テスト実行前にデータを全て削除して「まっさら」な状態にする
        callLogsRepository.deleteAll();
        userRepository.deleteAll();
        itemRepository.deleteAll();

        // 2. テスト商品
        testItem = new Item();
        testItem.setItemName("テスト商品");
        testItem.setDescription("テスト用");
        testItem = itemRepository.save(testItem);

     // 3. ユーザー作成（UUIDでusernameを生成）
        testUser = new User();
        testUser.setUsername(UUID.randomUUID().toString());
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole(1);
        testUser = userRepository.save(testUser);

        confirmUser = new User();
        confirmUser.setUsername(UUID.randomUUID().toString());
        confirmUser.setPassword(passwordEncoder.encode("password"));
        confirmUser.setRole(2);
        confirmUser = userRepository.save(confirmUser);

        adminUser = new User();
        adminUser.setUsername(UUID.randomUUID().toString());
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRole(3);
        adminUser = userRepository.save(adminUser);

     
        // 4. セキュリティコンテキスト設定
        //これは ログイン状態を模擬する処理 です。Spring Securityを使ってログイン中のユーザー情報を強制的に設定しています。
        //これによって、ログインしないと見られないページのテストも可能になります。
        UserDetails userDetails = userService.loadUserByUsername(testUser.getUsername());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        //UserService（UserDetailsServiceを実装したクラス）を使って、testUser のユーザー情報を UserDetails 型で取得しています。
        //UserDetails は、Spring Securityで認証情報を保持する標準的なインタフェース
        //認証済みのユーザーであることを示す Authentication オブジェクトを作成しています。
        //UsernamePasswordAuthenticationToken は、Spring Securityの標準的な認証トークン。
        //ここでは、ユーザー本体→ userDetails、password→ userDetails.getPassword()、権限→ userDetails.getAuthorities()を渡しています。
        //Spring Securityが認証ユーザーを保持しているスレッドローカルな SecurityContext に、今作成した Authentication を設定します。
        //これにより、テスト実行中のスレッドにおいて「このユーザーはログイン済み」と認識されます。
        //認証ユーザーであるかを確認するソースコードをコントローラ側で書いているので、それをテストでもできるように記載している。


        // 5. テスト通話ログ
	    testCallLog = new CallLogs();
	    testCallLog.setCalldate(LocalDate.now());
	    testCallLog.setFilename("test.mp4");
	    testCallLog.setFilepath("/uploads/test.mp4");
	    testCallLog.setCreatedAt(LocalDate.now());
	    testCallLog.setItem(testItem);
	    testCallLog.setStatus(0);
	    testCallLog.setConfirmed(false);
	    testCallLog.setAdminConfirmed(false);
	    callLogsRepository.save(testCallLog);



        // 6. 年月セット
	    //このフォーマットは、2025-05 のように 年月で検索する機能をテストするために使われます。
        testYearMonth = LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());
    }



    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    //テスト用のログインユーザー（testuser）を仮想的にログインさせています。
    //Spring Securityを使っている場合、これをつけないと認証エラーになります。
    void uploadCallLog_successful() throws Exception {
        // アップロードファイル名をユニークにする
        String uploadFileName = "uploaded_test.mp4";
        //モックファイルの作成
        MockMultipartFile file = new MockMultipartFile(
                "file", uploadFileName, MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy content".getBytes()
        );
        // Spring Framework の MockMultipartFile クラスを使って
        //、HTTPリクエストで送信されるファイル（Multipart File）を模倣するオブジェクトを作成する処理。
        //"file" というフィールド名は、コントローラの @RequestParam("file") MultipartFile file などと一致している必要があります。

        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .param("callDate", LocalDate.now().toString())
                        .param("itemId", String.valueOf(testItem.getId()))
                        .with(csrf()))  // CSRF対策があるなら必須
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/call-logs"));
        //multipart("/upload")：マルチパートリクエストを /upload に送ります。
        //.file(file)：さきほど作成したファイルを添付。
        //.param(...)：日付とitemId（商品）をフォームパラメータとして送信。
        //.with(csrf())：Spring SecurityのCSRF対策が有効な場合、トークンがないとリクエストが弾かれるため、テストにもCSRFトークンを追加しま
      //リダイレクトステータス（3xx）が返ることを確認。
        //[/call-logs] にリダイレクトされること（＝アップロード後、一覧画面に戻る想定）
        
        // DBに保存されているか検証
        CallLogs savedLog = callLogsRepository.findAll().stream()
                .filter(log -> uploadFileName.equals(log.getFilename()))
                .findFirst()
                .orElse(null);
        //保存されたログがnullでない（＝DBにちゃんと保存された）ことを確認。
        //関連付けた Item のIDが、送信時に指定した testItem と一致しているか検証。

        assertNotNull(savedLog, "アップロードした通話ログがDBに保存されていません");
        assertEquals(testItem.getId(), savedLog.getItem().getId(), "通話ログのitemIdが一致しません");
    }
    
    
    // callLogs一覧表示テスト
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void viewCallLogs_successful() throws Exception {
    	//setupメソッドで使用したtestCallLogを使用

        // 通話ログの一覧ページ（/call-logs）へGETリクエストを送信しています。
        mockMvc.perform(get("/call-logs"))
                .andExpect(status().isOk())
        //HTTP 200（成功）であることを確認します。
                .andExpect(view().name("call_logs"))
        //使用されるテンプレートが call_logs.html であることを確認します。
                .andExpect(model().attribute("callLogs", hasSize(greaterThanOrEqualTo(1))))
        //モデルに含まれる callLogs という属性が、最低でも1件以上あることを検証します。
                .andExpect(model().attribute("callLogs", hasItem(
                        allOf(
                        		hasProperty("filename", is(testCallLog.getFilename())),
                        		hasProperty("item", hasProperty("itemName", is(testItem.getItemName())))
                        )
 /*
 ☆hasItem(...)
これは Hamcrestマッチャー の一つで、リスト（またはコレクション）の中に「指定の条件を満たす要素が1つ以上含まれている」かどうかを判定します。
ここでは、callLogs のリストの中に条件を満たす通話ログがあるかをチェックしています。
☆allOf(...)
これは複数の条件をまとめて「すべて満たす」ことを意味します。
ここでは「filename」かつ「itemName 」という2つの条件を両方満たす要素があるかをチェックしています。

 ☆hasProperty("filename", is(testCallLog.getFilename()))
これは、リスト内のオブジェクト（ここでは CallLogs のインスタンス）がプロパティ（getterメソッド）getFilename() を持ち、
その値が testCallLog.getFilename() と一致するかどうかをチェックします。

☆hasProperty("item", hasProperty("itemName", is(testItem.getItemName())))
さらに、そのオブジェクトの getItem() が返す Item オブジェクトもチェック対象で、
Item オブジェクトの getItemName() が testItem.getItemName() と一致するかを確認しています。

*/                  )));
    }


    // call_check表示成功テスト
   
    @Test
    void viewCallCheck_successful() throws Exception {
        String yearMonth = LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());
//今日の日付から、「YYYY-MM」という形式の年月文字列を作成しています。
// 事前登録済みのtestUserのusernameを使う.すでにテスト用に作成・登録されているユーザーのユーザー名を取得しています。
        String username = testUser.getUsername();

        mockMvc.perform(get("/call_check")
//SpringのMockMvcを使って、HTTPのGETリクエストを /call_check に対して行っています。
                .param("callmonth", yearMonth)
//リクエストパラメータに「callmonth=YYYY-MM」という形で現在年月を付加しています。
                .with(user(username).roles("USER"))) 
// テスト中に認証済みユーザーとしてリクエストを送るための設定です。ここでは、username（testUserのユーザー名）を使い、役割（ロール）に "USER" を与えています。
            .andExpect(status().isOk())
            .andExpect(view().name("call_check"))
            .andExpect(model().attribute("callLogs", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(model().attribute("name", is(username)))
            .andExpect(model().attribute("role", is(testUser.getRole())));
/*.andExpect(status().isOk())
→ HTTPレスポンスのステータスが200 OKであること。

.andExpect(view().name("call_check"))
→ コントローラーが返すビュー名が "call_check" であること。

.andExpect(model().attribute("callLogs", hasSize(greaterThanOrEqualTo(1))))
→ モデルの "callLogs" というリスト属性があり、そのサイズが1以上であること。
→ 通話ログの一覧が1件以上渡されていることを意味します。

.andExpect(model().attribute("name", is(username)))
→ モデルの "name" という属性に、現在の認証ユーザーのユーザー名がセットされていること。

.andExpect(model().attribute("role", is(testUser.getRole())))
→ モデルの "role" という属性に、testUser の役割（ロール）がセットされていること*/
    }
 //

    @Test
  @WithMockUser(username = "testuser", roles = {"USER"}) 
  void testPlayCallLog_success() throws Exception {
      CallLogs callLog = new CallLogs();
      callLog.setFilename("playtest.mp4");
      callLog.setCalldate(LocalDate.now());
      callLog.setItem(testItem);
      callLog.setCreatedAt(LocalDate.now());
      callLog.setFilepath("/uploads/playtest.mp4");
      callLog.setStatus(0);
      callLogsRepository.save(callLog);
      //テスト用のCallLogsエンティティを新規作成し、状態はstatus = 0（未再生の状態）で保存します。
      //testItemはあらかじめ用意された関連商品のエンティティです。
      
      mockMvc.perform(post("/play")
    	        .param("logId", callLog.getId().toString())
    	        .param("callmonth", testYearMonth)
    	        .with(csrf())
    	        .with(user("testuser").roles("USER"))
    	)
      .andExpect(status().isFound()); 
      //CallLogController の @PostMapping("/play") メソッドでは、以下のように最終的に リダイレクト（302） を返しています
      //今回は２００を返すstatus().isOk()（= HTTP 200）はここでは使わない
      /*"logId"に先ほど保存した通話ログのIDを文字列で渡しています。
       * .with(user("testuser").roles("USER")) で認証済みユーザーとしてリクエストしています。
       .andExpect(status().isFound()) で、HTTPステータスが「302 Found」（リダイレクト）であることを確認しています。
→ これはコントローラーの@PostMapping("/play") メソッドが処理後にリダイレクトを返すためです。*/

      CallLogs updatedLog = callLogsRepository.findById(callLog.getId()).orElse(null);
      assert updatedLog != null;
      assert updatedLog.getStatus() == 1;
      /*database状態の確認
       * リクエスト後に、DBから同じ通話ログを再取得しています。
	updatedLogがnullでないこと（ログが存在していること）をチェックし、
	statusが1に更新されているかどうかを検証しています。*/
  }


    @Test
    public void testUpdateStatus_confirmRole_success() throws Exception {
        mockMvc.perform(post("/updateStatus")
                .with(csrf())
                .with(user(confirmUser.getUsername()).roles("CONFIRM")) // 動的にユーザー名・ロールを指定
                .param("logId", testCallLog.getId().toString())
                .param("role", "confirm")
                .param("callmonth", testYearMonth))
            .andExpect(status().is3xxRedirection());

        CallLogs updated = callLogsRepository.findById(testCallLog.getId()).orElseThrow();
        assert(updated.getConfirmed());
        /*.with(user(...).roles("CONFIRM"))：このリクエストを「確認者ユーザー」として送信。
         *.param("role", "confirm")：コントローラーがこの値を見て「確認者の更新」と判断。
         ステータスコードは 3xx リダイレクトであることを確認。
		最後にDBから該当ログを取得し、getConfirmed() が true に更新されていることを検証。*/
    }


    // 管理者ユーザーでadmin権限のステータス更新テスト
 
    @Test
    public void testUpdateStatus_adminRole_success() throws Exception {
        mockMvc.perform(post("/updateStatus")
                .with(csrf())
                .with(user(adminUser.getUsername()).roles("ADMIN")) // 動的にユーザー名・ロールを指定
                .param("logId", testCallLog.getId().toString())
                .param("role", "admin")
                .param("callmonth", testYearMonth))
            .andExpect(status().is3xxRedirection());

        CallLogs updated = callLogsRepository.findById(testCallLog.getId()).orElseThrow();
        assert(updated.getAdminConfirmed());
        assertTrue(updated.getAdminConfirmed(), "管理者承認がtrueになることを期待します");

    }
    /*
     * .with(user(...).roles("ADMIN"))：このリクエストを「管理者ユーザー」として送信。
     * .param("role", "admin")：コントローラーがこの値を見て「管理者承認の更新」と判断。
	ステータスコードは 3xx リダイレクトであることを確認。
	assertTrue(...) を使って、adminConfirmed フィールドが true に変わったことを明確に検
     */

    // 通話ログが存在する場合、正しく表示されるテスト
    @Test
    @WithMockUser(username = "testuser")
    public void testViewCallLog_success() throws Exception {
        mockMvc.perform(get("/callLogs/" + testCallLog.getId()))
        //mockMvc.perform(...)：MockMvcを使って擬似的にHTTPリクエストを送信します。
        //get("/callLogs/{id}")：指定IDの通話ログ詳細ページにGETリクエストを送っています。
            .andExpect(status().isOk())
                .andExpect(view().name("call_check"))  
                //HTTPステータスが200 OKであることを確認します。
                .andExpect(model().attributeExists("callLog"))
                //modelにcallLogがあるか
                .andExpect(model().attribute("callLog", hasProperty("id", is(testCallLog.getId()))))
                //callLog.id が期待されるIDと一致しているか確認。
                .andExpect(model().attribute("callLog", hasProperty("filename", is(testCallLog.getFilename()))))
                //testのファイル名と一致しているか
                .andExpect(model().attribute("callLog", hasProperty("item", hasProperty("itemName", is(testItem.getItemName())))));
        //callLog に関連付けられた商品（item）の itemName が、期待される商品名と一致しているかを確認しています。
    }

    @Test
    @WithMockUser(username = "testuser")
    //@Test：JUnitのテストであることを示します。
    //@WithMockUser：Spring Securityの認証をモックし、「testuser」というユーザーでログインしている状態を再現します。
    public void testGenerateCsv() throws Exception {
        String yearMonth = "2025-05";
//
        mockMvc.perform(get("/exportCsv")
                .param("callmonth", yearMonth))
        //mockMvc.perform(...)：擬似的なHTTPリクエストを送信します。
        //get("/exportCsv")：CSV出力用のエンドポイントにアクセス。
        //.param("callmonth", yearMonth)：リクエストパラメータとして callmonth=2025-05 を送信しています。
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
        //「Content-Type」とは、HTTPレスポンスやリクエストにおいて「中身の種類（＝データの形式）を示すヘッダー情報」で
        //レスポンスがCSVであることを示す Content-Type: text/csv ヘッダーが含まれているか確認。
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=calllog_")));
        //Content-Disposition: attachment; filename=calllog_2025-05.csv のような形式でダウンロード用のファイル名が設定されているかを確認します。
        //containsString("attachment; filename=calllog_") によって、少なくとも "calllog_" という名前でCSVが生成されているかを部分一致でチェックしています。
    }
   
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // テスト後にコンテキストをリセット
    public class BatchJobTest {

        @Autowired
        private JobLauncher jobLauncher;

        @Autowired
        private Job importItemJob;

        @Autowired
        private ItemRepository itemRepository;

        @Test
        public void testImportItemJob() throws Exception {
            // バッチ処理を起動
            JobExecution jobExecution = jobLauncher.run(importItemJob, new JobParameters());

            // 正常終了しているか確認
            assertThat(jobExecution.getStatus().isUnsuccessful()).isFalse();

            // データベースに正しくデータが保存されたか確認
            List<Item> itemList = itemRepository.findAll();

            // items.csv の内容に応じて、想定される件数を確
            assertThat(itemList.size()).isEqualTo(40); 
            // ← CSVの行数に合わせて調整

            // 任意：データの内容も確認
            assertThat(itemList.get(0).getItemName()).isEqualTo("android1"); 
            // CSVのヘッダーをのぞいた1行目のitemName書かれている名前に合わせる
            
        }
    }
}/*assert（Javaの標準）	シンプルな真偽検査	条件がfalseの場合にエラーになる	assert x == 10;
assertTrue（JUnit）	条件がtrueか確認	trueでないとテスト失敗	assertTrue(x > 0);
assertThat（JUnit + Hamcrest）	より柔軟・可読性高い検査	直感的な記述で細かい条件をテスト可能	assertThat(x, is(10));

☆assertTrueの例
int age = 20;
assertTrue(age >= 18);  // 成功（true）
assertTrue(age < 18);   // 失敗（false → テスト失敗）

☆assertThatの例
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

String name = "Nana";
assertThat(name, is("Nana"));              // 成功
assertThat(name, not("Shinobu"));          // 成功
assertThat(name.length(), greaterThan(2)); // 成功


*/
