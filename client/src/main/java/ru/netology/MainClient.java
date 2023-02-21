package ru.netology;


import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

public class MainClient {

    private static final String CONFIG_PATH = "client/src/main/resources/config.properties";
    private static final String LOG_PATH = "client/src/main/out/log.txt";
    private static final Properties property = new Properties();
    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

    public static void main(String[] args) {
        initProperty();

        final String host = getProperty("server.host");
        final int port = Integer.parseInt(getProperty("server.port"));


        try {
            Socket clientSocket = new Socket(host, port);

            new Thread(clientRead(clientSocket)).start();
            new Thread(clientWrite(clientSocket)).start();
        } catch (Exception e) {
            log("Client socket error: " +e.getMessage());
        }





    }

    private static Runnable clientRead(Socket clientSocket) {

        return () -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));) {

                while (true) {
                    String response = in.readLine();
                    log("response: " + response);

                    if ("exit".equalsIgnoreCase(response)) {
                        log("session end signal received");
                        break;
                    }
                }

            } catch (Exception e) {
                log("ClientRead error: " +e.getMessage());
            }
        };
    }

    private static Runnable clientWrite(Socket clientSocket) {
        Scanner scanner = new Scanner(System.in);

        return () -> {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);) {

                while (true) {
                    String msg = scanner.nextLine();
                    out.println(msg);

                    if ("exit".equalsIgnoreCase(msg)) {
                        log("session end signal send");
                        break;
                    }
                }

            } catch (Exception e) {
                log("ClientRead error: " +e.getMessage());
            }
        };
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
        System.out.println(sdf.format(new Date()) + mes);
    }
}
