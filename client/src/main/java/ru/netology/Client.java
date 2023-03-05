package ru.netology;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.netology.MainClient.*;

public class Client extends Thread {
    static AtomicBoolean flag = new AtomicBoolean(true);
    String host;
    int port;
    String logPath;
    static FileWriter writer;

    public Client(String host, int port, String logPath) {
        this.host = host;
        this.port = port;
        this.logPath = logPath;
    }

    @Override
    public void run() {
        try {
            writer = new FileWriter(logPath, true);

            logf(null);
            Socket clientSocket = new Socket(host, port);
            logf("Start session");

            new Thread(clientRead(clientSocket)).start();
            new Thread(clientWrite(clientSocket)).start();
        } catch (IOException e) {
            log("IOException: " + e.getMessage());
        } catch (Exception e) {
            log("Client socket error: " + e.getMessage());
        }
    }

    private static Runnable clientRead(Socket clientSocket) {

        return () -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));) {

                while (true) {
                    if (in.ready()) {
                        String response = in.readLine();
                        log("received: " + response);
                        logf("received: " + response);
                    }
                    if (!flag.get()) {
                        clientSocket.close();
                        writer.close();
                        break;
                    }
                }

            } catch (Exception e) {
                logf("ClientRead error: " +e.getMessage());
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
                    logf("sent: " + msg);

                    if ("exit".equalsIgnoreCase(msg)) {
                        log("END-SESSION signal send");
                        logf("END-SESSION signal send");
                        scanner.close();
                        flag.set(false);
                        break;
                    }
                }

            } catch (Exception e) {
                logf("ClientRead error: " +e.getMessage());
            }
        };
    }

    private static void logf(){
        logf(null);
    }

    private static void logf(String mes){
        try {
            writer.append(mes == null ? "" : getData() + mes).append("\n");
            writer.flush();
        } catch (IOException e) {
            log("Error while logf: " + e.getMessage());
        }
    }
}
