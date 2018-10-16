package com.microsoft.azure.cosmosdb.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {

    // CosmosDB Settings
    private static String CosmosDbEndPoint;
    private static String CosmosDbMasterKey;
    private static String cosmosDbDatabase;
    private static String cosmosDbDataCollection;
    private static int cosmosDbDataCollectionThroughput;
    private static String cosmosDbDataCollectionPkValue;
    private static int cosmosDbDataCollectionConnectionPoolSize;


    public static int getCosmosDbDataCollectionConnectionPoolSize() {
        return cosmosDbDataCollectionConnectionPoolSize;
    }

    static void initSettings() throws IOException {
        Properties settings = new Properties();
        InputStream propertiesInputStream =
                App.class.getClassLoader().getResourceAsStream("settings.properties");
        settings.load(propertiesInputStream);
        registerSettingValues(settings);
    }

    static String getCosmosDbEndPoint() {
        return CosmosDbEndPoint;
    }

    static String getCosmosDbMasterKey() {
        return CosmosDbMasterKey;
    }

    static String getCosmosDbDatabase() {
        return cosmosDbDatabase;
    }

    static String getCosmosDbDataCollection() {
        return cosmosDbDataCollection;
    }

    static int getCosmosDbDataCollectionThroughput() {
        return cosmosDbDataCollectionThroughput;
    }

    static String getCosmosDbDataCollectionPkValue() {
        return cosmosDbDataCollectionPkValue;
    }

    static void initUserSettings(String filePath) throws IOException {
        Properties settings = new Properties();
        File f = new File(filePath);
        InputStream targetStream = new FileInputStream(f);
        settings.load(targetStream);
        targetStream.close();
        registerSettingValues(settings);
    }

    private static void registerSettingValues(Properties settings) {
        // CosmosDB Settings
        Settings.CosmosDbEndPoint = settings.getProperty("cosmosDbEndPoint");
        Settings.CosmosDbMasterKey = settings.getProperty("cosmosDbMasterkey");
        Settings.cosmosDbDatabase = settings.getProperty("cosmosDbDatabase");

        Settings.cosmosDbDataCollection = settings.getProperty("cosmosDbDataCollection");
        Settings.cosmosDbDataCollectionThroughput =
                parseOrDefault(settings.getProperty("cosmosDbDataCollectionThroughput"), -1);
        Settings.cosmosDbDataCollectionPkValue = settings.getProperty("cosmosDbDataCollectionPkValue");

         Settings.cosmosDbDataCollectionConnectionPoolSize =
                parseOrDefault(settings.getProperty("cosmosDbDataCollectionConnectionPoolSize"), 200);

    }

    private static int parseOrDefault(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }


}
