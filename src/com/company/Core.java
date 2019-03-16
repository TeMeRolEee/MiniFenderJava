package com.company;

import com.github.msteinbeck.sig4j.signal.Signal1;
import netscape.javascript.JSObject;
import org.ini4j.Ini;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Core extends Thread {
    private Signal1<List<String>> addNewEngine_signal;
    private Signal1<Map<UUID, String>> startNewScanTask_signal;
    private Signal1<Integer> removeEngines_signal;
    private Signal1<UUID> startCalculateResult_signal;

    private String rootDirectory;
    EngineHandler engineHandler;
    DBManager dbManager;
    CliHandler cliHandler;

    Map<UUID, JSObject> scanMap;

    private JSONObject calculateResult_slot(UUID uuid) {
        return null;
    }


    boolean init(String rootDirectory) {
        if (!rootDirectory.isEmpty()) {
            this.rootDirectory = rootDirectory;

            addNewEngine_signal = new Signal1<>();
            engineHandler = new EngineHandler();
            dbManager = new DBManager();
            cliHandler = new CliHandler();
            startNewScanTask_signal = new Signal1<>();
            removeEngines_signal = new Signal1<>();
            startCalculateResult_signal = new Signal1<>();

            startCalculateResult_signal.connect(this::calculateResult_slot);
            //startNewScanTask_signal.connect();

            if (!dbManager.init(rootDirectory + "\\db\\scanHistoryDB.sqlite")) {
                return false;
            }

            

            return true;
        }

        return false;
    }

    private void readSettings(String filePath) {
        Ini ini = new Ini();
        try {
            ini.load(new FileReader(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<String> strings = ini.keySet();

        for (int i = 0; i < strings.size(); i++) {
            String key = (String) strings.toArray()[i];
            List<String> childrenNames = Arrays.asList(ini.get(key).childrenNames());
            if (childrenNames.contains("path") && childrenNames.contains("scan_parameter")) {

            }

        }
    }
}
