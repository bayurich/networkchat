package ru.netology;


import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class MainClient1 {

    public static final String ROOT_PATH = "client/src/main/";

    private static final String CONFIG_PATH = ROOT_PATH + "resources/config.properties";
    private static final Properties property = new Properties();
    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");



    public static void main(String[] args) {
        initProperty();

        String host = getProperty("server.host");
        int port = Integer.parseInt(getProperty("server.port"));
        String logPath = ROOT_PATH + getProperty("log.path");

        new Client(host, port, logPath).start();
    }

    private static void initProperty(){
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            property.load(fis);
        } catch (IOException e) {
            log("Error while initProperty: " + e.getMessage());
        }

    }

    public static String getProperty(String name) {
        return property.getProperty(name);
    }

    public static void log(String mes){
        System.out.println(getData() + mes);
    }

    public static String getData(){
        return sdf.format(new Date());
    }
}
