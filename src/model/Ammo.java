package model;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Клас для обробки патронів
 */
public class Ammo extends Circle {
    private int howManyAmmo;
    private boolean taken; //перевірка на те, чи був підібраний патрон
    //private AnchorPane gamePane;
    //Circle ammo;

    /**
     * Ініціалізація класу
     * @param x координата для створення снаряду по осі Х
     * @param y координата для створення снаряду по осі У
     */
    public Ammo(double x, double y){
        this.setCenterY(y);
        this.setCenterX(x);
        this.setRadius(5); //параметр радіусу для патрону
        this.setFill(Color.RED); //колір патронів
        taken = false; // встановлення параметру, що патрон не підібраний
    }

    /**
     * Переміщення патронів на екрані
     */
    public void moveAmmo(){
        this.setLayoutY(getLayoutY() - 1);
    }

    /**
     *
     * @return статус того, чи підняті патрони
     */
    public boolean isTaken() {
        return taken;
    }

    /**
     * Зміна статусу патронів
     * @param taken
     */
    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public void setHowManyAmmo(int howManyAmmo) {
        this.howManyAmmo = howManyAmmo;
    }

    public int getHowManyAmmo() {
        return howManyAmmo;
    }
}
