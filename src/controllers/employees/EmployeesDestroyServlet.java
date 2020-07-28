package controllers.employees;

import java.io.IOException;
import java.sql.Timestamp;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Employee;
import utils.DBUtil;

@WebServlet("/employees/destroy")
public class EmployeesDestroyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public EmployeesDestroyServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String _token = (String) request.getParameter("_token");

        if (_token != null && _token.equals(request.getSession().getId())) {

            EntityManager em = DBUtil.createEntityManager();

            // EmployeeEditServletで、request.getSession().setAttribute("employee_id", e.getId()); というふうにして
            // Employeeインスタンスの id を  "employee_id" というキーで、後で更新と削除のサーブレットでも使えるようにセッションスコープに置いておいたので、
            // 削除する役割のこのEmployeesDestroyServletで、セッションスコープから "employee_id" のキーを指定することによって、
            // Employeeインスタンスの id を取得してます

            Employee e = em.find(Employee.class, (Integer) (request.getSession().getAttribute("employee_id")));

            //  Integer型の  delete_flag プロパティ は、削除された従業員かどうか  数値型（現役：0、削除済み：1）
            e.setDelete_flag(1);  // 1  を引数にすると、削除済み
            e.setUpdated_at(new Timestamp(System.currentTimeMillis())); // 現在日時をセット

            // データベースの処理をします
            // 注意するのは、message_board ではHibernate(JPA)の remove メソッドを使って削除しましたが、今回は使っていません。
            // 代わりに Employee プロパティのひとつである delete_flag が1になっている従業員情報は削除されている とみなす ルールでシステムを作っています。

            // 日報は従業員一人ひとりが作成します。つまり、提出されたひとつの日報は一人の従業員に紐づいています。
            // もし日報を残したまま従業員だけ削除されてしまうと、提出者が不明の日報情報が溜まってしまいます。いわば、日報が宙に浮いた状態です。
            // それを防ぐために、destroy した従業員情報は削除したとみなしてシステム上で扱うことにして、従業員情報そのものはデータベースへ残す 形にしています。
            // このような方法を 論理削除 と呼んでいます。
            // message_boardでは、remove メソッドを使ってデータ自体を  削除しました(データベースから削除した)が、
            // この日報では、delete_flagで論理削除をすることで、elete_flag が1になっている従業員情報は削除されている とみなすことにしていて、
            // 実際のデータベース上は、残しています。 データを削除すると、「トラブルが起きた時に検証不能に陥る可能性がある」ため、論理削除を行う、という論拠でdelete_flagを利用されることが多くあります


            em.getTransaction().begin();
            em.getTransaction().commit();  // 実際は、データベースからは削除していないことに注意する
            em.close();

            // 無事に処理が成功したら、メッセージをセッションスコープに保存します。
            request.getSession().setAttribute("flush", "削除が完了しました。");

            // EmployeesIndexServletサーブレットに  リダイレクトします
            response.sendRedirect(request.getContextPath() + "/employees/index");

        }

    }
}
