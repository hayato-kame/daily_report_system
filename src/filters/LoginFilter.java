package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import models.Employee;

@WebFilter("/*")
public class LoginFilter implements Filter {

    public LoginFilter() {

   }

    public void destroy() {

    }

/**
 * 少し難しい話ですが doFilter の引数 request は
 * サーブレットの doGet の request と違い、
 *  ServletRequest 型のオブジェクトです。
 *  doGet の request は HttpServletRequest 型のオブジェクトです。
 *  アクセスされた  サーブレット／JSPの場所と日時を記録する  ログを残すフィルタの作成するなら、
 *
 *  アクセスされた場所を取得するには HttpServletRequest が持つ getRequestURI() を利用します。
 *  // アクセスされた場所と日時を記録する
        System.out.println( ((HttpServletRequest)request).getRequestURI() + ":" + LocalDateTime.now() );

 *  フィルタの中では ((HttpServletRequest)request).getRequestURI() というように
 *   request を HttpServletRequest 型にキャストした上で getRequestURI() を実行する形で記述します。
 *
 * chain.doFilter(request, response); という1行が書かれていますが、
 * これより前に記述するか後に記述するかで動作が変わります。
 * これより前に書いた処理はサーブレットが処理を実行する 前 にフィルタの処理が実行されます。
 * 逆に、これより後に書いた処理はサーブレットが処理を実行した 後 にフィルタの処理が実行されます
 */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        //  ログイン状態のチェックは、サーブレットの処理よりも前に実行されるよう、記述します
        // なので、chain.doFilter(request, response); の一文よりも、前に記述します

        // request を HttpServletRequest 型にキャストした上で getContextPath() や getServletPath() を実行する形で記述します
       //  getContextPath()   コンテキストルートの取得
        String context_path = ((HttpServletRequest)request).getContextPath();    //     /daily_report_system    エクリプスの場合    /プロジェクト名

        // getServletPath()  サーブレット名の取得
        String servlet_path = ((HttpServletRequest)request).getServletPath();  //      /login   とかのサーブレットの URLパターン

        //  @WebFilter("/*") としているための対処
        //  基本的に、Webページを表示する上で読み込むすべてのファイルでログイン状態を調べてしまうため、
        //  reset.css や style.css の読み込みにまでも「ログインしているかのチェック」が入ってしまいます。
        //  そこで、CSSフォルダのファイルにはフィルタを適用しないよう、!servlet_path.matches("/css.*") で抜いています。

        if(!servlet_path.matches("/css.*")) {       // CSSフォルダ内は認証処理から除外する
            // セッションスコープをまず、取得します。
            //  HttpSessionオブジェクトを取得するには、HttpServletRequest#getSession()を使用します。
            //  doFilter の引数 request は ServletRequest 型のオブジェクトです
            // 対して、doGet の request は HttpServletRequest 型のオブジェクトです。ですので、
            //  request を HttpServletRequest 型にキャストした上で getSession() を実行する形で記述します。

            // "getSession"メソッドは、サーブレットを要求してきたクライアントに対してセッションが既に開始されていればそのセッションを返します。
            //  また引数に"ture"を指定した場合にはセッションが開始されていなければ新規にセッションを開始した後でそのセッションを返してきます。

            //  引数に"false"を指定した場合、セッションが存在しない場合にはnullが帰ってきます。

            HttpSession session = ((HttpServletRequest)request).getSession();  //   getSession() に引数は指定していません

            // セッションスコープに保存された従業員（ログインユーザ）情報を取得
            // セッションスコープに保存されたものは、汎用的なObject型になっているので、(Employee)でキャストしている
            // "login_employee" というキーで セッションスコープに Employeeオブジェクトが保存されていれば、ログインされている状態であり、
            // Employeeオブジェクト が null だったら、ログインされてない状態である、と判断する

            // ログアウト状態のとき（セッションスコープに従業員情報が格納されていない場合）、 e は null になります。
            Employee e = (Employee)session.getAttribute("login_employee");

            if(!servlet_path.equals("/login")) {        // ログイン画面以外について
                // ログアウトしている状態であれば
                // ログイン画面にリダイレクト
                if(e == null) {   //   e == null  が true ということは、ログインされて無い状態ということ
                    // getContextPath()  コンテキストルートの取得     /daily_report_system  です    エクリプスの場合      /プロジェクト名
                    ((HttpServletResponse)response).sendRedirect(context_path + "/login");  //   /daily_report_system  に サーブレットの URLパターンを付け足す
                    return;   //  処理を終了させる return  もし、return の後ろに書かれているものがあっても 実行されない
                //  Javaのメソッドの処理を中断することができる もともとの意味合いは、呼び出したメソッドから元のメソッドに「戻る」という意味合い
                }

                // 従業員管理の機能は管理者のみが閲覧できるようにする
                // この部分は、/login 以外のページにアクセスした場合、ログイン状態でない（e がnull）なら /login ページに強制的にリダイレクトさせます。
                // さらに、従業員管理（/emploees）のページにアクセスした場合、ログインしている従業員情報の admin_flag をチェックし、
                // 一般従業員である（admin_flag の値が0）ならトップページへリダイレクトさせるようにしています。
                // "/employees.*"   の文字列を検索する 一致するもの
                //  getServletPath()メソッド    /login   とかのサーブレットの URLパターン とか、 jspファイルなら、WebContentからの相対パス
                // getContextPath()メソッド      /daily_report_system    エクリプスの場合    /プロジェクト名
                if(servlet_path.matches("/employees.*") && e.getAdmin_flag() == 0) {  // Integer型  admin_flagプロパティ  管理者権限があるかどうか  数値型（一般：0、管理者：1）
                    ((HttpServletResponse)response).sendRedirect(context_path + "/");  // コンテキストルートの取得  /daily_report_system  これに スラッシュ"/" をつけて、トップページ を表示 (index.html とか index.jspを自動で探して表示してくれる)
                    return;  // returnによって処理を中断して戻る  トップページにリダイレクトさせてから、処理を中断して戻る
                }
            } else {   //  "login_employee" というキーで セッションスコープに Employeeオブジェクトが保存されていれた時、つまりログインしていた時
                // ログインしているのにログイン画面を表示させようとした場合は  ログインページを表示させる理由がないので
                // システムのトップページにリダイレクト      コンテキストパス    /daily_report_system   に　スラッシュ"/"をつけると、
                //  トップページになるようにしてくれてる 自動的に index.html  や index.jsp を順番に探して表示してくれる
                if(e != null) {
                    ((HttpServletResponse)response).sendRedirect(context_path + "/");
                    return;  // returnによって処理を中断して戻る  トップページにリダイレクトさせてから、処理を中断して戻る
                }
            }
        }



        chain.doFilter(request, response);


    }


    public void init(FilterConfig fConfig) throws ServletException {

    }

}
