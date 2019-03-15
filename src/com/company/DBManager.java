package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBManager extends Thread {
    private String url;

    /**
     * @return returns the connection if the url variable in the class is properly set up
     */
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
     * @param url example: "C://sqlite/db/test.db"
     * @return TRUE if init was successful / FALSE if init was unsuccessful
     */
    public boolean init(String url) {
        if (!url.isEmpty()) {
            this.url = "jdbc:sqlite:" + url;
            String query = "CREATE TABLE IF NOT EXISTS \"scanHistory\" ( `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, `scanResult` INTEGER NOT NULL DEFAULT 0, `engineResults` TEXT NOT NULL, `scanDate` INTEGER NOT NULL )";
            try (Connection connection = this.connection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * @param url sets the url variable to the parameter value
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
