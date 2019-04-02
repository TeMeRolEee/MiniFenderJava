package com.company;

import com.github.msteinbeck.sig4j.signal.Signal1;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.sqlite.SQLiteConnection;
import org.sqlite.jdbc4.JDBC4Connection;
import org.sqlite.jdbc4.JDBC4PreparedStatement;
import org.sqlite.jdbc4.JDBC4ResultSet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings("Duplicates")
public class DBManager extends Thread {
    protected Signal1<Integer> getLastXScan_signal;
    protected Signal1<JSONArray> getLastXScanDone_signal;

    protected Signal1<JSONObject> addScanData_signal;
    protected Signal1<Boolean> addScanDataDone_signal;

    private String url;

    public DBManager() {
        getLastXScan_signal = new Signal1<>();
        getLastXScanDone_signal = new Signal1<>();
        addScanData_signal = new Signal1<>();
        addScanDataDone_signal = new Signal1<>();

        this.addScanData_signal.connect(this::addScanData_slot);
        this.getLastXScan_signal.connect(this::getLastXScan_slot);
    }

    @Override
    public void run() {
        super.run();
    }


    /**
     * @return returns the connection if the url variable in the class is properly set up
     */
    private JDBC4Connection connection() {
        org.sqlite.jdbc4.JDBC4Connection conn = null;
        try {
            conn = (JDBC4Connection) DriverManager.getConnection(url);
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
            SQLiteConnection connection = this.connection();
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = new JDBC4PreparedStatement(connection, query);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                boolean queryResult = preparedStatement.execute();
                connection.close();

                return queryResult;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void addScanData_slot(JSONObject jsonObject) {
        String query = "INSERT INTO scanHistory (scanResult, engineResults, scanDate) VALUES (?, ?, ?)";
        Connection connection = this.connection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            preparedStatement.setInt(1, (Integer) jsonObject.get("scanResult"));
            preparedStatement.setString(2, jsonObject.get("engineResults").toString());
            preparedStatement.setInt(3, (Integer) jsonObject.get("scanDate"));

            boolean queryResult = preparedStatement.execute();
            connection.close();

            this.addScanDataDone_signal.emit(queryResult);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.addScanDataDone_signal.emit(false);
    }

    /**
     * @param lastX > 0 number, shows how many previous scan will returned
     * @return scan result json / NULL if cannot get the data
     */
    private void getLastXScan_slot(int lastX) {
        JSONArray jsonArray = new JSONArray();

        String query = "SELECT scanResult, engineResults, scanDate FROM scanHistory ORDER BY id DESC LIMIT ?";
        Connection connection = this.connection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (lastX > 0) {
                preparedStatement.setInt(1, lastX);
            }

            JDBC4ResultSet queryResult = (JDBC4ResultSet) preparedStatement.executeQuery();

            while (queryResult.next()) {
                JSONObject data = new JSONObject();
                int scanResult = queryResult.getInt("scanResult");
                JSONObject engineResults = (JSONObject) queryResult.getObject("engineResults");
                int scanDate = queryResult.getInt("scanDate");
                data.put("scanResult", scanResult);
                data.put("engineResults", engineResults);
                data.put("scanDate", scanDate);
                jsonArray.add(data);
            }

            this.getLastXScanDone_signal.emit(jsonArray);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.getLastXScanDone_signal.emit(null);
    }
}
