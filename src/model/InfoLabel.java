package model;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;


/**
 * Клас для обробки написів
 */
public class InfoLabel extends Label {

    public final static String FONT_PATH = "/model/resources/UkrainianAdverGothic.ttf";//Шрифт
    public final static String BACKGROUND_PATH = "view/resources/yellow_small_panel.png";//не використовується

    /**
     * Ініціалізація класу
     * @param text
     */
    public InfoLabel(String text){
        setPrefWidth(380);//ширина тексту
        setPrefHeight(49);//висота тексту
        //setPadding(new Insets(40,40,40,40));
        setText(text);//встановлення переданого тексту
        setWrapText(true); //перенос на наступну строку, якщо напис не вміщається
        setLabelFont(); //встановлюємо шрифт
        setAlignment(Pos.CENTER); //центруємо текст
        this.setTextFill(Color.CORNSILK); //встановлюємо колік тексту
        setEffect(new DropShadow(5,Color.BLACK)); //встановлюємо еффект тіні

        //BackgroundImage backgroundImage = new BackgroundImage(
        //        new Image(BACKGROUND_PATH,300,49,false,true),
         //       BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,null);
        //setBackground(new Background(backgroundImage)) ;
    }

    /**
     * Встановлення шрифту
     */
    public void setLabelFont(){
        try {
            setFont(Font.loadFont(getClass().getResource(FONT_PATH).toURI().toString(),23));
        } catch (URISyntaxException e) {
            setFont(Font.font("Verdand",23));
        }

    }
}
