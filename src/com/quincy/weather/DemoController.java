package com.quincy.weather;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DemoController {
    WeatherUtil util;
    ArrayList<String> provinceHtml;
    String[] citiesHTML;
    HashMap<Integer, String> townURL = new HashMap<>();
    String finalURL;

    @FXML
    private ComboBox<String> city;

    @FXML
    private TableColumn<WeatherInformation, String> columnDate;

    @FXML
    private TableColumn<WeatherInformation, String> columnPhenomenon;

    @FXML
    private TableColumn<WeatherInformation, String> columnTemperature;

    @FXML
    private TableColumn<WeatherInformation, String> columnWindDirection;

    @FXML
    private TableColumn<WeatherInformation, String> columnWindPower;

    @FXML
    private Button getWeather;

    @FXML
    private ComboBox<String> province;

    @FXML
    private ComboBox<String> town;

    @FXML
    private TableView<WeatherInformation> weatherChart;

    public void initialize() {
//        getWeather.getStylesheets().add(getClass().getResource("button.css").toExternalForm());
//        getWeather.getStyleClass().add("button");
        Label initLabel = new Label("Initializing...");
        initLabel.setFont(Font.font(40));
        Scene scene = new Scene(initLabel);
        Demo.stage.setScene(scene);
        Thread initDataThread = new Thread(() -> {
            try {
                util = new WeatherUtil();
                provinceHtml = util.getAllProvinces(true);
                ArrayList<String> ALLprovinces = util.getAllProvinces(false);
                for (String tmp : ALLprovinces) {
                    Platform.runLater(() -> province.getItems().add(tmp));
                }
            } catch (IOException e) {
                Alert errBox = new Alert(Alert.AlertType.ERROR);
                errBox.setContentText("获取服务器数据异常！");
                errBox.initModality(Modality.APPLICATION_MODAL);
                errBox.show();
                Platform.exit();
            }
            Platform.runLater(() -> Demo.stage.setScene(Demo.primaryScene));
        });
        initDataThread.start();
        columnDate.setCellValueFactory(cellData -> cellData.getValue().Date);
        columnPhenomenon.setCellValueFactory(cellData -> cellData.getValue().Phenomenon);
        columnTemperature.setCellValueFactory(cellData -> cellData.getValue().Temperature);
        columnWindDirection.setCellValueFactory(cellData -> cellData.getValue().WindDirection);
        columnWindPower.setCellValueFactory(cellData -> cellData.getValue().WindPower);
        province.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (t1.intValue() == -1) {
                    return;
                }
                city.setDisable(false);
                city.getItems().clear();
                town.getItems().clear();
                town.setDisable(true);
                getWeather.setDisable(true);
                weatherChart.getItems().clear();

                String selectedHTML = provinceHtml.get(t1.intValue());
                citiesHTML = selectedHTML.split("详情");
                for (String tmp : citiesHTML) {
                    String assistantString = tmp.replaceAll("<a href=\"http://www.weather.com.cn/weather/\\d*.shtml\" target=\"_blank\">", "[Quincy]");
                    int beginIndex = assistantString.indexOf("[Quincy]") + "[Quincy]".length();
                    int endIndex = assistantString.indexOf("</a></td>", beginIndex);
                    if (beginIndex != "[Quincy]".length() - 1 && endIndex != -1 && beginIndex < endIndex) {
                        city.getItems().add(assistantString.substring(beginIndex, endIndex));
                    }
                }
            }
        });
        city.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (t1.intValue() == -1) {
                    return;
                }
                town.setDisable(false);
                town.getItems().clear();
                townURL.clear();
                getWeather.setDisable(true);
                weatherChart.getItems().clear();
                String currentCityHtml = citiesHTML[t1.intValue()];
                int citySeriesNumber = Integer.parseInt(WeatherUtil.getMidString(currentCityHtml,"<a href=\"http://www.weather.com.cn/weather/",".shtml"));
                String cityName = city.getItems().get(t1.intValue());
                Thread get = new Thread(() -> {
                    int i = 0;
                    while (true) {
                        String currentURL = "http://www.weather.com.cn/weather/" + (citySeriesNumber + i) + ".shtml";
                        String currentHTML;
                        try {
                            currentHTML = WeatherUtil.getHTML(currentURL);
                            if (currentHTML.equals("<!-- empty -->")) {
                                break;
                            } else {
                                String key = cityName + "</a><span>></span>";
                                int indexWithoutPrefix = currentHTML.indexOf(key) + key.length();
                                int beginIndex = currentHTML.indexOf("<span>", indexWithoutPrefix) + "<span>".length();
                                int endIndex = currentHTML.indexOf("</span>", indexWithoutPrefix);
                                String townName = currentHTML.substring(beginIndex, endIndex);
                                townURL.put(i, currentURL);
                                Platform.runLater(() -> town.getItems().add(townName));
                                i++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (town.getItems().size() == 1) {
                        Platform.runLater(() -> town.getItems().set(0, "城区"));
                    }
                });
                get.start();

            }
        });
        town.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (t1.intValue() == -1) {
                    return;
                }
                finalURL = townURL.get(t1.intValue());
                getWeather.setDisable(false);
                weatherChart.getItems().clear();
            }
        });
        getWeather.setOnAction(e -> {
            try {
                weatherChart.getItems().clear();
                String html = WeatherUtil.getHTML(finalURL);
                html = html.split("id=\"update_time\"")[1];
                String[] days = html.split("<div class=\"slid\"></div>");
                for (int i = 0; i < 7; i++) {
                    String currentDayHtml = days[i];
                    currentDayHtml = currentDayHtml.replaceAll("<p title=\".*\" class=\"wea\">", "[QuincyPnmn]");
                    String date = WeatherUtil.getMidString(currentDayHtml, "<h1>", "</h1>");

                    String phenomenon = WeatherUtil.getMidString(currentDayHtml, "[QuincyPnmn]", "</p>");

                    String temperatureHTML = WeatherUtil.getMidString(currentDayHtml, "<p class=\"tem\">", "</p>");
                    temperatureHTML = temperatureHTML.replaceAll("</?span>", "");
                    temperatureHTML = temperatureHTML.replaceAll("</?i>", "");
                    String temperature;
                    int splitterIndex = temperatureHTML.indexOf('/');
                    if (splitterIndex == -1) {
                        temperature = temperatureHTML;
                    } else {
                        String lowestT = temperatureHTML.substring(splitterIndex + 1);
                        String highestT = temperatureHTML.substring(0, splitterIndex);
                        temperature = lowestT + "~" + highestT;
                        if (!temperature.endsWith("℃")) {
                            temperature += "℃";
                        }
                    }
                    temperature = temperature.replaceAll("\\s", "");

                    String WindHTML = WeatherUtil.getMidString(currentDayHtml, "<em>", "</em>");
                    String[] winds = WindHTML.split("</span>");
                    for (int j = 0; j < winds.length - 1; j++) {
                        String tmpWind = winds[j];
                        winds[j] = WeatherUtil.getMidString(tmpWind, "<span title=\"", "\"");
                    }
                    String windDirection = "";
                    if (winds.length == 2 || (winds.length > 2 && winds[0].equals(winds[1]))) {
                        windDirection = winds[0];
                    } else if (!(winds.length == 0)) {
                        windDirection = winds[0] + "转" + winds[1];
                    }

                    int beginIndex = currentDayHtml.indexOf("<i>", currentDayHtml.indexOf("win")) + "<i>".length();
                    int endIndex = currentDayHtml.indexOf("</i>", beginIndex);
                    String windPower = currentDayHtml.substring(beginIndex, endIndex);

                    WeatherInformation information = new WeatherInformation(date, phenomenon, temperature, windDirection, windPower);
                    weatherChart.getItems().add(information);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }
    public void bindHotKey(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER), ()->getWeather.fire());
    }
}

class WeatherInformation {
    public SimpleStringProperty Date = new SimpleStringProperty();
    public SimpleStringProperty Phenomenon = new SimpleStringProperty();
    public SimpleStringProperty Temperature = new SimpleStringProperty();
    public SimpleStringProperty WindDirection = new SimpleStringProperty();
    public SimpleStringProperty WindPower = new SimpleStringProperty();

    public WeatherInformation(String date, String phenomenon, String temperature, String windDirection, String windPower) {
        Date.set(date);
        Phenomenon.set(phenomenon);
        Temperature.set(temperature);
        WindDirection.set(windDirection);
        WindPower.set(windPower);
    }
}