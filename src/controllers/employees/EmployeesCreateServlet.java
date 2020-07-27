package controllers.employees;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Employee;
import models.validators.EmployeeValidator;
import utils.DBUtil;
import utils.EncryptUtil;

@WebServlet("/employees/create")
public class EmployeesCreateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public EmployeesCreateServlet() {
        super();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String _token = (String) request.getParameter("_token");

        if (_token != null && _token.equals(request.getSession().getId())) {

            EntityManager em = DBUtil.createEntityManager();

            Employee e = new Employee();

            e.setCode(request.getParameter("code"));
            e.setName(request.getParameter("name"));
            e.setPassword(
                    EncryptUtil.getPasswordEncrypt( // 引数を2つとるEncryptUtilクラスの static な クラスメソッド
                            request.getParameter("password"), // 第一引数では、パラメータで送られた入力されたパスワードを取得
                            (String) this.getServletContext().getAttribute("salt") // 第2引数では、ソルト文字   リスナーによって プロパティファイルに書かれていたソルト文字を取得してアプリケーションスコープに登録されたものです、
                    ));
            // パラメータで渡ってきたものを
            // Integer型の  admin_flag プロパティにセットする　管理者権限があるかどうか  数値型（一般：0、管理者：1）
            e.setAdmin_flag(Integer.parseInt(request.getParameter("admin_flag")));

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            e.setCreated_at(currentTime);
            e.setUpdated_at(currentTime);
            // Integer型の delete_flagプロパティ にセットする 新規作成なので 0 を 引数にセットする
            e.setDelete_flag(0); // 削除された従業員かどうか  数値型（現役：0、削除済み：1）

            // 新規登録の場合、パスワードの入力値チェックと社員番号の重複チェックは必ず実施したいので、
            // List<String> errors = EmployeeValidator.validate(e, true, true); と記述（EmployeeValidator.validate() の第2と第3引数を両方とも true で指定）しています。
            List<String> errors = EmployeeValidator.validate(e, true, true);
            if(errors.size() > 0) {
                // エラーがあったら、データベースの処理は行わないので、EntityManagerオブジェクトは閉じる

                em.close();
                // リクエストスコープに保存する
                request.setAttribute("_token", request.getSession().getId());  // セッションスコープから取得したセッションIDを　リクエストスコープに登録し直す
                request.setAttribute("employee", e);  //  入力してもらった情報をEmployeeインスタンスにセットして リクエストスコープにセットして、もう一度、確認してもらうために、新規登録画面に戻してあげる
                request.setAttribute("errors", errors);  // エラーの文言が入ったリストを リクエストスコープにセットする

                // 新規作成画面のビューへ フォワードする
                RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/employees/new.jsp");
                rd.forward(request, response);
            } else {
                // エラーがなかった時 データベースの処理を行う
                em.getTransaction().begin();
                em.persist(e);  // persist(引数)  オブジェクトをデータベースに新規登録するメソッド
                em.getTransaction().commit();
                em.close();

                // セッションスコープに登録する
                request.getSession().setAttribute("flush", "登録が完了しました。");
                // リダイレクトする
                response.sendRedirect(request.getContextPath() + "/employees/index");
            }
        }
    }

}
