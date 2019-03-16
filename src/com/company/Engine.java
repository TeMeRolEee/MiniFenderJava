package com.company;

import com.github.msteinbeck.sig4j.signal.Signal1;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Engine extends Thread {

    int id;
    String enginePath;
    String scanPath;

    private Map<UUID, WorkerThread> engineProcesses;
    public Signal1<Map<UUID, JSONObject>> engineResult_signal;
    public Signal1<Integer> deletingDone_signa;
    private ProcessBuilder processBuilder;
    private Process process;
    private Thread thread = new Thread();


    public Engine(int id, String enginePath, String scanPath) {

        this.enginePath = enginePath;
        this.id = id;
        this.scanPath = scanPath;

    }

    public void run() {
        thread.run();
    }

    public void addNewWorker_slot(UUID uuid, String parameter) {
        if (!parameter.isEmpty()) {

            List<String> paramList = null;
            paramList.add(scanPath);
            paramList.add(parameter);

            WorkerThread workerThread = new WorkerThread(uuid, enginePath, paramList);

            engineProcesses.put(uuid, workerThread);
            workerThread.start();
            workerThread.processFinished_signal.connect(this::handlerProcessDone_slot);
            workerThread.processStart_signal.emit(1);

        }
    }

    public void handlerProcessDone_slot(Map<UUID, JSONObject> resultMap) {
        engineResult_signal.emit(resultMap);
        if (!resultMap.isEmpty()) {
            UUID uuid = (UUID) resultMap.keySet().toArray()[0];
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
        deletingDone_signa.emit(1);
    }


}
