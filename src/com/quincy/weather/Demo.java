package com.quincy.weather;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Demo extends Application {
    public static Stage stage;
    public static Scene primaryScene;
    @Override
    public void start(Stage stage) throws Exception {
        Demo.stage = stage;
        FXMLLoader loader=new FXMLLoader();
        loader.setLocation(getClass().getResource("QuincyWeatherForecast.fxml"));
        AnchorPane root = loader.load();
        Scene scene = new Scene(root);
        primaryScene=scene;
        DemoController controller=loader.getController();
        controller.bindHotKey(scene);
        stage.setScene(scene);
        stage.setTitle("Quincy Zhang的天气预报小程序");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        stage.setResizable(false);
        stage.show();
    }
}