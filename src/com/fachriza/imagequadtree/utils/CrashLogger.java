package com.fachriza.imagequadtree.utils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;

public class CrashLogger {
    public static void logException(Exception e) {
        try {
            // Get the root folder
            Path jarDir = Paths.get(CrashLogger.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParent()
                    .getParent();

            Path logDir = jarDir.resolve("log");
            Files.createDirectories(logDir);

            Path logFile = logDir.resolve("crash.log");

            try (PrintWriter pw = new PrintWriter(new FileWriter(logFile.toFile(), true))) {
                pw.println("---- CRASH LOG ----");
                pw.println("Timestamp: " + java.time.LocalDateTime.now());
                e.printStackTrace(pw);
                pw.println();
            }

        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    }
}
