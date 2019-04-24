package com.company;

import com.github.msteinbeck.sig4j.Type;
import com.github.msteinbeck.sig4j.signal.Signal2;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EngineHandler extends Thread {

    protected Signal2<UUID, JSONObject> scanComplete_signal;
    private Signal2<UUID, String> newTask_signal;

    private Map<Integer, Engine> engineList;
    private Map<String, Integer> engineNameList;

    private int engineCount = 0;


    public EngineHandler() {
        scanComplete_signal = new Signal2<>();
        newTask_signal = new Signal2<>();
        engineList = new HashMap<>();
        engineNameList = new HashMap<>();
    }

    @Override
    public void run() {
        super.run();
    }

    public void handleEngineResult_slot(UUID uuid, JSONObject result) {
        //System.out.println("[EngineHandler]\t" + uuid.toString());
        scanComplete_signal.emit(uuid,result);
    }

    public void handlerNewTask_slot(UUID uuid, String file) {
        if (!file.isEmpty()) {
            newTask_signal.emit(uuid,file);
            //System.out.println("EngineHandler " + file);
        }
    }

    public boolean findExistingEngine(String engineName) {

        if (!engineName.isEmpty()) {
            return engineNameList.containsValue(engineName);
        }
        return false;
    }

    public int getEngineCount(){
        return engineList.size();
    }

    public void addNewEngine_slot(String enginePath, String scanParameters, String engineName) {
        Engine engine = new Engine(engineCount, enginePath, scanParameters);
        engineList.put(engineCount, engine);
        engineNameList.put(engineName, engineCount++);

        try {
            engine.engineResult_signal.connect(this::handleEngineResult_slot, Type.QUEUED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.newTask_signal.connect(engine::addNewWorker_slot, Type.QUEUED);

        engine.start();
        //System.out.println("Engine" + (engineCount-1) + " " + engineName);
    }
}
