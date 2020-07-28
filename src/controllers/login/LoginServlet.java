package controllers.login;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Employee;
import utils.DBUtil;
import utils.EncryptUtil;


/**
 *
 * ログイン画面 も   ログインフォームからの送信先 も     同じ /login です。
 * 同じパスでも、    ログイン画面の表示は GET、    認証処理は POST    と処理を分けています。
 *
 * @author kamey
 *
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public LoginServlet() {
        super();
    }

    /**
     * ログイン画面を表示
     * ログイン画面の表示は GET
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // セッションスコープから、request.getSession().getId() で、セッションIDを取得してきて、
        // それを リクエストスコープに  "_token"  というキーで 保存する
        request.setAttribute("_token", request.getSession().getId());

        // リクエストスコープに  "hasError" というキーで  false という値を  保存する
        request.setAttribute("hasError", false);

        //  もし、request.getSession().getAttributeメソッドを使って セッションスコープから取得してきた
        // "flush"というキーのオブジェクトが nullじゃなかったら、リクエストスコープに保存する、その後で、要らなくなった、セッションスコープにある
        // "flush"というキーのオブジェクト は、removeAttributeメソッドで削除する、セッションスコープからは、積極的に削除することが大事

        if(request.getSession().getAttribute("flush") != null) {
            request.setAttribute("flush", request.getSession().getAttribute("flush"));
            request.getSession().removeAttribute("flush");
        }

        //  viewsフォルダ以下の    /login/login.jsp  へフォワードする

        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/login/login.jsp");
        rd.forward(request, response);


    }

    /**
     *
     * viewフォルダ以下の loginフォルダの中のlogin.jspから遷移してくる
     * 入力フォームで入力したものが POST送信されてくる
     * ログイン処理を実行
     * 認証処理は POST
     *
     * このあと作成するログインページで入力された社員番号とパスワードをもとにデータベースへ照合し、
     * 情報に間違いがなければセッションスコープに login_employee という名前で、
     * その従業員情報のオブジェクトを格納します。
     * 「セッションスコープに login_employee という名前で従業員情報のオブジェクトが保存されている状態」をログインしている状態とします。
     *
     * 繰り返しますが「セッションスコープに login_employee という名前で従業員情報のオブジェクトを持っていること」をログインしている状態だ
     * としているので、セッションスコープから login_employee を除去することでログアウトした状態にします。
     * ログアウトしたら、自動でログインページにリダイレクトされます。
     *
     * なお、パスワードはハッシュ化されてデータベースに登録されていますので、
     * フォームから入力されたパスワードに EncryptUtil.getPasswordEncrypt() を使ってソルト文字列を連結した文字列をハッシュ化し、
     * そのデータとデータベース上のデータで照合を行います。
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

     // 認証結果を格納する変数
        Boolean check_result = false;

        String code = request.getParameter("code");
        String plain_pass = request.getParameter("password");

        Employee e = null;

        if(code != null && !code.equals("") && plain_pass != null && !plain_pass.equals("")) {

            EntityManager em = DBUtil.createEntityManager();

            // ハッシュ化されたパスワードを作成する
            String password = EncryptUtil.getPasswordEncrypt(  // パスワード  ハッシュ化
                    plain_pass,   //  入力フォームから送信されたパスワード
                    (String)this.getServletContext().getAttribute("salt")  // アプリケーションスコープから取得した "salt" というキーのオブジェクトを引数にする (ソルト文字です)
                    );

            // 社員番号とパスワードが正しいかチェックする
            try {
                e = em.createNamedQuery("checkLoginCodeAndPassword", Employee.class)
                      .setParameter("code", code)   //  入力フォームで入力されてきた社員番号をセット
                      .setParameter("pass", password)   // 入力フォームで入力されてきたパスワードをハッシュ化したパスワードをセット
                      .getSingleResult();
            } catch(NoResultException ex) {}

            em.close();

            if(e != null) {
                check_result = true;
            }
        }

        if(!check_result) {
            // 認証できなかったらログイン画面に戻る
            // その際、入力フォームで入力されてきた、社員番号を、リクエストスコープに保存して、戻されたログイン画面で表示するようにしておく
         // セッションスコープから、request.getSession().getId() で、セッションIDを取得してきて、
            // それを リクエストスコープに  "_token"  というキーで 保存する
            // リクエストスコープに "hasError" というキー（名前）で、 true の値を保存する
            request.setAttribute("_token", request.getSession().getId());
            request.setAttribute("hasError", true);
            request.setAttribute("code", code);

            //   Viewとして /WEB-INF/views/login/login.jsp  へフォワード

            RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/login/login.jsp");
            rd.forward(request, response);
        } else {
            // 認証できたらログイン状態にしてトップページへリダイレクト
            request.getSession().setAttribute("login_employee", e); // セッションスコープに  "login_employee" というキーで その従業員情報のオブジェクトを格納します
            // 「セッションスコープに login_employee という名前で従業員情報のオブジェクトが保存されている状態」をログインしている状態とします

            // セッションスコープに、"flush"というキーで 文字列を保存する
            request.getSession().setAttribute("flush", "ログインしました。");

            //  request.getContextPath() + "/"  で  コンテキストパス に　スラッシュ終わりにすることで、　最初に表示される画面 トップページ となる
            // トップページにリダイレクトする
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

}