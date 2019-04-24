package com.company;

import com.github.msteinbeck.sig4j.signal.Signal1;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.*;

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
    private Connection connection() {
        Connection conn = null;
        try {
            //System.out.println(url);
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
                 Statement statement = connection.createStatement()) {
                //System.out.println("[DBManager]\t Initializing db");
                return statement.executeUpdate(query) == 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void addScanData_slot(JSONObject jsonObject) {
        String query = "INSERT INTO scanHistory (scanResult, engineResults, scanDate) VALUES (?, ?, ?)";
        try (Connection connection = this.connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, (Integer) jsonObject.get("scanResult"));
            statement.setString(2, jsonObject.get("engineResults").toString());
            statement.setInt(3, (Integer) jsonObject.get("scanDate"));

            this.addScanDataDone_signal.emit(statement.execute(query));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param lastX > 0 number, shows how many previous scan will returned
     * @return scan result json / NULL if cannot get the data
     */
    private void getLastXScan_slot(int lastX) {
        JSONArray jsonArray = new JSONArray();

        String query = "SELECT scanResult, engineResults, scanDate FROM scanHistory ORDER BY id DESC LIMIT ?";
        try (Connection connection = this.connection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, lastX);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                JSONObject data = new JSONObject();
                JSONObject engineResults = (JSONObject) resultSet.getObject("engineResults");

                int scanResult = resultSet.getInt("scanResult");
                int scanDate = resultSet.getInt("scanDate");

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
