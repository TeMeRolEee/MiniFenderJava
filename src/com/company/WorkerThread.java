package com.company;


import com.github.msteinbeck.sig4j.signal.Signal1;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class WorkerThread extends Thread {

    private final UUID id;
    private final String enginePath;
    private final List<String> paramList;
    final ProcessBuilder processBuilder;
    private Process process;
    private Thread thread = new Thread();

    private final Signal1<String> signal1 = new Signal1<>();

    public WorkerThread(UUID id, String enginePath, List<String> paramList) {
        this.enginePath = enginePath;
        this.id = id;
        this.paramList = paramList;

        processBuilder = new ProcessBuilder(enginePath, paramList.get(0), paramList.get(1));
        this.signal1.connect(this::processDone_slot);

    }

    private void processDone_slot(String s) {
        String tempString = process.getOutputStream().toString();
    }

    public void run() {
        thread.run();
    }

    public void startWorker_slot() {
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
