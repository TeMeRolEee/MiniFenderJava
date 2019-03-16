package com.company;

import netscape.javascript.JSObject;
import org.ini4j.Ini;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Core extends Thread {
    private String rootDirectory;
    EngineHandler engineHandler;
    DBManager dbManager;
    CliHandler cliHandler;

    Map<UUID, JSObject> scanMap;

    private JSONObject calculateResult(UUID uuid) {
        return null;
    }


    boolean init(String rootDirectory) {
        this.rootDirectory = rootDirectory;

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

    }
}
