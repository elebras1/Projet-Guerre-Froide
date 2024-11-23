package com.populaire.projetguerrefroide.util;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;
import java.io.IOException;

public class Logging {
    public static Logger getLogger(String className) {
        Logger logger = Logger.getLogger(className);
        try {
            FileHandler fileHandler = new FileHandler("projet-guerre-froide.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.INFO);
        } catch (SecurityException | IOException e) {
            logger.log(Level.SEVERE, "Error setting up file logger", e);
        }
        return logger;
    }
}
