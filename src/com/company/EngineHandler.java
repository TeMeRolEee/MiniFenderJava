package com.company;

import com.github.msteinbeck.sig4j.Type;
import com.github.msteinbeck.sig4j.signal.Signal2;
import com.github.msteinbeck.sig4j.slot.Slot2;
import com.github.msteinbeck.sig4j.slot.Slot3;
import org.json.simple.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.Vector;

public class EngineHandler {

    private Signal2<UUID, JSONObject> scanComlete_signal = new Signal2<>();
    private Signal2<UUID, String> newTask_sinal = new Signal2<>();

    private Slot2<UUID, JSONObject> handleEngineResult_slot;
    private Slot3<String, String, String> addNewEngine_slot;
    private Slot2<UUID, String> handleNewTask_slot;


    private Map<Integer, Engine> engineList;
    private Map<String, Integer> engineNameList;
    private Vector<Integer> resultMap;

    int engineCount = 0;
    Thread thread;


    public EngineHandler() {
    }

    public void run() {
        thread.run();
    }

    public void handlerEngineResult_solt(UUID uuid, JSONObject reuslt) {

        scanComlete_signal.emit(uuid,reuslt);
    }

    public void handlerNewTask_slot(UUID uuid, String file) {
        if (!file.isEmpty()) {
            newTask_sinal.emit(uuid,file);
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
        engine.engineResult_signal.connect(this::handleEngineResult_slot, Type.QUEUED);
        this.newTask_sinal.connect(engine::addNewWorker_slot);

        engineList.put(engineCount,engine);
        engineNameList.put(engineName, engineCount++);

        engine.start();
    }
}
