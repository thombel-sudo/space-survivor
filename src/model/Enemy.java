package model;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * Клас обробки ворогів
 */
public class Enemy extends ImageView {
    private boolean isDead;
    private double speed;
    private int pointsForKill;
    private String enemyPicture = "view/resources/enemies/enemy";

    /**
     * Ініціалізація класу
     * @param name
     */
    public Enemy(String name){
        setEnemyPicture(name + Integer.toString((int)(Math.random() * 5)+1) + ".png");
        System.out.println(getEnemyPicture());
        switch (name){
            case "Black":
                setSpeed(1);
                setPointsForKill(15);
                break;
            case "Blue":
                setSpeed(2);
                setPointsForKill(30);
                break;
            case "Green":
                setSpeed(3.5);
                setPointsForKill(60);
                break;
            case "Red":
                setSpeed(4);
                setPointsForKill(77);
                break;
        }
        //setSpeed(2);
        setDead(false);
        this.setImage(new Image(enemyPicture));
        this.setEffect(new DropShadow(20, Color.BLACK));

    }

    /**
     * Встановлення параметру "смерті" ворога
     * @param dead
     */
    public void setDead(boolean dead) {
        isDead = dead;
    }

    /**
     * Встановлення зображення ворога
     * @param enemyPicture шлях до картинки
     */
    public void setEnemyPicture(String enemyPicture) {
        this.enemyPicture = this.enemyPicture + enemyPicture;
    }

    /**
     * Встановлення параметру швидкості
     * @param speed
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     *
     * @return швидкість ворога
     */
    public double getSpeed() {
        return speed;
    }

    /**
     *
     * @return шлях до зображення ворога
     */
    public String getEnemyPicture() {
        return enemyPicture;
    }

    /**
     *
     * @return статус ворога
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Встановлення кількості очків за ворога
     * @param pointsForKill
     */
    public void setPointsForKill(int pointsForKill) {
        this.pointsForKill = pointsForKill;
    }

    /**
     *
     * @return кількість очків за ворога
     */
    public int getPointsForKill() {
        return pointsForKill;
    }
}
