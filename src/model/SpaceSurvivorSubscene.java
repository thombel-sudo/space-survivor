package model;

import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Клас для обробки панелі меню
 */
public class SpaceSurvivorSubscene extends SubScene {
    private final static  String FONT_PATH = "src/model/resources/UkrainianAdverGothic.ttf";
    private final static  String BACKGROUND_IMAGE = "/model/resources/table_02.png";

    private Boolean isHiden = true;

    /**
     * Ініціалізація класу
     */
    public SpaceSurvivorSubscene() {
        super(new AnchorPane(),600,400);
        prefWidth(600);
        prefHeight(400);
//        setLayoutX(0);
//        setLayoutY(0);
        BackgroundImage image = null;

        BackgroundSize backgroundSize = new BackgroundSize(600, 400, true, true, true, true);
        try {
            image = new BackgroundImage(new Image(getClass().getResource(BACKGROUND_IMAGE).toURI().toString()),
                    BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,backgroundSize);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //System.out.println(image.getImage().impl_getUrl());
        this.setEffect(new DropShadow(40,Color.BLACK));
        AnchorPane root2 = (AnchorPane) this.getRoot();
        root2.setBackground(new Background(image));

        //root2.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");

        setLayoutY(180);
        setLayoutX(1074);
    }

    /**
     * Переміщення панелі
     */
    public void moveSubscene(){

        TranslateTransition transition = new TranslateTransition();
        transition.setDuration(Duration.seconds(1));
        transition.setNode(this);
        if(isHiden) {
            transition.setToX(-716);
            isHiden = false;
        }else{
            transition.setToX(716);
            isHiden = true;
        }
        transition.play();
    }

    /**
     *
     * @return панель
     */
    public AnchorPane getPane(){
        return (AnchorPane) this.getRoot();
    }
//    public void hideSubscene(){
//        TranslateTransition transition = new TranslateTransition(Duration.seconds(1),this);
//        transition.setToX(696);
//        transition.play();
//    }
}
