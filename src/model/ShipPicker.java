package model;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Клас для обробки елементу вибору космоліта
 */
public class ShipPicker extends VBox {

    private ImageView circleImage;

    private ImageView shipImage;
    private String circleNotChoosen = "/view/resources/shipchooser/grey_circle.png";
    private String circleChoosen = "/view/resources/shipchooser/circle_choosen.png";
    private SHIP ship;
    private Boolean  isCircleChoosen;

    /**
     * Ініціалізація класу
     * @param ship
     */
    public ShipPicker(SHIP ship){

        try {
            circleImage = new ImageView(new Image(getClass().getResource(circleNotChoosen).toURI().toString())); //по стандарту устанавливаем не выбранный
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //System.out.println(circleImage.getImage().impl_getUrl());
        circleImage.setFitWidth(40);
        circleImage.setFitHeight(40);
        shipImage = new ImageView(ship.getUrlShip());
        this.ship = ship;
        isCircleChoosen = false;
        this.setAlignment(Pos.CENTER);
        this.setSpacing(20);
        this.getChildren().add(shipImage);
        this.getChildren().add(circleImage);

    }

    /**
     *
     * @return обраний космоліт
     */
    public SHIP getShip(){
        return ship;
    }

    /**
     *
     * @return чи був обраний космоліт
     */
    public boolean getIsCircleChoosen(){
        return isCircleChoosen;
    }

    /**
     * встановлення вибору космоліта
     * @param isCircleChoosen
     */
    public void setIsCircleChoosen(boolean isCircleChoosen){
        this.isCircleChoosen = isCircleChoosen;
        String imageToSet = this.isCircleChoosen ? circleChoosen : circleNotChoosen;
        try {
            circleImage.setImage(new Image(getClass().getResource(imageToSet).toURI().toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
