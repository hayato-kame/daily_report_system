package controllers.employees;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Employee;
import utils.DBUtil;

@WebServlet("/employees/index")
public class EmployeesIndexServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public EmployeesIndexServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        EntityManager em = DBUtil.createEntityManager();

        int page = 1;  // 何ページ目なのか ページ番号
        try{
            page = Integer.parseInt(request.getParameter("page"));
        } catch(NumberFormatException e) { }

        List<Employee> employees = em.createNamedQuery("getAllEmployees", Employee.class)
                                     .setFirstResult(15 * (page - 1))    //  １ページ目では、０番目のから表示されるようにしている
                                     .setMaxResults(15)
                                     .getResultList();

        long employees_count = (long)em.createNamedQuery("getEmployeesCount", Long.class)  // 全件数を取得している
                                       .getSingleResult();

        em.close();

        request.setAttribute("employees", employees);
        request.setAttribute("employees_count", employees_count);
        request.setAttribute("page", page);
        if(request.getSession().getAttribute("flush") != null) {
            request.setAttribute("flush", request.getSession().getAttribute("flush"));
            request.getSession().removeAttribute("flush");  // いらなくなったセッションスコープのものは、積極的にremoveAttribute(キー) してください
        }

        // ビューへ フォワードする
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/employees/index.jsp");
        rd.forward(request, response);

    }

}
