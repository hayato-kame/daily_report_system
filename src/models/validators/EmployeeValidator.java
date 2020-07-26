package models.validators;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import models.Employee;
import utils.DBUtil;

// バリデーションのクラス
public class EmployeeValidator {

    /**
     * バリデーションのメソッドを定義する staticキーワードのついている クラスメソッド
     * staticメンバ    クラスメソッド  インスタンス化しなくても使える クラス名.メソッド名で使える
     * @param e  Employeeクラスのインスタンス
     * @param code_duplicate_check_flag
     * @param password_check_flag
     * @return List<String> エラーメッセージが入ったリストを返す   リストの要素が、全部空文字の時、入力チェックOKになる
     */
    public static List<String> validate(Employee e, Boolean code_duplicate_check_flag, Boolean password_check_flag) {

        // エラーメッセージをいれるリストのインスタンスを生成する 要素はまだ無い
        List<String> errors = new ArrayList<String>();

        String code_error = _validateCode(e.getCode(), code_duplicate_check_flag);
        if (!code_error.equals("")) {
            errors.add(code_error); // エラーリストに追加する
        }

        String name_error = _validateName(e.getName());
        if (!name_error.equals("")) {
            errors.add(name_error);
        }

        String password_error = _validatePassword(e.getPassword(), password_check_flag);
        if (!password_error.equals("")) {
            errors.add(password_error);
        }
        return errors;
    }


    /**
     * 社員番号チェック
     * 第2引数にBoolean型の引数を用意し、そこが true であれば、パスワードの入力値チェックと社員番号の重複チェックを行うようにしています
     * 変更（update）の場合は、バリデーションが不要な場合もあるので
     * @param code
     * @param code_duplicate_check_flag
     * @return
     */
    private static String _validateCode(String code, Boolean code_duplicate_check_flag) {
        // 必須入力チェック
        if (code == null || code.equals("")) { //  code.length() != 0 でもいいし   code.isEmpty()  でもいいし　code.equals("")　でもいい
            return "社員番号を入力してください。";
        }

        // すでに登録されている社員番号との重複チェック  社員番号のみ「すでにデータベースに存在する社員番号かどうか」のチェックも入れています
        if (code_duplicate_check_flag) { // 条件式が trueの時 {}の中の処理を行う

            EntityManager em = DBUtil.createEntityManager();
            long employees_count = (long) em.createNamedQuery("checkRegisteredCode", Long.class)
                    .setParameter("code", code)
                    .getSingleResult();
            em.close();
            if (employees_count > 0) {
                return "入力された社員番号の情報はすでに存在しています。";
            }
        }
        // 条件式がfalseの時は、
        return "";
    }


    /**
     * 社員名の必須入力チェック
     * @param name
     * @return
     */
    private static String _validateName(String name) {
        if (name == null || name.equals("")) {
            return "氏名を入力してください。";
        }

        return "";
    }


    /**
     * パスワードの必須入力チェック
     * 第2引数にBoolean型の引数を用意し、そこが true であれば、パスワードの入力値チェックと社員番号の重複チェックを行うようにしています
     * 変更（update）の場合は、バリデーションが不要な場合もあるので、
     * @param password
     * @param password_check_flag
     * @return
     */
    private static String _validatePassword(String password, Boolean password_check_flag) {
        // パスワードを変更する場合のみ実行
        if (password_check_flag && (password == null || password.equals(""))) {
            return "パスワードを入力してください。";
        }
        return "";
    }

}
