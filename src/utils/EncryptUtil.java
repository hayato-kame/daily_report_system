package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

// DAOに相当するクラスです
// 文字列をSHA256でハッシュ化するクラスの作成
// さまざまなコントローラで使えるよう、ユーティリティクラスとしてEncryptUtilクラスを作成してます

// このクラスの getPasswordEncrypt メソッドは、引数で受け取った文字列にソルト文字列を連結させたものを SHA256 でハッシュ化します。
// 引数の文字列が何もなければ、空の文字列を返します。

public class EncryptUtil {

/**
 * さまざまなコントローラで使える ハッシュ化メソッド
 * staticキーワードのついた staticメンバ   staticメソッド   クラス名.メソッド名 と記述すれば、インスタンス化しなくても使える クラスに紐づいた クラスメソッドです
 * @param plain_p 入力されたパスワード文字列
 * @param salt Webアプリケーションが起動したとき リスナーによって、プロパティファイルが読み込まれるように設定してあるので、
 * そこに書かれている ソルト文字列を第2引数に指定している    パスワードに連結させる文字列のことを ソルト文字列 といいます
 * @return String型
 */
    public static String getPasswordEncrypt(String plain_p, String salt) {
        String ret = "";

        if(plain_p != null && !plain_p.equals("")) {  // 引数の文字列plain_p が何も無い場合 は、if文の条件でfalseになり、{}の中には入らない

            byte[] bytes;
            String password = plain_p + salt;  // 入力されたパスワード文字列と、ソルト文字を連結している
            try {
                bytes = MessageDigest.getInstance("SHA-256").digest(password.getBytes());  // SHA256 でハッシュ化してる
                ret = DatatypeConverter.printHexBinary(bytes); // バイト配列をString型にしてる
            } catch(NoSuchAlgorithmException ex) {}
        }

        return ret;  // String型を返してる   if文に入らないなら(引数の文字列が何も無い場合つまり、入力したパスワードがnullだったり、空文字だったりした時)、空文字を返してる
    }
}
// Webアプリケーションが起動したとき リスナーが実行されるようにしているPropertiesListenerクラスの contextInitializedメソッドが実行され、
// リスナーによって、
// WebContent/META-INF/application.properties ファイル が読み込まれ、
// salt=********   salt という名前の変数と 値 が読み込まれる
//  パスワードに連結させる文字列のことを ソルト文字列 という  この salt というデータをパスワードのハッシュ化に用います。
