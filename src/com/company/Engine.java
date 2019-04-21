package com.company;

import com.github.msteinbeck.sig4j.Type;
import com.github.msteinbeck.sig4j.signal.Signal0;
import com.github.msteinbeck.sig4j.signal.Signal2;
import org.json.simple.JSONObject;

import java.util.*;

public class Engine extends Thread {

    int id;
    String enginePath;
    String scanPath;

    private Map<UUID, WorkerThread> engineProcesses;
    public Signal2<UUID, JSONObject> engineResult_signal;
    public Signal0 deletingDone_signal;

    public Engine(int id, String enginePath, String scanPath) {
        this.enginePath = enginePath;
        this.id = id;
        this.scanPath = scanPath;
        engineResult_signal = new Signal2<>();
        engineProcesses = new HashMap<>();
    }

    public void run() {
        super.run();
    }

    public void addNewWorker_slot(UUID uuid, String parameter) {
        if (!parameter.isEmpty()) {
            //System.out.println("Engine\t" + uuid.toString() + " " + parameter);
            List<String> paramList = new ArrayList<>();
            paramList.add(scanPath);
            paramList.add(parameter);

            WorkerThread workerThread = new WorkerThread(uuid, enginePath, paramList);

            engineProcesses.put(uuid, workerThread);
            workerThread.start();
            workerThread.processFinished_signal.connect(this::handlerProcessDone_slot, Type.QUEUED);
            workerThread.processStart_signal.emit();
        }
    }

    public void handlerProcessDone_slot(UUID uuid, JSONObject jsonObject) {
        if (!jsonObject.isEmpty()) {
            System.out.println("[Engine_" + id + "]\t" + uuid.toString());
            engineResult_signal.emit(uuid, jsonObject);
            engineProcesses.get(uuid).interrupt();
            engineProcesses.remove(uuid);
        }

    }

    public String getEnginePath() {
        return enginePath;
    }

    public void deleteEngine_slot() {
        int i = 0;
        while (i <= engineProcesses.size()) {
            engineProcesses.get(i).interrupt();
            engineProcesses.remove(i);

            i++;
        }
        deletingDone_signal.emit();
    }


}
