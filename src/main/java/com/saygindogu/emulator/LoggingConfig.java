package com.saygindogu.emulator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggingConfig {

    private LoggingConfig() {}

    public static void init() {
        var rootLogger = Logger.getLogger("com.saygindogu.emulator");
        rootLogger.setLevel(Level.ALL);
        try {
            var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            var fileHandler = new FileHandler("emulator_" + timestamp + ".log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            rootLogger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Could not initialize file logging: " + e.getMessage());
        }
    }
}
