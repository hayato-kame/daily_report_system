package controllers.toppage;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// @WebServlet に /index.html とファイル名みたいなものを入れたのは
// http://localhost:8080 という記述のみでトップページにアクセスできるようにするためです
// サイトのトップページのURLについて  スラッシュ終わりのURLにアクセスすると、自動でindex.htmlを探して画面に表示してくれるため
// Tomcatが起動したことを確認したらブラウザを開いて http://localhost:8080/daily_report_system/ とだけ入力してエンターキーを押します
// スラッシュ（/）終わりのURLにアクセスします アクセスする際のURLはスラッシュ（/）終わり
@WebServlet("/index.html")
public class TopPageIndexServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public TopPageIndexServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // セッションスコープから "flush" のキーで取り出してきた(情報を取得してくるだけで、セッションスコープには残ってる)Object型のオブジェクトが nullでなければ、それをリクエストスコープに保存し直す
        if(request.getSession().getAttribute("flush") != null){
            request.setAttribute("flush", request.getSession().getAttribute("flush"));
            // 使い終わった後は、セッションスコープに残しておくのは悪いので、積極的に削除する
            request.getSession().removeAttribute("flush");
        }
// リクエストスコープにフラッシュメッセージを設定しましたので、フラッシュメッセージがセットされていたら、そのメッセージをビューで表示する
        // ビューのindex.jspへ フォワードする
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/topPage/index.jsp");
        rd.forward(request, response);
    }

}
