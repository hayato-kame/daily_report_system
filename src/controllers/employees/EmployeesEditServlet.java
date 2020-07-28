package controllers.employees;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Employee;
import utils.DBUtil;

@WebServlet("/employees/edit")
public class EmployeesEditServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public EmployeesEditServlet() {
        super();
    }

    /**
     * show.jsp から aタグで urlの末尾にくっついてパラメータが送られてくる
     * aタグで送ると、GET送信になる GET 送信で受け取りますので doGetの中に処理を書く
     * パラメーターで送られてくる（GETで送られくる)
     * idを元にして、データベースから、情報を取得する
     * 編集画面を表示するためののサーブレット
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager em = DBUtil.createEntityManager();
        // データベースから、該当するid の情報を取得する
        Employee e = em.find(Employee.class, Integer.parseInt(request.getParameter("id")));

        em.close();

        // データベースから検索して取得したEmployeeインスタンスをリクエストスコープにセットして、
        request.setAttribute("employee", e);
        // セッションIDをセッションスコープから取得して、それをリクエストスコープに保存する
        request.setAttribute("_token", request.getSession().getId());

        // Employeeインスタンスの id だけは、後で、削除のサーブレットでも使えるようにセッションスコープに置いておく
        // もし、削除するとなると、セッションスコープに保存しておくことで、EmployeesDestroyServletサーブレットの方でも呼び出せるので、
        // Employeeインスタンスの id を セッションスコープに置いておきます。
        request.getSession().setAttribute("employee_id", e.getId());

        // ビューの 編集画面  (   WebContent/WEB-INF/views/employees/edit.jsp   )  へフォワードする
        // ビューの 編集画面では、条件分岐の処理によって  Employeeインスタンスが null では無い時は、表示するが、
        // null だった時は、お探しのデータは見つかりませんでした。と表示される。
        // この後、edit.jsp に行った後には、 編集した内容で更新するなら、EmployeeUpdateServletへPOST送信される
        // もしも、削除するなら EmployeeDestroyServletへPOST送信される  というふうに処理が別れます。 edit.jspを跨いで送信される先で、
        //  ここで検索した Employeeインスタンスの id が 必要となるので、セッションスコープに置いた訳です。
        // その際に、ここで、セッションスコープに登録した、"employee_id" の キーで、 Employeeインスタンスの id を取得するようになってます。

        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/employees/edit.jsp");
        rd.forward(request, response);

    }

}
