package com.company;

import com.github.msteinbeck.sig4j.signal.Signal1;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.*;

public class DBManager extends Thread {
    protected Signal1<Integer> getLastXScan_signal;
    protected Signal1<JSONArray> getLastXScanDone_signal;

    protected Signal1<JSONObject> addScanData_signal;
    protected Signal1<Boolean> addScanDataDone_signal;

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
        try (Connection connection = this.connection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
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
        try (Connection connection = this.connection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            if (lastX > 0) {
                preparedStatement.setInt(1, lastX);
            }

            ResultSet queryResult = preparedStatement.executeQuery();

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
