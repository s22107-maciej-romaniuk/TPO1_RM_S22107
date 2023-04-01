package zad2;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;


import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.json.*;

import java.io.IOException;
import java.util.Set;

public class Controller {
    public TextArea weatherContent;
    public TextArea currencyRateContent;
    public TextArea PLNratioContent;
    public WebView wikiPage;

    private final Service service;

    public SimpleStringProperty weatherValue = new SimpleStringProperty();
    public SimpleStringProperty currencyRateValue = new SimpleStringProperty();
    public SimpleStringProperty PLNratioValue = new SimpleStringProperty();

    public Controller(){
        this.service = new Service("Poland");
    }

    @FXML
    private void initialize(){
        this.weatherContent.textProperty().bind(weatherValue);
        this.currencyRateContent.textProperty().bind(currencyRateValue);
        this.PLNratioContent.textProperty().bind(PLNratioValue);
    }

    public String getCityLink(){
        if(this.service.state != null) return String.format("https://en.wikipedia.org/wiki/%s,_%s", this.service.city, this.service.state);
        else return String.format("https://en.wikipedia.org/wiki/%s", this.service.city);
    }



    public void showModal(ActionEvent actionEvent){
        Stage dialogStage = new Stage();


        Label countryLabel = new Label("Country:");
        Label cityLabel = new Label("City:");
        Label currencyLabel = new Label("Currency code:");
        TextField countryTextField = new TextField("Poland");
        TextField cityTextField = new TextField("Warsaw");
        TextField currencyTextField = new TextField("USD");
        GridPane dataGrid = new GridPane();
        Button confirmButton = new Button("Confirm");
        dataGrid.add(countryLabel, 0, 0);
        dataGrid.add(cityLabel, 0, 1);
        dataGrid.add(currencyLabel, 0, 2);
        dataGrid.add(countryTextField, 1, 0);
        dataGrid.add(cityTextField, 1, 1);
        dataGrid.add(currencyTextField, 1, 2);
        dataGrid.add(confirmButton, 1, 3);
        countryLabel.setTextAlignment(TextAlignment.RIGHT);
        cityLabel.setTextAlignment(TextAlignment.RIGHT);
        currencyLabel.setTextAlignment(TextAlignment.RIGHT);
        GridPane.setHalignment(countryLabel, HPos.RIGHT);
        GridPane.setHalignment(cityLabel, HPos.RIGHT);
        GridPane.setHalignment(currencyLabel, HPos.RIGHT);
        GridPane.setMargin(countryLabel, new Insets(5, 5, 5, 5));
        GridPane.setMargin(cityLabel, new Insets(5, 5, 5, 5));
        GridPane.setMargin(currencyLabel, new Insets(5, 5, 5, 5));


        //Creating a scene object
        Scene scene = new Scene(new Group(dataGrid));
        dialogStage.initOwner(((Node) actionEvent.getTarget()).getScene().getWindow());
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Change settings");
        dialogStage.setOnCloseRequest(Event::consume);
        dialogStage.setScene(scene);


        confirmButton.setOnAction( event -> {
            try {
                this.service.getCountryCode(countryTextField.getText());
            }
            catch(Exception ex){
                this.weatherValue.set("An error occured during country load.");
                this.currencyRateValue.set("An error occured during country load.");
                this.PLNratioValue.set("An error occured during country load.");
                dialogStage.close();
                return;
            }
            Set<City> cities;
            try {
                cities = this.service.getGeolocation(cityTextField.getText());
            } catch (Exception e) {
                this.weatherValue.set("An error occured during city load.");
                dialogStage.close();
                return;
            }
            if(cities != null && cities.size() > 1){
                Stage stateStage = new Stage();
                ListView<City> listView = new ListView<>();
                listView.getItems().addAll(cities);
                Button cityPickButton = new Button("Pick city");
                cityPickButton.setPrefWidth(Double.MAX_VALUE);
                VBox vbox = new VBox(listView, cityPickButton);
                vbox.setMaxWidth(800);
                vbox.setMinWidth(800);
                vbox.setPrefWidth(800);
                vbox.setFillWidth(true);
                stateStage.setResizable(false);
                cityPickButton.setOnAction( pickEvent -> {
                    ObservableList<Integer> list =  listView.getSelectionModel().getSelectedIndices();
                    if(list.size() > 0) {
                        Integer selectionIndex = list.get(0);

                        City selectedCity = listView.getItems().get(selectionIndex);

                        this.service.city = selectedCity.cityName;
                        this.service.countryCode = selectedCity.countryCode;
                        this.service.state = selectedCity.state;
                        this.service.latitude = selectedCity.lat;
                        this.service.longitude = selectedCity.lon;

                        stateStage.close();
                    }
                });
                Scene stateScene = new Scene(new Group(vbox));
                stateStage.initOwner(scene.getWindow());
                stateStage.initModality(Modality.APPLICATION_MODAL);
                stateStage.setTitle("City picker");
                stateStage.setScene(stateScene);
                stateStage.setOnCloseRequest(Event::consume);
                stateStage.showAndWait();
            }
            this.service.getWeatherWithCurrentCountryAndState();
            this.service.getRateFor(currencyTextField.getText());
            this.service.getNBPRate();


            JSONObject weather = new JSONObject(this.service.weather);
            String location = this.service.city;
            String sky = weather.getJSONArray("weather").getJSONObject(0).getString("main");
            String temperature = String.valueOf(weather.getJSONObject("main").getDouble("temp"));
            String pressure = String.valueOf(weather.getJSONObject("main").getDouble("pressure"));
            String humidity = String.valueOf(weather.getJSONObject("main").getDouble("humidity"));
            String wind = String.valueOf(weather.getJSONObject("wind").getDouble("speed"));
            String weatherValue =
                    "Location: " + location + '\n' +
                    "Sky: " + sky + '\n' +
                    "Temperature: " + temperature + '\n' +
                    "Pressure: " + pressure + '\n' +
                    "Humidity: " + humidity + '\n' +
                    "Wind: " + wind
                    ;
            this.weatherValue.set(weatherValue);
            if(this.service.currencyRate != -1.0) this.currencyRateValue.set(String.valueOf(this.service.currencyRate));
            else this.currencyRateValue.set("An error occured while loading exchange rate for provided currency");
            if(this.service.nbpRate != -1.0) this.PLNratioValue.set(String.valueOf(this.service.nbpRate));
            else this.PLNratioValue.set("An error occured while loading exchange rate for provided currency");
            this.wikiPage.getEngine().load(getCityLink());

            dialogStage.close();
        });
        dialogStage.showAndWait();
    }
}
