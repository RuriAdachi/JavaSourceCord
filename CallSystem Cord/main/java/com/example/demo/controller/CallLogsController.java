package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import com.example.demo.Service.CallLogsService;
import com.example.demo.Service.CsvService;
import com.example.demo.Service.ItemService;
import com.example.demo.model.CallLogs;
import com.example.demo.model.Item;
import com.example.demo.model.User;
import com.example.demo.repository.CsvhistoriesRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class CallLogsController {
	

	//アップロード先のディレクトリパス（application.propertiesから取得）
	@Value("${upload.dir}")
 	private String uploadDir1;
    @Autowired
    //通話ログ(service class)
    private CallLogsService callLogsService;
    //ユーザー情報取得	(Repository class)
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CsvhistoriesRepository csvHistoriesRepository;
    @Autowired
    private ItemService itemService;
    
	
    //登録済みの通話ログ（CallLogs）と、商品一覧（Item）を画面に表示するための処理です。
    //DBから通話ログ一覧（callLogs）と商品一覧（items）を取得し、Modelにセットして、
    //call_logs.html というビューに渡して表示させます。
    @GetMapping("/call-logs")
    public String viewCallLogs(Model model) {
    	//過去のアップロードデータ（DB）を表示する
        List<CallLogs> callLogs = callLogsService.getAllCallLogs();
        //画面に値を渡す
        model.addAttribute("callLogs", callLogs);
        
     // 商品一覧を取得☆Itemserviceで取得したデータを一覧で表示☆
        List<Item> itemsList = itemService.findAll();
        //Itemテーブルから商品一覧を取得し、フォームに表示する
     //通話ログ画面（call_logs.html）に商品一覧を表示するため(モデルに渡すためとも言う）
        model.addAttribute("items", itemsList);
     //itemListをビューにはitemsで渡す
        return "call_logs";
    }
   

    /** ファイルや通話ログ、アイテムなどを選択し、データベースに保存するメソッド **/
    //重要　フォームからアップロードされたファイル、通話日時、そして商品ID（選択されたもの）を受け取る。
    @PostMapping("/upload")
    public String uploadCallLog(@RequestParam("file") MultipartFile file,
            @RequestParam("callDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate callDate, @RequestParam("itemId") Integer itemId,
            Model model,RedirectAttributes redirectAttributes)  
  //file→アップロードされた通話ログ
  //callDate→ユーザーが指定した通話日
  //itemID→セレクトボックスで選択した商品ID
  //model→Viewなどにメッセージなどを渡すオブジェクト（ここでは）
    {
    	//CallLogにitem?idでカラム追加
        if (file.isEmpty()) {
            model.addAttribute("message", "アップロードされたファイルが空です。");
            //このmessageはerror_pageのmessageとつながっている
            return "error_page";
        }

        try {
            // アップロードディレクトリ作成
            File uploadDirectory = new File(uploadDir1);
            //ファイルが無ければuploadDir1というファイルを作成
            if (!uploadDirectory.exists()) {
            	//uploadが存在しないときにmkdirs()メソッドでディレクトリを作成
                uploadDirectory.mkdirs();
            }
            //ファイル名の生成
            //元の名前を取得
            String originalFilename = file.getOriginalFilename();
            //ファイル名にまれる特殊文字やスペースをアンダースコアに置き換える
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            //UUIDによって重複することのない独自の名前を付けた新しいファイル名を作成
            String storedFilename = UUID.randomUUID() + "_" + safeFilename;
            //保存先のファイルパスを決定
            String filePath = uploadDir1 + File.separator + storedFilename;
            // フ実際にをローカルに保存
            file.transferTo(new File(filePath));

            // 新しいCallLogsオブジェクトを作成し、通話ログエンティティ生成する。形式指定するだけ
            //通話ログエンティティ（CallLogs）に必要な情報をすべてセット
            CallLogs log = new CallLogs();
            log.setCalldate(callDate);
            log.setFilename(safeFilename);
            log.setFilepath(filePath);
            log.setStatus(0);
            log.setCreatedAt(LocalDate.now());
            
         // アップロードされた通話ログに選択された商品を紐づけて、一緒にデータベースに保存するため
            List<Item> items = itemService.findAll();
         //itemService.findAll() は、データベースからすべての Item（商品）を取得するメソッド
          //選択された商品IDと一致する Item オブジェクトを探し、アップロードされる CallLogs に紐づけるため。
         //結果は List<Item> として items に入る
            for (Item item : items) {
          //for 文はこの一覧から1件ずつ順番に商品を取り出し、変数 item に代入しています。
          //1件ずつループして該当IDの商品を探す
          //この item は「Itemエンティティクラスのインスタンス（オブジェクト）」です。
                if (item.getId().equals(itemId)) {
            //データベースに存在する商品一覧のID（item.getId()）と
            //商品品選択画面（セレクトボックスなど）で選んだ商品ID（itemId）を照合して、一致する商品を見つける
             //itemという変数からidをゲットしている
             //このitemIDは事前にCallLogモデルクラスで定義している
                    log.setItem(item);
                    break;
             //条件が一致した商品を log.setItem(item) で通話ログ（CallLogs）に紐づけます。
            // 見つけたらbreak でループを終了（目的の商品が見つかったのでそれ以上探さない
                }
            }
            
            callLogsService.saveCallLog(log);
            //上で作成したものをもとにcallLogsServiceを使って、作成したCallLogsオブジェクトをデータベースに保存します。
            
            // アップロード成功時
            redirectAttributes.addFlashAttribute("message", "ファイルが正常にアップロードされました！");
          

            //upload 成功
            //(log（通話ログオブジェクト）はデータベースに保存されるだけで、model.addAttribute(...) で ビューに渡す処理はしていない)
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message", "ファイル保存中にエラーが発生しました: " + e.getMessage());
            return "error_page";
            //保存中にエラーが出たら、エラーメッセージを出してerror_pageへ遷移。
        }

        return "redirect:/call-logs";
    }
/*|
| `th:each="item : ${items}"`  | `items`リスト（Controllerから渡された商品一覧）を1つずつループして、`item`という変数名で使います。 |
| `th:value="${item.id}"`      | 各商品のIDを、選択肢の値（value）にします。つまり、選ばれた商品のIDが送信されます。                |
| `th:text="${item.itemName}"` | 各商品の名前を、選択肢の表示文字列にします。                                        |
*/
  
        /** 通話ログ検索メソッド **/
    	//HTTP GETリクエストに対応し、ビューに通話ログデータを渡す役割を果たす（メソッド呼び出し）
        @GetMapping("/call_check")
        //@RequestParam("callmonth") にはたとえばcallmonth=2025-06 の 値「2025-06」 が yearMonth に入る
        public String viewCallCheck(@RequestParam("callmonth") String yearMonth,Model model) {
        	//指定された日付（calldate）に基づいて通話ログをデータベースから取得する処理
        	 List<CallLogs> callLogs = callLogsService.findByYearMonth(yearMonth); 
        	 //取得したデータをビューに渡す
           //取得したコールログ一覧をモデルに追加
            model.addAttribute("callLogs", callLogs);
            //入力された年月をモデルに追加
            model.addAttribute("yearMonth",yearMonth);
            
            // 認証ユーザー情報の取得
            //Spring Security の仕組みを使って、ログインユーザーの username を取得。
            //その username を使って、DBから User 情報を引っ張ってくる。
            //Authenticationオブジェクトには現在ログインしているユーザーに関する情報が含まれる
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //現在認証されているユーザーの名前を取得
            String username = authentication.getName();
            //データベースからusernameに基づいてUserオブジェクトを取得
            User user = userRepository.findByUsername(username);

            if (user != null) {
            	//ユーザーが存在する場合、ユーザー名とユーザーの役割をcall_check.htmlに渡す
            	//User オブジェクトが見つかった場合は、name → ユーザー名role → 権限（例：ADMIN、USERなど)をビューに渡すということ。
                model.addAttribute("name", user.getUsername());
                model.addAttribute("role", user.getRole());	
            }else {
                model.addAttribute("message", "ユーザー情報が見つかりませんでした。");
                return "error_page";
            }
            return "call_check"; 
            //call_check.html に遷移
        }



        //個別の通話ログ詳細を表示する画面処理
       
        @GetMapping("/callLogs/{id}")
        public String viewCallLog(@PathVariable("id") Integer id, Model model) {
       //PathVariable("id") Integer id により、URLから通話ログのIDを取得。
        //Model model は、このメソッドからビューにデータを渡すためのオブジェクト。


            // 通話ログを取得
            CallLogs callLog = callLogsService.getCallLogById(id);
            
            // 指定したIDの通話ログが存在しない（nullの場合）には、エラーメッセージをモデルに追加し、
            //エラーページ（error_page.html）に遷移します。
            if (callLog == null) {
                model.addAttribute("message", "指定された通話ログは存在しません。");
                return "error_page";
            }
            
            // 取得した通話ログの詳細をモデルに追加し、ビュー側で情報を表示できるよう。
            model.addAttribute("callLog", callLog);
            
            // 現在の認証ユーザー情報を取得
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            
            if (user != null) {
                model.addAttribute("role", user.getRole());
            }
            //ユーザー名を使ってDBからUserエンティティを取得し、そのユーザーの「役割（role）」をモデルに追加。
            //これにより、ビューでユーザーの権限に応じた表示切り替えが可能になる。
            return "call_check";
            //call_check.html というテンプレートに遷移し、モデルにセットした callLog と role を使ってページが描画されます
        }
        
        
        @PostMapping("/play")
        public String playCallLog(@RequestParam("logId") Integer logId,@RequestParam("callmonth")String yearMonth, Model model,RedirectAttributes redirectAttributes) {
            //logId: 対象の通話ログID
        	//callmonth: 元の検索対象の年月（call_check に戻すために必要
        	//RedirectAttributes →Spring MVCではリダイレクトをするとModelの情報は失われる。
        	//しかし、RedirectAttributesを使うことで、リダイレクト先に一度だけ渡せる値を持たせることができる
        	CallLogs log = callLogsService.getCallLogById(logId);
        	//指定された logId に該当する CallLogs オブジェクトを取得。

            if (log != null && log.getStatus() != null && log.getStatus() ==0) {
            	//通話ログが存在し、かつステータスが 0（未再生）であることを確認。
                log.setStatus(1);        
                // 指定された通話ログ（IDで指定）のステータスを「再生中（=1）」に変更。
                callLogsService.saveCallLog(log);
                //saveメソッドで保存
            } else {
                model.addAttribute("message", "指定された通話ログは再生できません。");
                return "error_page";  
                //通話ログが存在しない、またはステータスが 0 でない場合（再生中または再生済みなど）はエラー処理
            }

            // ログインユーザー情報を取得
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user != null) {
            	redirectAttributes.addFlashAttribute("role", user.getRole());
            //ログイン中のユーザーが見つかったときだけ、
            //user.getRole()（＝「ADMIN」や「USER」などの役割情報）を
            //一時的なデータ（Flash属性）としてリダイレクト先に渡す準備をしている
            // Flash属性なので、URLには表示されず、リダイレクト後に一度だけ Model に渡される。
            }
            redirectAttributes.addFlashAttribute("yearMonth",yearMonth);
            //ユーザーが選択・入力した年月（例：2025-06）をcall_check 画面に再表示するために渡してる。
           return "redirect:/call_check?callmonth=" + UriUtils.encode(yearMonth, StandardCharsets.UTF_8);
           //実際に /call_check へ リダイレクト（画面遷移） します。ここでは callmonth パラメータとして年月をURLに含めてる。
           /*Flash HTML内で条件分岐や表示制御したい時に便利（セッション的な扱い）
			URLパラメータ	callmonth=2025-06	@RequestParam("callmonth") で再検索するために必要*/
           /*通常、リダイレクトを行うとModelにセットした値は消えてしまいます。
            * そのときに登場するのが RedirectAttributes#addFlashAttribute() である。
            */

        }

        /*特定の通話ログ（CallLogs）の「確認」や「管理者確認」の状態を更新するための処理を担当。
         *フォームから送信された情報を受け取り、ユーザーの権限に応じてログの状態を更新し、再度通話ログ表示画面へリダイレクトする。
         */
        @PostMapping("/updateStatus")
        public String updateStatus(@RequestParam("logId") Integer logId,
                                   @RequestParam("role") String role,
                                   @RequestParam("callmonth")String yearMonth,
                                   Model model
                                   ,RedirectAttributes redirectAttributes)
      //logId：更新対象の通話ログID
      //role：操作の種類（例：confirmやadmin）
      //yearMonth：表示中の年月（リダイレクト後の画面表示に使用）
      //Model model：ビューに情報を渡すためのモデル
      //RedirectAttributes redirectAttributes：リダイレクト時に一時的なデータを渡すためのオブジェクト
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
       //spring Securityから現在ログイン中のユーザー名を取得し、データベースからユーザー情報を取得
            CallLogs log = callLogsService.getCallLogById(logId);
            if (log == null || user == null) {
                model.addAttribute("message", "ログまたはユーザー情報が取得できませんでした。");
                return "error_page";
        //指定されたログIDの通話ログ、またはユーザーが存在しない場合はエラーページを表示
            }
            System.out.println("Received role: " + role);
            System.out.println("User role: " + user.getRole());
          //デバッグ用に受け取った操作ロールとユーザーの権限をコンソールに出力している
            
            if ("confirm".equals(role) && Integer.valueOf(2).equals(user.getRole())) {
                log.setConfirmed(true);
            } else if ("admin".equals(role) && Integer.valueOf(3).equals(user.getRole())) {
                log.setAdminConfirmed(true);
            } else {
                model.addAttribute("message", "不正な操作、または権限がありません。");
                return "error_page";
            }
            //通話ログの状態を「確認済み」や「管理者確認済み」に更新する処理
          /*roleがconfirmでかつユーザーの権限が2（確認者）なら、logのconfirmedをtrueにします。
			roleがadminでかつユーザーの権限が3（管理者）なら、logのadminConfirmedをtrueにします。
			それ以外の場合は、不正な操作か権限不足とみなしてエラーを返します。*/
            
            redirectAttributes.addFlashAttribute("yearMonth",yearMonth);
            //これはリダイレクト先のコントローラーやビューにyearMonthを一時的にデータを渡すためのもの。
            callLogsService.saveCallLog(log);
            // //通話ログの状態変更を永続化（データベースに保存）
            //例えば、「確認済み」や「管理者確認済み」などのフラグをセットしたlogオブジェクトを更新。
            
            return "redirect:/call_check?callmonth=" + UriUtils.encode(yearMonth, StandardCharsets.UTF_8);
          //redirect:で始まる返却値はSpring MVCに「このURLにリダイレクト（再送信）してください」と伝えます
            //yearMonth（例: "2025-06"）をURLエンコードして、安全にURLに埋め込みます。
            
           /*このメソッドは、通話ログの「確認者による確認」または「管理者による管理者確認」の状態を更新するためのもの。
		リクエストパラメータのroleと現在のユーザーの権限に基づいて動作する。
		適切な権限がなければエラーページを表示し、権限があればログの状態を更新し保存。*/
        }

        
        @Autowired
        private CsvService csvService;

       
        @GetMapping("/exportCsv")
        public void  generateCsv(@RequestParam("callmonth") String yearMonth, HttpServletResponse response) throws IOException {
//HTTPレスポンスオブジェクト。ここにCSVデータを書き込んで返す
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

 //ユーザー名からユーザー情報をデータベースから取得し、ユーザーIDと権限IDを取り出しています。
            Integer userId = userRepository.findByUsername(username).getId();
            Integer roleId = userRepository.findByUsername(username).getRole();
//ダウンロードするCSVファイル名を「calllog_2025-06-05.csv」のように設定しています。
            String fileName = "calllog_" + LocalDate.now() + ".csv";


            // レスポンス設定.データ形式の指定とブラウザにCSVファイルとして書き出すための処理
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            //contenttypeはCSVと指定
//attachment は「このレスポンスを画面に表示するのではなく、ファイルとしてダウンロードさせる」ことをブラウザに指示します。
//filename= は「保存時のファイル名」を指定します
            // CSV出力
            try (OutputStream o = response.getOutputStream();
            		//Shift-jis形式で呼び出す
                    OutputStreamWriter writer = new OutputStreamWriter(o, "MS932")) {
            	/*レスポンスの出力ストリームを取得し、文字コード「MS932」（Shift-JIS）でラップしている
            	 * そのwriterを使ってcsvService.createCsvFile()を呼び出し、実際のCSVデータ生成を書き込ませている。
            	try-with-resourcesで自動的に閉じるのでflush()は省略可能*/
                   csvService.createCsvFile(writer, yearMonth, userId,roleId);
                   //writer.flush();tryブロックを抜けるとブロックされるので今回はflush()は必要なし
               }
        }
        
        
}




        
      





