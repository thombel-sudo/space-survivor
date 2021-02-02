package view;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Клас обробки головного вікна одиночної гри
 */
public class GameViewManager {

    private AnchorPane gamePane;
    private Scene gameScene;
    private Stage gameStage;
    private ViewManager rootWindow;
    private Rectangle info;
    private Rectangle back;
    private SpaceSurvivorButton resume;
    private SpaceSurvivorButton exit;
    private SpaceSurvivorButton again;
    private Stage menuStage;
    private ImageView ship;
    private Rectangle shipBox;

    private static final int GAME_WIDTH = 600;
    private static final int GAME_HEIGHT = 720;

    private boolean isLeftKeyPressed;
    private boolean isRightKeyPressed;
    private boolean isSpaceKeyPressed;

    private int bulletsLeft;
    private int lives;
    private int score;
    Text currentScoreStatus;

    Text bulletsLeftStatus;
    Text currentLivesStatus;

    private int angle;
    private AnimationTimer gameTimer;
    private boolean paused = false;
    private Rectangle rect;
    private GridPane gridPane1;
    private GridPane gridPane2;
    private final static String BACKGROUND_IMAGE = "view/resources/gameBackgrounds/";

    MediaPlayer shootSound;
    MediaPlayer deathSound;

    List<Bullet> bullets = new ArrayList<>();
    List<Enemy> enemies = new ArrayList<>();
    List<Ammo> ammos = new ArrayList<>();
    List<Bullet> enemyBullets = new ArrayList<>();

    /**
     * Ініціалізація класу
     * @param root
     */
    public GameViewManager(ViewManager root){
        rootWindow = root;
        initializeStage();
        createKeyListeners();
    }

    /**
     * Ініціалізація вікна
     */
    public void initializeStage() {

        gamePane = new AnchorPane();
        gameScene = new Scene(gamePane,GAME_WIDTH,GAME_HEIGHT);
        gameStage = new Stage();
        gameStage.setResizable(false);
        gameStage.setTitle("Одиночна гра");
        Image gameIcon = new Image("model/resources/game_icon.png");
        gameStage.getIcons().add(gameIcon);
        gameStage.setOnCloseRequest(event -> {
            resetAll();
            gameTimer.stop();
            menuStage.show();
            gameStage.close();
        });
        createMenu();
        gameStage.setScene(gameScene);

    }

    /**
     * Початок нової гри
     * @param menuStage
     * @param choosenShip
     */
    public void createNewGame(Stage menuStage, SHIP choosenShip){
        this.menuStage = menuStage;
        this.menuStage.hide();
        createBackround();
        createStats();
        createShip(choosenShip);
        resetAll();

        gameStage.show();
        createGameLoop();

        //createTestObject();
        //ship = choosenShip.getUrlShip();
    }

