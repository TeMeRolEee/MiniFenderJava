package com.company;

import com.github.msteinbeck.sig4j.signal.Signal1;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class DBManager extends Thread {
    @Override
    public void run() {
        super.run();
    }


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

    private String url;

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

                boolean queryResult = preparedStatement.execute();
                preparedStatement.close();

                return queryResult;
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

    public boolean addScanData(JSONObject jsonObject) throws ParseException {
        String query = "INSERT INTO scanHistory (scanResult, engineResults, scanDate) VALUES (?, ?, ?)";
        try (Connection connection = this.connection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, (Integer) jsonObject.get("scanResult"));
            preparedStatement.setString(2, jsonObject.get("engineResults").toString());
            preparedStatement.setInt(3, (Integer) jsonObject.get("scanDate"));

            boolean queryResult = preparedStatement.execute();
            preparedStatement.close();

            return queryResult;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
