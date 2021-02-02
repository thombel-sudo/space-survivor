package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.ViewManager;

/**
 * Головний клас програми
 */
public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        ViewManager manager = new ViewManager(); //тут весь контент
        primaryStage = manager.getMainStage(); //получаем окно
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
