package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager extends Thread {
    private String url;

    private Connection connection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException exc) {
            System.out.println(exc.getMessage());
        }
        return conn;
    }

    /**
     * @param url example: "jdbc:sqlite:C://sqlite/db/test.db"
     * @return TRUE if init was successful / FALSE if init was unsuccessful
     */
    public boolean init(String url) {
        this.url = "jdbc:sqlite:" + url;
        return false;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
