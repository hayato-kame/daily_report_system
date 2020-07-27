package listeners;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application Lifecycle Listener implementation class PropertiesListener
 *
 */
@WebListener
public class PropertiesListener implements ServletContextListener {

    /**
     * Default constructor.
     */
    public PropertiesListener() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  {
         // TODO Auto-generated method stub
    }

    /**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     * リスナーは、Webアプリケーションの動作を監視し、特定の動作が起こったことを感知したら特定の処理を実行するというもの
     * アプリケーション全般の設定をテキスト形式のファイルから読み込み、アプリケーションスコープに設定値を格納する
     * アプリケーションの起動時 に実行する、
     *
     * Webアプリケーションが起動したとき」の処理については javax.servlet.ServletContextListener を選んでください
     * Webアプリケーションが起動したとき」の処理を定義するのは contextInitialized メソッドの中です
     *
     *
     */
    public void contextInitialized(ServletContextEvent arg0)  {


        //  ServletContextオブジェクト（コンテキスト情報）を取得するには,HttpServlet#getServletContext()を使用します。

        // arg0.getServletContext() このメソッドで取得できるオブジェクトを ServletContext 型の変数 context に格納しておきます
        //
        ServletContext context = arg0.getServletContext();

        // このリスナーを正しく実行させるためには「アプリケーション全般の設定が書かれたテキスト形式のファイル」を用意する必要があります
        // META-INF は WEB-INF と同じくインターネット上には公開されないファイルを格納するためのフォルダで、主にアプリケーションの設定ファイルを格納するために利用します
        String path = context.getRealPath("/META-INF/application.properties");  // META-INF フォルダの中 application.properties ファイル
        try {
            InputStream is = new FileInputStream(path);
            Properties properties = new Properties();
            properties.load(is);
            is.close();

            Iterator<String> pit = properties.stringPropertyNames().iterator();
            while(pit.hasNext()) {
                String pname = pit.next();
                context.setAttribute(pname, properties.getProperty(pname));  // 1つずつアプリケーションスコープに登録する
            }
        } catch(FileNotFoundException e) {
        } catch(IOException e) {}


    }

}