    /**
     * Створення івентів для клавіатури
     */
    public void createKeyListeners() { //метод для прослушки клавиш

        try {
            URL resource = getClass().getResource("/view/resources/sound/shoot.WAV");
            shootSound = new MediaPlayer(new Media(resource.toURI().toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        shootSound.setVolume(rootWindow.getVolumeSounds());
        try {
            URL resource = getClass().getResource("/view/resources/sound/death.WAV");
            deathSound = new MediaPlayer(new Media(resource.toURI().toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        deathSound.setVolume(rootWindow.getVolumeSounds());
        gameScene.setOnKeyPressed(event -> {    //стреляем на пробел
            if (event.getCode() == KeyCode.SPACE){
                if(isSpaceKeyPressed==false){ //АНТИСПАМ ПРОВЕРКА
                    shoot();
                    shootSound.play();
                    shootSound.seek(Duration.ZERO);
                    isSpaceKeyPressed = true;
                }
            }
            if(event.getCode() == KeyCode.ESCAPE){
                pauseGame();
                showMenu();
            }
            if(event.getCode() == KeyCode.LEFT){
                isLeftKeyPressed = true;
                //ship.setLayoutX(ship.getX() - 5);
            }else if(event.getCode()== KeyCode.RIGHT){
                //ship.setLayoutX(ship.getX() - 5);
                isRightKeyPressed = true;
            }
        });
        gameScene.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.LEFT){
                isLeftKeyPressed = false;
                //ship.setLayoutX(ship.getX() - 5);
            }else if(event.getCode()== KeyCode.RIGHT){
                //ship.setLayoutX(ship.getX() - 5);
                isRightKeyPressed = false;
            }
            if (event.getCode() == KeyCode.SPACE){
                if(isSpaceKeyPressed==true){
                    //shoot();
                    isSpaceKeyPressed = false;
                }
            }
        });

        gameScene.setOnKeyTyped(event -> {

        });

    }

    /**
     *
     * @return кількість знарядів гравця
     */
    public int getBulletsLeft() {
        return bulletsLeft;
    }

    /**
     * Встановлення кількості знарядів гравця
     * @param bulletsLeft
     */
    public void setBulletsLeft(int bulletsLeft) {
        this.bulletsLeft = bulletsLeft;
    }

    /**
     *
     * @return космоліт гравця
     */
    public ImageView getShip() {
        return ship;
    }

    /**
     *
     * @return кількість життів
     */
    public int getLives(){
        return lives;
    }

    /**
     * Встановлення кількості життів
     * @param lives
     */
    public void setLives(int lives) {
        this.lives = lives;
    }


    /**
     * Пауза гри
     */
    public void pauseGame(){
        if(!paused) {
            synchronized (gameTimer) {

                gameTimer.stop();
                paused = true;

            }
        }else{
            synchronized (gameTimer) {


                gameTimer.start();

                paused = false;
            }
        }
    }

    /**
     * Обробка пострілу гравця
     */
    public void shoot(){
        if(bulletsLeft > 0) {
            Bullet bullet = new Bullet(ship.getLayoutX() + ship.getImage().getWidth() / 2, ship.getLayoutY() - 10, 0);
            bullets.add(bullet);  //добавляем в лист пули
            gamePane.getChildren().add(bullet); // добавляем на экран
            bulletsLeft--;
        }
    }

    /**
     * Обробка пострілу супротивника
     * @param enemy
     */
    public void enemyShoot(Enemy enemy){
        Bullet bullet = new Bullet(enemy.getLayoutX() + enemy.getImage().getWidth() / 2, enemy.getLayoutY()+enemy.getImage().getWidth() + 10, 1);
        enemyBullets.add(bullet);  //добавляем в лист пули
        gamePane.getChildren().add(bullet); // добавляем на экран
        //bulletsLeft--;
    }

    /**
     * Кінець гри
     */
    public void endGame(){
//        gameStage.close();
//        resetAll();
//        gameTimer.stop();
//        menuStage.show();
//
        showDeathPanel();
    }

    /**
     *
     * @return чи живий гравець
     */
    public boolean checkPlayerStatus(){
        if(lives <= 0) {
            return false;
        }
        return  true;
    }

    /**
     * Перевірка коллізії між двома об'єктами
     * @param toCheck
     * @param toCheck2
     * @return
     */
    public boolean checkCollision(Node toCheck, Node toCheck2){
        if(toCheck.getBoundsInParent().intersects(toCheck2.getBoundsInParent())){
            //System.out.println("yep");
            return true;
        }else{
            //System.out.println("no");
            return  false;
        }
    }

    /**
     * Повернення стану гри до початкового
     */
    public void resetAll(){
        gamePane.getChildren().removeAll(enemies);
        gamePane.getChildren().removeAll(bullets);
        gamePane.getChildren().removeAll(enemyBullets);
        gamePane.getChildren().removeAll(ammos);

        exit.setOnAction(event -> {
            resetAll();
            gameTimer.stop();
            menuStage.show();
            gameStage.close();
        });

        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        ammos.clear();
        bulletsLeft = 50;
        lives = 5;
        //currentLevel = 1;
        score = 0;
        ship.setLayoutX(GAME_WIDTH/2 - ship.getImage().getWidth()/2);
        ship.setLayoutY(GAME_HEIGHT- ship.getImage().getHeight() - 15);
        shipBox.setX(ship.getLayoutX() + ((ship.getImage().getWidth() - 80)/2));
        shipBox.setY(ship.getLayoutY() + ((ship.getImage().getHeight() - 40)));
    }

    /**
     *
     * @return вікно меню
     */
    public Stage getMenuStage() {
        return menuStage;
    }

    /**
     * Встановлює вікно меню
     * @param menuStage
     */
    public void setMenuStage(Stage menuStage) {
        this.menuStage = menuStage;
    }


    /**
     * Створення космоліту
     * @param choosenShip
     */
    public void createShip(SHIP choosenShip){
        ship = new ImageView(choosenShip.getUrlShip()); //устанавливаем картинку
        shipBox = new Rectangle(80,80,Color.TRANSPARENT);
        ship.setLayoutY(GAME_HEIGHT- ship.getImage().getHeight() - info.getHeight()); //страшные формулы для позиции
        //System.out.println(ship.getImage().getHeight());
        //System.out.println(ship.getFitWidth());
        //ship.setEffect(new DropShadow(20, Color.BLACK));
        ship.setLayoutX((GAME_WIDTH/2) - ship.getImage().getWidth()/2);
        shipBox.setX(ship.getLayoutX() + ((ship.getImage().getWidth() - 80)/2));
        shipBox.setY(ship.getLayoutY() + ((ship.getImage().getHeight() - 40)));
        gamePane.getChildren().add(shipBox);
        gamePane.getChildren().add(ship); //добавляем на поле
    }

    /**
     * Оновлення місцезнахождення знарядів
     */
    public void moveBullets(){
        for(Bullet bullet : bullets){ //проходимся по всему списку

            if(bullet.getLayoutY() <= -GAME_HEIGHT){ //вихід за межі екрану
                bullet.setIsDestroyed(true);
                gamePane.getChildren().remove(bullet);
            }else{
                bullet.setLayoutY(bullet.getLayoutY()  - 5);
            }
        }
//        bullets.forEach(bullet -> {
//
//        });
        bullets.removeIf(Bullet::getExist);
    }

    /**
     * Оновлення місцезнаходження супротивників
     */
    public void moveEnemyBoolets(){
        for(Bullet bullet : enemyBullets){

            if(bullet.getLayoutY() > GAME_HEIGHT || checkCollision(shipBox,bullet)){
                if(checkCollision(shipBox,bullet)){
                    setLives(getLives()-1);
                }
                bullet.setIsDestroyed(true);
                gamePane.getChildren().remove(bullet);

            }else{
                bullet.setLayoutY(bullet.getLayoutY()  + 5);

            }
        }
//        bullets.forEach(bullet -> {
//
//        });
        enemyBullets.removeIf(Bullet::getExist);
    }

    /**
     * Створення головного потіку гри
     */
    public void createGameLoop(){
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                moveShip();
                moveBackground();
                moveBullets();
                if(enemies.size() < 8 && Math.random() < 0.01 ) {

                    //createEnemy("Red");
                    createWave();
                }
                updateStats();
                moveEnemies();
                moveEnemyBoolets();
                moveAmmo();
                if(checkPlayerStatus() == false){
                    endGame();
                }
            }
        };
        gameTimer.start();
    }

    /**
     * Оновлення місцезнахдження та куту нахилу космоліту
     */
    public void moveShip(){

        if(isLeftKeyPressed && !isRightKeyPressed){
            if(angle > -30){
                angle-=5;
            }
            ship.setRotate(angle);
            if(ship.getLayoutX() > -20){
                ship.setLayoutX(ship.getLayoutX() - 3 );
                shipBox.setX(shipBox.getX() - 3);
            }
        }
        if(!isLeftKeyPressed && isRightKeyPressed){
            if(angle < 30){
                angle +=5;
            }
            ship.setRotate(angle);
            if(ship.getLayoutX()+ship.getImage().getWidth() < GAME_WIDTH){
                ship.setLayoutX(ship.getLayoutX() + 3 );
                shipBox.setX(shipBox.getX() + 3);
            }
        }
        if(!isLeftKeyPressed && !isRightKeyPressed){
            if(angle < 0){
                angle+=5;
            }else if (angle > 0){
                angle-=5;
            }
            ship.setRotate(angle);
        }
        if(isLeftKeyPressed && isRightKeyPressed){
            if(angle < 0){
                angle+=5;
            }else if (angle > 0){
                angle-=5;
            }
            ship.setRotate(angle);
        }
    }

    /**
     * Створення фону
     */
    public void createBackround(){
        gridPane1 = new GridPane();

        gridPane2 = new GridPane();
        String currentImage = BACKGROUND_IMAGE + (int)(Math.random() * 3) + ".png";

        for (int i = 0 ; i < 16; i++){
            ImageView backgroundImage1 = new ImageView(currentImage);
            ImageView backgroundImage2 = new ImageView(currentImage);

            GridPane.setConstraints(backgroundImage1, i%4,i/4);
            GridPane.setConstraints(backgroundImage2, i%4,i/4);
            gridPane1.getChildren().add(backgroundImage1);
            gridPane2.getChildren().add(backgroundImage2);
        }

        gridPane2.setLayoutY(-720);
        gamePane.getChildren().addAll(gridPane1,gridPane2);
    }

    /**
     * Створення нового ворога
     * @param name
     */
    public void createEnemy(String name){
        Enemy enemy = new Enemy(name);
        enemy.setLayoutY(ship.getLayoutY() - GAME_HEIGHT);
        enemy.setLayoutX(Math.random() * (GAME_WIDTH-enemy.getImage().getWidth()*2) + enemy.getImage().getWidth());

        enemies.add(enemy);
        gamePane.getChildren().add(enemy);
        //System.out.println(enemy.getEnemyPicture());
    }

    /**
     * Створення хвилі ворогів
     */
    public void createWave(){
        if(score < 250){
            createEnemy("Black");
        }else if(score < 500){
            createEnemy("Blue");
        }else if(score < 1500){
            createEnemy("Green");
        }else{
            createEnemy("Red");
        }
    }

    /**
     * Оновлення місцезнаходження ворогів
     */
    public void moveEnemies(){
        for(Enemy enemy : enemies){
            System.out.println(enemy.getLayoutY());
            if(enemy.getLayoutY() > GAME_HEIGHT + 20){
                //System.out.println("---------------------||||"+enemy.getLayoutY());
                //enemy.setDead(true);
                //gamePane.getChildren().remove(enemy);
                enemy.setLayoutY(ship.getLayoutY() - GAME_HEIGHT);
                enemy.setLayoutX(Math.random() * (GAME_WIDTH-enemy.getImage().getWidth()) + enemy.getImage().getWidth());

            }else{
                for(Bullet bullet : bullets) {
                    if(checkCollision(bullet,enemy)){
                        bullet.setIsDestroyed(true);
                        enemy.setDead(true);
                        if(Math.random() < 0.3){
                            createAmmo(enemy.getLayoutX() + enemy.getImage().getWidth()/2,enemy.getLayoutY()+enemy.getImage().getHeight()/2);
                        }
                        gamePane.getChildren().remove(bullet);
                        gamePane.getChildren().remove(enemy);
                        score+= enemy.getPointsForKill();
                        deathSound.play();
                        deathSound.seek(Duration.ZERO);
                    }
                }
                System.out.println(Math.random());
                if(Math.random()<0.008){
                    enemyShoot(enemy);
                }
                if(checkCollision(shipBox,enemy)){
                    setLives(getLives()-1);
                    gamePane.getChildren().remove(enemy);
                    enemy.setDead(true);
                    deathSound.play();
                    deathSound.seek(Duration.ZERO);
                }
                enemy.setLayoutY(enemy.getLayoutY()  + enemy.getSpeed());

            }
        }
//        bullets.forEach(bullet -> {
//
//        });
        enemies.removeIf(Enemy::isDead);

    }

    /**
     * Оновлення місцезнаходження патронів
     */
    public void moveAmmo(){
        for(Ammo ammo : ammos){

            if(ammo.getLayoutY() <= -GAME_HEIGHT){
                //System.out.println("---------------------||||"+ammo.getLayoutY());
                ammo.setTaken(true);
                gamePane.getChildren().remove(ammo);
            }else{
                if(checkCollision(shipBox,ammo)){
                    gamePane.getChildren().remove(ammo);
                    addAmmoToPlayer(20);
                    ammo.setTaken(true);
                }
                ammo.setLayoutY(ammo.getLayoutY() + 2.5);
                //System.out.println("AMMO IS HERE :" + ammo.getLayoutY()+ " | " + ammo.getLayoutX());
                //System.out.println("PLAYER IS HERE :" + ship.getLayoutY()+ " | " + ship.getLayoutX());
            }
        }
        ammos.removeIf(Ammo::isTaken);

    }

    /**
     * Створення патронів
     * @param startX
     * @param startY
     */
    public void createAmmo(double startX, double startY){
        //System.out.println("ammo created");
        Ammo newAmmo = new Ammo(startX,startY);
        //newAmmo.setLayoutX(startX);
        //newAmmo.setLayoutY(startY);
        ammos.add(newAmmo);
        gamePane.getChildren().add(newAmmo);
    }

    /**
     * Додавання патронів гравцю
     * @param amm
     */
    public void addAmmoToPlayer(int amm){
        this.bulletsLeft += amm;
    }

    /**
     * Створення даних гравця (кількість очок, кількість знарядів та здоров'я)
     */
    public void createStats(){
        info = new Rectangle(GAME_WIDTH + 20,50,Color.DARKGRAY);
        
        info.toFront();
        info.setOpacity(0.5);
        info.setLayoutX(0);
        info.setLayoutY(0);
        currentScoreStatus = new Text(30,25, "Score: " + score);
        try {
            currentScoreStatus.setFont(Font.loadFont(getClass().getResource("/model/resources/UkrainianAdverGothic.ttf").toURI().toString(),15));
        } catch ( URISyntaxException e) {

        }
        bulletsLeftStatus = new Text(currentScoreStatus.getX() + currentScoreStatus.getLayoutBounds().getWidth() + 90,25, "Bullets left: " + bulletsLeft);
        try {
            bulletsLeftStatus.setFont(Font.loadFont(getClass().getResource("/model/resources/UkrainianAdverGothic.ttf").toURI().toString(),15));
        } catch ( URISyntaxException e) {

        }
        //currentLvlStatus = new Text(bulletsLeftStatus.getX()+bulletsLeftStatus.getLayoutBounds().getWidth() + 30,25, "Level: " + currentLevel);
        currentLivesStatus = new Text(bulletsLeftStatus.getX()+bulletsLeftStatus.getLayoutBounds().getWidth() + 90,25, "HP: " + lives);
        try {


            currentLivesStatus.setFont(Font.loadFont(getClass().getResource("/model/resources/UkrainianAdverGothic.ttf").toURI().toString(),15));
        } catch ( URISyntaxException e) {

        }
        gamePane.getChildren().addAll(info,currentScoreStatus,bulletsLeftStatus,currentLivesStatus);

        //info.set
    }

    /**
     * Оновлення даних гравця
     */
    public void updateStats(){
        info.toFront();
        bulletsLeftStatus.toFront();
        currentLivesStatus.toFront();
        currentScoreStatus.toFront();

        bulletsLeftStatus.setText("Снарядів : " + bulletsLeft);
        currentScoreStatus.setText("Рахунок: " + score);
        //currentLvlStatus.setText("Level: " + currentLevel);
        currentLivesStatus.setText("Життів залишилось: " + lives);
    }

    /**
     * Пересування фону
     */
    public void moveBackground(){
        gridPane1.setLayoutY(gridPane1.getLayoutY()+0.5);
        gridPane2.setLayoutY(gridPane2.getLayoutY()+0.5);

        if(gridPane1.getLayoutY() >= 720){
            gridPane1.setLayoutY(-720);
        }
        if(gridPane2.getLayoutY() >= 720){
            gridPane2.setLayoutY(-720);
        }

    }

    /**
     * Відображення меню
     */
    public void showMenu(){
        if(back.isVisible()){
            //gridPane1.setVisible(false);

            back.setVisible(false);
            resume.setVisible(false);
            exit.setVisible(false);
        }else{
            back.toFront();
            resume.toFront();
            exit.toFront();
            back.setVisible(true);
            resume.setVisible(true);
            exit.setVisible(true);
        }
    }

    /**
     * Створення меню
     */
    public void createMenu(){
        back = new Rectangle(GAME_WIDTH+10,GAME_HEIGHT+10,Color.DARKCYAN);

        back.setOpacity(0.4);
        resume = new SpaceSurvivorButton("ПРОДОВЖИТИ");
        resume.setLayoutX(GAME_WIDTH/2 - resume.getPrefWidth()/2);
        resume.setLayoutY(GAME_HEIGHT/3);
        System.out.println(resume.getLayoutX() + " | " + resume.getLayoutY());
        exit = new SpaceSurvivorButton("ВИХІД");
        exit.setLayoutX(GAME_WIDTH/2 - resume.getPrefWidth()/2);
        exit.setLayoutY(GAME_HEIGHT/3 + resume.getPrefHeight() + 30);
        System.out.println(exit.getLayoutX() + " | " + exit.getLayoutY());
        gamePane.getChildren().addAll(back,resume,exit);

        resume.setOnAction(event -> {
            pauseGame();
            showMenu();
        });

        exit.setOnAction(event -> {
            resetAll();
            gameTimer.stop();
            menuStage.show();
            gameStage.close();
        });
        back.setVisible(false);
        resume.setVisible(false);
        exit.setVisible(false);

    }

    /**
     * Створення програшної панелі
     */
    public void showDeathPanel(){
        gameTimer.stop();
        Rectangle deathPanel = new Rectangle(GAME_WIDTH+10,GAME_HEIGHT+10,Color.DARKRED);

        InfoLabel looseText = new InfoLabel("ВИ ПРОГРАЛИ");
        InfoLabel scoreText = new InfoLabel("Ваш результат: " + score + " !");
        looseText.setLayoutY(30);
        looseText.setLayoutX((GAME_WIDTH - looseText.getPrefWidth())/2);

        scoreText.setLayoutY(20 + looseText.getPrefHeight() + looseText.getLayoutY());
        scoreText.setLayoutX((GAME_WIDTH - scoreText.getPrefWidth())/2);

        deathPanel.setOpacity(0.6);
        SpaceSurvivorButton startAgain = new SpaceSurvivorButton("НОВА СПРОБА");
        startAgain.setLayoutX(GAME_WIDTH/2 - startAgain.getPrefWidth()/2);
        startAgain.setLayoutY(GAME_HEIGHT/3);
        System.out.println(startAgain.getLayoutX() + " | " + startAgain.getLayoutY());
        exit = new SpaceSurvivorButton("ВИХІД");
        exit.setLayoutX(GAME_WIDTH/2 - startAgain.getPrefWidth()/2);
        exit.setLayoutY(GAME_HEIGHT/3 + startAgain.getPrefHeight() + 30);
        //System.out.println(exit.getLayoutX() + " | " + exit.getLayoutY());
        gamePane.getChildren().addAll(deathPanel,startAgain,exit,looseText,scoreText);

        deathPanel.toFront();
        scoreText.toFront();
        looseText.toFront();
        startAgain.toFront();
        exit.toFront();
        startAgain.setOnAction(event -> {
            //pauseGame();
            //initializeStage();
            resetAll();
            gameTimer.start();
            //showMenu();
            deathPanel.setVisible(false);
            startAgain.setVisible(false);
            looseText.setVisible(false);
            scoreText.setVisible(false);
            exit.setVisible(false);
            gamePane.getChildren().removeAll(deathPanel,startAgain,looseText,scoreText);
        });

        exit.setOnAction(event -> {
            resetAll();
            gameTimer.stop();
            menuStage.show();
            gameStage.close();
        });
    }


}
