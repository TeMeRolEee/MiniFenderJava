package com.company;

import com.github.msteinbeck.sig4j.signal.Signal0;
import com.github.msteinbeck.sig4j.signal.Signal1;
import com.github.msteinbeck.sig4j.signal.Signal2;
import com.github.msteinbeck.sig4j.signal.Signal3;
import netscape.javascript.JSObject;
import org.ini4j.Ini;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class Core extends Thread {
    private Signal3<String, String, String> addNewEngine_signal;
    private Signal2<UUID, String> startNewScanTask_signal;
    private Signal0 removeEngines_signal;
    private Signal1<UUID> startCalculateResult_signal;

    private String rootDirectory = null;
    private EngineHandler engineHandler;
    private DBManager dbManager;
    private CliHandler cliHandler;

    Map<UUID, JSONObject> scanMap;

    private JSONObject calculateResult_slot(UUID uuid) {

        return null;
    }


    boolean init(String rootDirectory) {
        if (!rootDirectory.isEmpty()) {
            this.rootDirectory = rootDirectory;

            addNewEngine_signal = new Signal3<>();
            engineHandler = new EngineHandler();
            dbManager = new DBManager();
            cliHandler = new CliHandler();
            startNewScanTask_signal = new Signal2<>();
            removeEngines_signal = new Signal0();
            startCalculateResult_signal = new Signal1<>();

            startCalculateResult_signal.connect(this::calculateResult_slot);
            startNewScanTask_signal.connect(engineHandler::handlerNewTask_slot);

            dbManager.start();
            if (!dbManager.init(rootDirectory + "\\db\\scanHistoryDB.sqlite")) {
                dbManager.interrupt();
                return false;
            }

            if (!readSettings(rootDirectory + "\\settings\\settings.ini")) {
                return false;
            }

            cliHandler.init();
            cliHandler.newTask_signal.connect(this::handleNewTask_slot);
            cliHandler.start();

            return true;
        }

        return false;
    }

    private boolean readSettings(String filePath) {
        Ini ini = new Ini();
        try {
            ini.load(new FileReader(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<String> strings = ini.keySet();
        int badEngineCount = 0;

        for (int i = 0; i < strings.size(); i++) {
            String key = (String) strings.toArray()[i];
            List<String> childrenNames = Arrays.asList(ini.get(key).childrenNames());
            if (childrenNames.contains("path") && childrenNames.contains("scan_parameter")) {
                String path = "";
                String scanParameter = "";
                for (int j = 0; j < childrenNames.size(); j++) {
                    if (childrenNames.toArray()[j].toString().equals("path")) {
                        path = childrenNames.toArray()[j].toString();
                    }

                    if (childrenNames.toArray()[j].toString().equals("scan_parameter")) {
                        scanParameter = childrenNames.toArray()[j].toString();
                    }
                }
                addNewEngine_signal.emit(path, scanParameter, key);
            } else {
                badEngineCount++;
            }
        }

        return badEngineCount != strings.size();
    }

    private void handleNewTask_slot(String filePath) {
        if (filePath.isEmpty()) {
            File file = new File(filePath);
            if (file.exists()) {
                UUID uuid = new UUID(file.hashCode(),file.hashCode());

                JSONObject initialData = new JSONObject();
                JSONArray initialArray = new JSONArray();
                initialData.put("scanDate", Instant.now().toEpochMilli());
                initialData.put("engineResults", initialArray);
                scanMap.put(uuid, initialData);

                startNewScanTask_signal.emit(uuid, filePath);
            } {
                System.out.println("[ERROR]\t" + filePath + "<-- file does not exists!");
            }
        } else {
            System.out.println("No filepath was given, ignoring scan request.");
        }
    }
}
