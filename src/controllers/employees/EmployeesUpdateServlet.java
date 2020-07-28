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

@WebServlet("/employees/update")
public class EmployeesUpdateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public EmployeesUpdateServlet() {
        super();
    }

    /**
     * ビューフォルダ以下の、employeesフォルダ直下のedit.jsp から遷移してくる
     * edit.jspからは、何も送られてこない、
     * ここで使う Employeeインスタンスの id は、
     *  edit.jsp の前の EmployeeEditServlet で　セッションスコープに保存したものを取得します  キーが"employee_id" で保存してあります。
     *  ここで、使用したあとは、使わなくなるから、セッションスコープに保存したものは、積極的に削除します。
     *
     * 入力フォームから、POST送信で送られてくるので、doPostメソッドに書く
     * 変更内容をデータベースに更新保存する処理
     * edit.jsp では　共通ファイルの _form.jspファイルを インポートしており、その中に
     * input type="hidden" name="_token" value="${_token}"
     * とあるので、 hiddenで この EmployeesUpdateServletサーブレットに POST送信されてきてます
     *
     * if(_token != null && _token.equals(request.getSession().getId())) { ... } でCSRF対策のチェックを行っています。
     * _token に値がセットされていなかったりセッションIDと値が異なったりしたらデータの登録ができないようにしています。
     * ここのチェックがtrueにならないのは、意図しない不正なページ遷移によって /create へアクセスされた場合です。
     * 悪意のあるネット利用者が勝手に投稿できないようにするための対策です。
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String _token = (String)request.getParameter("_token");

        if(_token != null && _token.equals(request.getSession().getId())) {

            EntityManager em = DBUtil.createEntityManager();

            Employee e = em.find(Employee.class, (Integer)(request.getSession().getAttribute("employee_id")));
         // 検索した社員番号と、入力フォームから送られてきた社員番号が同じかどうか
            // 現在の値と異なる社員番号が入力されていたら
            // 重複チェックを行う指定をする
            Boolean code_duplicate_check = true;  // 社員コード重複チェック
            if(e.getCode().equals(request.getParameter("code"))) {  // 検索した社員番号と、入力フォームから送られてきた社員番号が同じだったのなら
                // 同じであった  重複はしてない ・・・つまり、変更は希望してない
                code_duplicate_check = false; // 同じであったので code_duplicate_check の値に false を代入する   重複してない！
            } else { // 同じではなかったので、code_duplicate_check の値は trueのまま  重複してる！！
                // 変更を希望してるので、新しい、入力フォームで送られてきたのをセット
                e.setCode(request.getParameter("code")); // セッター使って、Employeeインスタンスにセットする
            }

            // パスワード欄に入力があったら
            // パスワードの入力値チェックを行う指定をする
            Boolean password_check_flag = true;  //  パスワード入力チェック
            String password = request.getParameter("password");
            // 入力フォームでPOST送信されてきたパスワードがnull 、もしくは 空文字 という条件分が true の時は {}の中の処理が行われる
            // パスワードがnull 、もしくは 空文字なら、 password_check_flag の値に false を代入する
            if(password == null || password.equals("")) {
                password_check_flag = false;
            } else {  // null もしくは 空白文字ではなかった
                e.setPassword(   // セッター使って、Employeeインスタンスにセットする
                        EncryptUtil.getPasswordEncrypt(    // 入力されたパスワードと、アプリケーションスコープから取得したソルト文字を連結するメソッドを使う
                                password,
                                (String)this.getServletContext().getAttribute("salt")
                                )
                        );
            }

            e.setName(request.getParameter("name"));
            e.setAdmin_flag(Integer.parseInt(request.getParameter("admin_flag")));
            e.setUpdated_at(new Timestamp(System.currentTimeMillis()));
            e.setDelete_flag(0);

            List<String> errors = EmployeeValidator.validate(e, code_duplicate_check, password_check_flag);
            if(errors.size() > 0) {
                // エラーがあったら、データベース操作はしないから、クローズする
                em.close();
                // 一旦、編集ページのビューに戻すため、
                // 入力フォームで送られてきたものでセットしたEmployeeインスタンスをセットして、編集画面に表示するために
                // Employeeインスタンスをリクエストスコープにセットする、
                request.setAttribute("_token", request.getSession().getId());
                request.setAttribute("employee", e);
                request.setAttribute("errors", errors);

                // 編集画面のビューにフォワードする
                RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/employees/edit.jsp");
                rd.forward(request, response);
            } else {
                // エラーがなかったので、データベース操作ができる
                em.getTransaction().begin();
                em.getTransaction().commit();
                em.close();
                request.getSession().setAttribute("flush", "更新が完了しました。");

                request.getSession().removeAttribute("employee_id");  // 使わなくなったから、セッションスコープに保存したものは、積極的に削除します。

                // EmployeeIndexServletサーブレット にリダイレクトする
                response.sendRedirect(request.getContextPath() + "/employees/index");
            }
        }
    }
}