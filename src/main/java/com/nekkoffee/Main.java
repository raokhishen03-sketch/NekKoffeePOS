package com.nekkoffee;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/main_menu.fxml"));
        // Setting a standard baseline resolution for a POS desktop app (e.g., 1280x800)
        Scene scene = new Scene(fxmlLoader.load(), 1280, 800);

        stage.setTitle("NekKoffee POS System");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}