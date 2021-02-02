package model;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.Serializable;
import java.net.URISyntaxException;

/**
 * Клас для обробки знарядів (пуль)
 */
public class Bullet extends ImageView implements Serializable {
    private boolean isDestroyed;
    private String enemyPicture = "/view/resources/laserEnemy.png";
    private String playerPicture = "/view/resources/laserPlayer.png";

    /**
     * Ініціалізація класу
     * @param x
     * @param y
     * @param type
     */
    public Bullet(double x, double y, int type){
        if(type == 1) {
            try {
                this.setImage(new Image(getClass().getResource(enemyPicture).toURI().toString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            this.setEffect(new DropShadow(20, Color.RED));
        }else{
            try {
                this.setImage(new Image(getClass().getResource(playerPicture).toURI().toString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            this.setEffect(new DropShadow(20, Color.GREEN));
        }
        this.setLayoutX(x-this.getImage().getWidth()/2);
        this.setLayoutY(y);

        isDestroyed = false;
    }

    /**
     * Встановлення параметру чи знищенний снаряд
     * @param isDestroyed
     */
    public void setIsDestroyed(boolean isDestroyed) {
        this.isDestroyed = isDestroyed;
    }

    /**
     *
     * @return чи снаряд знищенний
     */
    public boolean getExist(){
        return isDestroyed;
    }
}
