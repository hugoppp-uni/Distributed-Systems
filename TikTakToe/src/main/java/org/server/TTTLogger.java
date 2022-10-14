package org.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TTTLogger {
    private static final String lfPath = "/TikTakToe/log/ttt.log";
    public static Logger logger = Logger.getLogger("TTTLog");

    public static void init() {
        FileHandler fh;
        try {
            fh = new FileHandler(new java.io.File(".").getCanonicalPath() + lfPath);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.log(Level.INFO, "Logger initialized");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception: " + e + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }
}
