package com.company;


import com.github.msteinbeck.sig4j.signal.Signal0;
import com.github.msteinbeck.sig4j.signal.Signal1;
import com.github.msteinbeck.sig4j.signal.Signal2;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WorkerThread extends Thread {

    private final UUID id;

    private final ProcessBuilder processBuilder;
    private Process process;

    private Signal0 processDone_signal;
    protected Signal0 processStart_signal;
    protected Signal2<UUID, JSONObject> processFinished_signal;

    public WorkerThread(UUID id, String enginePath, List<String> paramList) {
        this.id = id;

        processDone_signal = new Signal0();
        processStart_signal = new Signal0();
        processFinished_signal = new Signal2<>();

        processBuilder = new ProcessBuilder(enginePath, paramList.get(0), paramList.get(1));
        this.processDone_signal.connect(this::processDone_slot);

        this.processStart_signal.connect(this::startWorker_slot);
    }

    private void processDone_slot() {
        String tempString = process.getOutputStream().toString();
        JSONObject jsObject = null;

        JSONParser jsonParser = new JSONParser();
        try {
            jsObject = (JSONObject) jsonParser.parse(tempString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.processFinished_signal.emit(id, jsObject);
    }

    public void run() {
        super.run();
    }

    private void startWorker_slot() {
        try {
            process = processBuilder.start();
            process.waitFor(500, TimeUnit.MILLISECONDS);
            processDone_signal.emit();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

}
