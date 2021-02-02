package model;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

import java.io.Serializable;

/**
 * Клас для обробки снарядів для мультиплеєру
 */
public  class MultiplayerBullet extends Bullet implements Serializable {
    public transient Point2D velocity = new Point2D(0,0);


    /**
     * Ініціалізація класу
     * @param x
     * @param y
     * @param type
     */
    public MultiplayerBullet(double x, double y, int type) {
        super(x, y, type);
        //setPreserveRatio(true);

    }

    /**
     * Встановлення цілі для снаряду
     * @param x
     * @param y
     */
    public void setTarget(double x, double y){

        velocity = new Point2D(x,y).subtract(getLayoutX(),getLayoutY()).normalize().multiply(8);
        //System.out.println("You gave me this x and y: " + x + "|" + y);
       // System.out.println("This is where shooting starts from x and y: " + getLayoutX() + "|" + getLayoutY());
        //System.out.println("This is 1 step size x and y: " + velocity.getX() + "|" + velocity.getY());
        double angle = calcAngle(velocity.getX(),velocity.getY());
        getTransforms().clear();
        //setRotate(angle+90);
        getTransforms().add(new Rotate(angle+90,0,0));

    }

    /**
     * Обробка переміщення снарядів
     */
    public void move(){
        try {
            setLayoutX(getLayoutX() + velocity.getX());
            setLayoutY(getLayoutY() + velocity.getY());
            //System.out.println("going to move from this x to this x: " + (getTranslateX() - velocity.getX()) + " to " + getTranslateX());
        }catch (Exception ex){
            //System.out.println("");
        }
    }

    /**
     * Підрахунок куту для повороту снаряда
     * @param vecX
     * @param vecY
     * @return
     */
    public double calcAngle(double vecX,double vecY){
        double angle = new Point2D(vecX,vecY).angle(1,0);
        //System.out.println("got this vecX: " + vecX + " and this vecY: " + vecY);
        //System.out.println("angle is :" + angle );
        return  vecY > 0 ? angle : -angle;
    }
}
