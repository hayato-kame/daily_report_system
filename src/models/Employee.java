package models;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 4つのSELECT文を用意しました。
 getAllEmployees（すべての従業員情報を取得）
 getEmployeesCount（従業員情報の全件数を取得）
 checkRegisteredCode は、指定された社員番号がすでにデータベースに存在しているかを調べます。
 checkLoginCodeAndPassword は従業員がログインするときに社員番号とパスワードが正しいかをチェックするためのものです。

  code のプロパティに unique = true という指定を入れました  一意制約
*/
@Table(name = "employees")
@NamedQueries({
        @NamedQuery(name = "getAllEmployees", query = "SELECT e FROM Employee AS e ORDER BY e.id DESC"),
        @NamedQuery(name = "getEmployeesCount", query = "SELECT COUNT(e) FROM Employee AS e"),
        @NamedQuery(name = "checkRegisteredCode", query = "SELECT COUNT(e) FROM Employee AS e WHERE e.code = :code"),
        @NamedQuery(name = "checkLoginCodeAndPassword", query = "SELECT e FROM Employee AS e WHERE e.delete_flag = 0 AND e.code = :code AND e.password = :pass")
})
@Entity
public class Employee {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自動連番 主キーにユニークな値を自動で生成
    private Integer id; // リソース内での連番

    @Column(name = "code", nullable = false, unique = true)  //  一意制約
    private String code; // 社員番号  文字列型  codeプロパティには一意制約を付けた
    // データベースの用語で 一意制約 といい、すでに存在している社員番号は登録できない旨をデータベースに教えてあげるための設定
    // 従業員がシステムにログインする場合は社員番号をログインIDとして使ってもらいたいので、code に一意制約を入れています


    @Column(name = "name", nullable = false)
    private String name; // 社員名

    @Column(name = "password", length = 64, nullable = false)  //  length = 64  入力できる文字情報が最大64文字までになります
    private String password; // システムへのログインパスワード
    //  今回、パスワードの情報については SHA256 というハッシュ関数を利用してハッシュ化した文字列をデータベースへ保存できるようにします。
    //  ハッシュ化されていない生のパスワード文字列をデータベースに保存するのはセキュリティ面から考えて危険だからです。
    //  SHA256 は、どんな文字数の文字列でも必ず、64文字のハッシュ化された文字列にしてくれます。そのため、固定で64文字までという設定を追記したのです。

    @Column(name = "admin_flag", nullable = false)
    private Integer admin_flag; // 管理者権限があるかどうか  数値型（一般：0、管理者：1）

    @Column(name = "created_at", nullable = false)
    private Timestamp created_at; // 登録日時  日時型

    @Column(name = "updated_at", nullable = false)
    private Timestamp updated_at; // 更新日時  日時型

    @Column(name = "delete_flag", nullable = false)
    private Integer delete_flag; // 削除された従業員かどうか  数値型（現役：0、削除済み：1）

    // アクセッサ
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAdmin_flag() {
        return admin_flag;
    }

    public void setAdmin_flag(Integer admin_flag) {
        this.admin_flag = admin_flag;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    public Integer getDelete_flag() {
        return delete_flag;
    }

    public void setDelete_flag(Integer delete_flag) {
        this.delete_flag = delete_flag;
    }
}