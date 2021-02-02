package model;

import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Shadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

/**
 * Клас для оброки кнопок
 */
public class SpaceSurvivorButton extends Button {
    private final String FONT_PATH = "/model/resources/UkrainianAdverGothic.ttf";

    private Image image;
    private final String BUTTON_PRESSED_STYLE = "-fx-background-color: transparent; -fx-background-image: url('/model/resources/blue_button_pressed.png');";
    private final String BUTTON_FREE_STYLE = "-fx-background-color: transparent; -fx-background-image: url('/model/resources/blue_button.png');";

    /**
     * Ініціалізація класу
     * @param text
     */
    public SpaceSurvivorButton(String text){
        setText(text); //встановлюємо текст
        setButtonFont(); //встановлюємо шрифт
        setPrefWidth(190); //ширина кнопки
        setPrefHeight(49); //висота кнопки

        try {
            image = new Image(getClass().getResource("/model/resources/yellow_button.png").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        BackgroundSize bg = new BackgroundSize(190,49,false, false,true,true);
        this.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,bg)));
        setEffect(new DropShadow(20,Color.BLACK));

        //setStyle(BUTTON_FREE_STYLE);
        initializeButtonListeners();
    }

    /**
     * Встоновлює шрифт тексту конпки
     */
    public void setButtonFont(){
        try {
            this.setFont(Font.loadFont(getClass().getResource(FONT_PATH).toURI().toString(),18));
            this.setTextFill(Color.CORNSILK);
        } catch ( URISyntaxException e) {
            this.setFont(Font.font("Verdana",23));
        }
    }

    /**
     * Зміщення кропки при натисканні
     */
    public void setButtonPressedStyle(){
        //setStyle(BUTTON_PRESSED_STYLE);
        //setPrefHeight(45);
        setLayoutY(getLayoutY() + 4);
    }

    /**
     * Повертання кнопки в початковий стан
     */
    public void setButtonReleasedStyle(){
        //setStyle(BUTTON_FREE_STYLE);
        //setPrefHeight(49);
        setLayoutY(getLayoutY()-4);
    }

    /**
     * Встановлення дії при натисканні кнопки
     */
    public void initializeButtonListeners(){
        setOnMousePressed(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                setButtonPressedStyle();
            }
        });

        setOnMouseReleased(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                setButtonReleasedStyle();
            }
        });

        setOnMouseEntered(mouseEvent -> {
             //setEffect(new ColorAdjust(0.3,0,0.3,0));
            setEffect(new DropShadow(30,Color.LIGHTBLUE));
        });

        setOnMouseExited(mouseEvent -> {
            setEffect(new DropShadow(20,Color.BLACK));
        });
    }
}
