package ru.netology;


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {

    public static final String ROOT_PATH = "server/src/main/";

    static final String CONFIG_PATH = ROOT_PATH + "resources/config.properties";
    static final Properties property = new Properties();
    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

    static final String SERVER_NAME = "SERVER";
    private static final String UNKNOWN_NAME = "UNKNOWN USER";
    static final String NAME_PATTERN = "^[a-zA-Z0-9]{3,}$";


    private static final ConcurrentMap<String, User> users = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        initProperty();

        final int port = Integer.parseInt(getProperty("server.port"));
        final ExecutorService threadPool = Executors.newFixedThreadPool(64);


        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log(String.format("Server started on port: %d", port));
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    log(String.format("New connection accepted. Port: %d", clientSocket.getPort()));
                    //threadPool.submit(new ServerRunnable(clientSocket));
                    //и так и так работает
                    threadPool.submit(new ServerThread(clientSocket));
                } catch (Exception e) {
                    log("Server error while connection: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log("Server error: " + e.getMessage());
        }


    }


    private static class ServerThread extends Thread {
        Socket clientSocket;

        public ServerThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        private String userName;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);) {

                sendMessage("You are successfully connect to server", out);
                sendMessage("Please write your name", out);

                // connect to chat
                String name;
                while (true) {
                    name = in.readLine();
                    if (name == null || name.length() < 3 || name.length() > 15) {
                        sendMessage("Incorrect name. Sorry, your name must be between 3 and 15 characters long", out);
                        sendMessage("Please choose another name", out);
                        continue;
                    }
                    if (!checkName(name)) {
                        sendMessage("Incorrect name. Sorry, only letters (a-z), numbers (0-9) are allowed", out);
                        sendMessage("Please choose another name", out);
                        continue;
                    }
                    if (isFindUser(name)) {
                        String response = String.format("Name, '%s' is used already. Please choose another name", name);
                        sendMessage(response, out);
                        continue;
                    }
                    addUser(name, out);

                    break;
                }

                while (true) {
                    String inMess = in.readLine();
                    log("from: " + name + ": " + inMess);
                    if (StringUtils.isBlank(inMess)) {
                        sendMessage("Your message is empty. Please send correct message", out);
                        continue;
                    }
                    inMess = inMess.trim();
                    if ("exit".equalsIgnoreCase(inMess)) {
                        log("from: " + name + ": END-SESSION signal received");
                        deleteUser(out);
                        break;
                    }

                    // всем
                    send(name, inMess);
                }
            } catch (Exception e) {
                log("ServerThread error: " + Arrays.toString(e.getStackTrace()));
            } finally {
                try {
                    clientSocket.close(); // закрываем сокет клиента
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean checkName(String name) {
            return name != null && name.matches(NAME_PATTERN);
        }

        private void addUser(String name, PrintWriter out){
            users.put(name.toLowerCase(), new User(name, out));
            setUserName(name);
            String response = String.format("Wellcome to chat, %s!", name);
            sendMessage(response, out);
            sendMessage("Your messages will be send to all chat users", out);

            if (users.size() == 1) {
                sendMessage("You are the only user of chat. Waiting for another users... :))", out);
            }
            else {
                //сообщение о входе нового пользователя
                send(SERVER_NAME, "LOGON user: " + name, name);
                //всем статистика
                send(SERVER_NAME, "Number of chat users: " + users.size());
            }

            log("LOGON user: " + name);
        }

        private void deleteUser(PrintWriter out){
            users.remove(getUserName().toLowerCase());

            String response = String.format("Goodbye, %s! See you later", getUserName());
            sendMessage(response, out);

            //сообщить всем о выходе
            response = "LOGOUT user: " + getUserName();
            send(SERVER_NAME, response);
            log(response);

            //всем статистика
            response = users.size() != 1 ? "Number of chat users: " + users.size() : "You are the only user of chat. Waiting for another users... :))";
            send(SERVER_NAME, response);
        }

        private boolean isFindUser(String name){
            return users.containsKey(name.toLowerCase()) || SERVER_NAME.equalsIgnoreCase(name);
        }

        private void send(String sender, String message) {
            send(sender, message, null);
        }

        private synchronized void send(String sender, String message, String excludeName) {
            sender = sender == null ? "" : sender;
            for (Map.Entry<String, User> entry : users.entrySet()) {
                String name = entry.getKey();
                if (name.equalsIgnoreCase(sender) || (excludeName != null && name.equalsIgnoreCase(excludeName))) {
                    continue;
                }
                sendMessage(sender, message, entry.getValue().getOut());
            }
        }

        private void sendMessage(String sender, String message, PrintWriter out) {
            out.println("From " + (StringUtils.isBlank(sender) ? UNKNOWN_NAME : sender + ": " + message));
            out.flush();
        }

        private void sendMessage(String message, PrintWriter out) {
            sendMessage(SERVER_NAME, message, out);
        }
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
