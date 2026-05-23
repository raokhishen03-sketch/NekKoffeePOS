package com.nekkoffee;

public class AppLauncher {
    public static void main(String[] args) {
        // This tricks the JVM and forces it to load the Maven dependencies safely
        Main.main(args);
    }
}