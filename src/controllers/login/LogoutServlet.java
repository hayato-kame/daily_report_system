package controllers.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public LogoutServlet() {
        super();
    }

    /**
     * 繰り返しますが「セッションスコープに login_employee という名前で従業員情報のオブジェクトを持っていること」をログインしている状態だ
     * としているので、セッションスコープから login_employee を除去することでログアウトした状態にします。
     * ログアウトしたら、自動でログインページにリダイレクトされます。
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // セッションスコープから、"login_employee"  というキーで 保存してある 従業員情報のオブジェクトを  removeAttributeすることで、
        // ログアウトの状態としている
        request.getSession().removeAttribute("login_employee");

        request.getSession().setAttribute("flush", "ログアウトしました。");

        // ログアウトしたら、自動で、ログインページのビューに遷移するように、している
        // LoginServletサーブレットにリダイレクトしている
        response.sendRedirect(request.getContextPath() + "/login");

    }

}
