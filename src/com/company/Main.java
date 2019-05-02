package com.company;

public class Main {

    private Core core;

    public static void main(String[] args) {
        new Main().start(args[0]);
    }

    private void start(String settingsFilePath) {
        core = new Core();
        core.start();
        core.init(settingsFilePath);
    }
}
