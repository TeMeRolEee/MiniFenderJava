package com.company;

import com.github.msteinbeck.sig4j.signal.Signal0;
import com.github.msteinbeck.sig4j.signal.Signal1;

import java.util.Scanner;


public class CliHandler extends Thread {
    protected Signal1<String> newTask_signal;
    protected Signal0 stopCli_signal;

    boolean stopCli = false;

    void init() {
        newTask_signal = new Signal1<>();
        stopCli_signal = new Signal0();
        stopCli_signal.connect(this::stopCli_slot);
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (!stopCli) {
            //System.out.println("Waiting for input...");
            newTask_signal.emit(scanner.nextLine());
        }
        super.run();
    }

    void stopCli_slot() {
        stopCli = true;
    }
}
