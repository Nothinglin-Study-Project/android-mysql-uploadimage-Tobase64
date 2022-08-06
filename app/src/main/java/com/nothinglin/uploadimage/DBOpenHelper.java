package com.nothinglin.uploadimage;

import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBOpenHelper {
    private static String diver = "com.mysql.jdbc.Driver";

    private static String url = "jdbc:mysql://192.168.0.6:3306/androidmysql?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    private static String user = "androidmysql";//mysql用户名
    private static String password = "1353695200";//mysql用户密码


    /**
     * 连接数据库
     */

    public static Connection getConn(){
        Connection conn = null;
        try {
            Class.forName(diver);//尝试调用mysql驱动
            conn = (Connection) DriverManager.getConnection(url,user,password);//获取mysql的驱动管理
            System.out.println("数据库连接成功");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return conn;
    }
}
