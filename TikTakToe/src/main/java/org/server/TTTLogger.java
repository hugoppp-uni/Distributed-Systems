package org.server;

import java.io.IOException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TTTLogger {
    private static final String lfPath = "/log/";
    private static Logger _logger = Logger.getLogger("TTTLog");
    public static final TTTLogger logger = new TTTLogger();

    public static void init(String file) {
        FileHandler fh;
        try {
            fh = new FileHandler(new java.io.File(".").getCanonicalPath() + lfPath + file);
            _logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            _logger.log(Level.INFO, "Logger initialized");
        } catch (Exception e) {
            _logger.log(Level.WARNING, "Exception: " + e + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public void log(Level level, String msg) {

        String ip = null;
        try {
            ip = RemoteServer.getClientHost();

        } catch (ServerNotActiveException ignored) {
        }
        if (ip == null)
            _logger.log(level, msg);
        else
            _logger.log(level, "[" + ip + "] " + msg);

    }
}
