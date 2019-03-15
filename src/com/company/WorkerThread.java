package com.company;


import com.github.msteinbeck.sig4j.signal.Signal1;

import java.util.List;
import java.util.UUID;

public class WorkerThread extends Thread {

    private final UUID id;
    private final String enginePath;
    private final List<String> paramList;
    final ProcessBuilder process;

    private final Signal1<String> signal1 = new Signal1<>();

    public WorkerThread(UUID id, String enginePath, List<String> paramList) {
        this.enginePath = enginePath;
        this.id = id;
        this.paramList = paramList;

        process = new ProcessBuilder(enginePath.toString(),paramList.get(0).toString(),paramList.get(1).toString());
        this.signal1.connect(this::processDone_slot);
    }

    private void processDone_slot(String s) {

    }
}
