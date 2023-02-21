package ru.netology;

import java.io.PrintWriter;

public class User {

    private String name;
    private PrintWriter out;

    public User(String name, PrintWriter out) {
        this.name = name;
        this.out = out;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", out=" + out +
                '}';
    }
}
