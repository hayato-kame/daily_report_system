package controllers.employees;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Employee;

@WebServlet("/employees/new")
public class EmployeesNewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public EmployeesNewServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // リクエストスコープに保存する
        request.setAttribute("_token", request.getSession().getId());  // セッションスコープからセッションIDを取得する それを リクエストスコープに保存する
        request.setAttribute("employee", new Employee());  // 空のEmployeeインスタンスをリクエストスコープに保存する


        // 新規登録のビューの new.jsp へフォワードする
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/employees/new.jsp");
        rd.forward(request, response);

    }


}
