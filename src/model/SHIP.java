package model;

import java.net.URISyntaxException;

/**
 * Клас для роботи з космолітом
 */
public enum SHIP {
    BLUE("/view/resources/shipchooser/blue_ship.png"),
    GREEN("/view/resources/shipchooser/green_ship.png"),
    ORANGE("/view/resources/shipchooser/orange_ship.png"),
    RED("/view/resources/shipchooser/red_ship.png");

    private String urlShip;

    /**
     * Ініціалізація класу
     * @param urlShip
     */
    private SHIP(String urlShip){
        try {
            this.urlShip = getClass().getResource(urlShip).toURI().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return шлях до картинки космоліту
     */
    public String getUrlShip() {
        return urlShip;
    } //возвращаем путь к картинке корабля
}
