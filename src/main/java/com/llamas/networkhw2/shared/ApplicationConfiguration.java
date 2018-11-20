package com.llamas.networkhw2.shared;

/**
 * Basic configuration variables for the application
 */
public final class ApplicationConfiguration {
    private static volatile ApplicationConfiguration _instance = null;


    private ApplicationConfiguration() {

    }

    /**
     * Get the singleton configuration instance
     */
    public static ApplicationConfiguration getInstance() {
        if (_instance == null) {
            synchronized (ApplicationConfiguration.class) {
                if (_instance == null) {
                    _instance = new ApplicationConfiguration();
                }
            }
        }
        return _instance;
    }




 
}
