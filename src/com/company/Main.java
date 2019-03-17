package com.company;
import com.github.msteinbeck.sig4j.*;

import java.io.File;

public class Main {

    Core core;

    public static void main(String[] args) {
        new Main().start();
    }

    private void start() {
        core = new Core();
        core.start();
        core.init(System.getProperty("user.dir"));
    }
}
