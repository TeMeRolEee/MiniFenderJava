package com.company;


import java.util.List;
import java.util.UUID;

public class WorkerThread extends Thread {

    private final UUID id;
    private final String enginePath;
    private final List<String> paramList;

    public WorkerThread(UUID id, String enginePath, List<String> paramList, List<String> paramList1) {
        this.enginePath = enginePath;
        this.id = id;
        this.paramList = paramList1;
    }

    @Override
    public void run() {
        super.run();
    }

}
