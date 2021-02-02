package network;


import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;
import view.ViewManager;

import java.io.*;
import java.net.*;
import java.net.InetAddress.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

/**
 * Клас для обробки хосту
 */
public class Server  {
    private AnchorPane gamePane; //главная панель для элементов
    private Scene gameScene; // главная сцена
    private Stage gameStage;
    private ViewManager rootWindow;

    private Stage menuStage; //для контроля доступа к основному меню


    private ImageView shipPlayer; //собсна наш корабль
    private Rectangle shipPlayerBox;

    private ImageView shipOpponent; //собсна наш корабль
    private Rectangle shipOpponentBox;
    private boolean isLeftKeyPressed; //нажата ли левая кнопка мыши
    private boolean isRightKeyPressed; // нажата ли правая
    private boolean isUpKeyPressed; // нажата ли правая
    private boolean isDownKeyPressed;

    private static final int GAME_WIDTH = 600; //разрешение экрана
    private static final int GAME_HEIGHT = 720;

    private AnimationTimer gameTimer; //поток для игры

    private GridPane gridPane1; //первая панель для прокрутки фона и вторая
    private GridPane gridPane2;
    private final static String BACKGROUND_IMAGE = "view/resources/gameBackgrounds/"; //собсна фон

    private List<MultiplayerBullet> playerBullets = new ArrayList<>(); // тут храним пули

    private List<MultiplayerBullet> opponentBullets = new ArrayList<>();

    private int port = 8001;
    private InetAddress inetAddress;
    private ServerSocket serverSocket;
    private Socket socket;
    private Pane infoPane;
    private boolean move;
    private AnimationTimer at;
    //int testcounter = 0;
    private InputStream in = null;
    private OutputStream out = null;

    private int hpPlayer = 5;
    private int hpOpponent = 5;

    static final byte PLAYER_MOVE = 1, SHOOT_PLAYER = 2;

    /**
     * Ініціалізація класу
     * @param root
     * @param menuStage
     */
    public Server(ViewManager root, Stage menuStage) {
        rootWindow = root;
        gamePane = new AnchorPane();
        gameScene = new Scene(gamePane,GAME_WIDTH,GAME_HEIGHT);
        gameStage = new Stage();
        gameStage.setTitle("Онлайн гра");
        Image gameIcon = new Image("model/resources/game_icon.png");
        gameStage.getIcons().add(gameIcon);
        gameStage.setResizable(false);
        gameStage.setScene(gameScene);
        this.menuStage = menuStage;
        this.menuStage.hide();
        createBackround();
        createPlayerShip();
        showWaitConnection();
        gameStage.show();
        //infoPane.toFront();
        runServer();
        infoPane.setVisible(false);
        //startAnim();
        createListener();
        startAnimation();
        startConnectionListener();
        /*
        gameScene.setOnMouseClicked(event -> {
            byte[] data = new byte[1+8*4];
            ByteBuffer.wrap(data).put(PLAYER_MOVE).putShort((short) 5).putShort((short) 2);
            try {
                out.write(data);
            } catch (IOException e) {
                System.out.println("some shit occured " + e.getMessage());
            }
        });

         */
    }

    /**
     * Очікування з'єднання з супротивником
     */
    public void runServer(){

        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            socket.setTcpNoDelay(true);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            createOpponentShip();
        } catch (IOException e) {
            System.out.println("Проблема зі створенням сокету");
        }

    }

    /**
     * Відображення поточної IP адреси хосту
     */
    public void showWaitConnection(){
        infoPane = new Pane();
        infoPane.setPrefSize(500,500);
        infoPane.setLayoutX((GAME_WIDTH - infoPane.getPrefWidth())/2);
        infoPane.setLayoutY(200);
        infoPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,null,null)));
        InfoLabel waitText = new InfoLabel("ОЧІКУЄТЬСЯ ДРУГИЙ ГРАВЕЦЬ");
        InfoLabel ipText;
        try {
            ipText = new InfoLabel("Ваш ip для підключення:\n " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            ipText = new InfoLabel("Виникла помилка з вашою IP адресою");
        }
        waitText.setPrefWidth(400);
        waitText.setLayoutX((infoPane.getPrefWidth() - waitText.getPrefWidth())/2);
        waitText.setLayoutY((infoPane.getPrefHeight()/4));
        waitText.setWrapText(false);
        ipText.setLayoutX((infoPane.getPrefWidth() - ipText.getPrefWidth())/2);
        ipText.setLayoutY(infoPane.getPrefHeight()/3);
        ipText.setWrapText(false);
        ipText.setTextAlignment(TextAlignment.CENTER);
        //ipText.toFront();
        //waitText.toFront();
        infoPane.getChildren().addAll(waitText,ipText);
        infoPane.toFront();
        gamePane.getChildren().add(infoPane);
    }

    /**
     * Вистріл гравця
     * @param event
     */
    public void shootPlayer(MouseEvent event){

        MultiplayerBullet bullet = new MultiplayerBullet(shipPlayer.getLayoutX() + shipPlayer.getImage().getWidth() / 2, shipPlayer.getLayoutY() +  shipPlayer.getImage().getHeight()/2 , 0);
        bullet.setTarget(event.getSceneX(),event.getSceneY());
        playerBullets.add(bullet);  //добавляем в лист пули
        gamePane.getChildren().add(bullet); // добавляем на экран
        shipPlayer.toFront();

        byte[] data = new byte[1+8*4];
        ByteBuffer.wrap(data).put(SHOOT_PLAYER) .putDouble(event.getSceneX()) .putDouble(event.getSceneY()) .putDouble(bullet.getLayoutX()) .putDouble(bullet.getLayoutY());
        try {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            System.out.println("something occured");
        }

        //System.out.println(event.getSceneX() + " | " + event.getScreenX()  + " | " +  event.getX());

    }
    /**
     * Оновлення місцезнаходження знарядів гравця
     */
    public void movePlayerBullets(){
        for(MultiplayerBullet bullet : playerBullets){
            //System.out.println(bullet.getLayoutY());
            if(checkCollision(bullet,shipOpponentBox)){
                hpOpponent--;
                System.out.println("[SERVER] OPPONENT HP: " + hpOpponent);
                bullet.setIsDestroyed(true);
                gamePane.getChildren().remove(bullet);
            }else if(bullet.getLayoutY() < -5 || bullet.getLayoutY() > GAME_HEIGHT + 5 || bullet.getLayoutX() < -5 || bullet.getLayoutX() > GAME_WIDTH + 5){ //выход за границу
                //System.out.println("-------------------BULLET OUT OF RANGE");
                bullet.setIsDestroyed(true);
                gamePane.getChildren().remove(bullet);
            }else{ //иначе двигаем выше
                bullet.move();
                shipPlayer.toFront();
            }

        }
        playerBullets.removeIf(Bullet::getExist);
    }
    /**
     * Оновлення місцезнаходження знарядів супротивника
     */
    public void moveOpponentBullets(){
        for(MultiplayerBullet bullet : opponentBullets){

            if(checkCollision(bullet,shipPlayerBox)){

                hpPlayer--;
                System.out.println("[SERVER] PLAYER HP: " + hpPlayer);
                bullet.setIsDestroyed(true);
                gamePane.getChildren().remove(bullet);
            }else if(bullet.getLayoutY() < -5 || bullet.getLayoutY() > GAME_HEIGHT + 5 || bullet.getLayoutX() < -5 || bullet.getLayoutX() > GAME_WIDTH + 5){ //выход за границу
                //System.out.println("---------------------||||"+bullet.getLayoutY());
                bullet.setIsDestroyed(true);
                // System.out.println("enemy bullet out of range");
                gamePane.getChildren().remove(bullet);
            }else{ //иначе двигаем выше
                bullet.move();
                shipOpponent.toFront();
            }

        }
        opponentBullets.removeIf(Bullet::getExist); //удаление всех пуль которые вышли за границу
    }
    /**
     * Створення космоліту гравця
     */
    public void createPlayerShip(){
        shipPlayer = new ImageView(new Image("view/resources/multiplayerShips/playerShip.png")); //устанавливаем картинку
        shipPlayerBox = new Rectangle(70,70, Color.TRANSPARENT);
        //shipPlayerBox.setFill(Color.BLACK);
        shipPlayer.setLayoutY(GAME_HEIGHT / 2); //страшные формулы для позиции
        shipPlayer.setLayoutX(10);
        shipPlayerBox.setX(shipPlayer.getLayoutX() + (shipPlayer.getImage().getWidth() - shipPlayerBox.getWidth())/2);
        shipPlayerBox.setY(shipPlayer.getLayoutY() + (shipPlayer.getImage().getHeight() - shipPlayerBox.getHeight())/2 );
        gamePane.getChildren().add(shipPlayerBox);
        gamePane.getChildren().add(shipPlayer); //добавляем на поле
    }
    /**
     * Створення космоліту супротивника
     */
    public void createOpponentShip(){
        shipOpponent = new ImageView(new Image("view/resources/multiplayerShips/opponentShip.png")); //устанавливаем картинку
        shipOpponentBox = new Rectangle(70,70, Color.TRANSPARENT);
        //shipPlayerBox.setFill(Color.BLACK);
        shipOpponent.setLayoutY(GAME_HEIGHT / 2); //страшные формулы для позиции
        shipOpponent.setLayoutX(GAME_WIDTH - shipPlayer.getImage().getWidth());
        shipOpponentBox.setX(shipOpponent.getLayoutX() + (shipPlayer.getImage().getWidth() - shipOpponentBox.getWidth())/2);
        shipOpponentBox.setY(shipOpponent.getLayoutY() + (shipPlayer.getImage().getHeight() - shipOpponentBox.getHeight())/2 );
        gamePane.getChildren().add(shipOpponentBox);
        gamePane.getChildren().add(shipOpponent); //добавляем на поле
    }
    /**
     * Створення фону
     */
    public void createBackround(){
        gridPane1 = new GridPane();
        //квик напоминалка: ГридПэйн это что-то по типу таблицы, которую в нашем случае ты заполняешь картинками
        gridPane2 = new GridPane();
        String currentImage = BACKGROUND_IMAGE + (int)(Math.random() * 3) + ".png";
        //16 - потому что получается 4 на 4 фон (ширина фона по формуле ширина_экрана/ширина_пикчи)
        for (int i = 0 ; i < 16; i++){
            ImageView backgroundImage1 = new ImageView(currentImage);
            ImageView backgroundImage2 = new ImageView(currentImage);

            GridPane.setConstraints(backgroundImage1, i%4,i/4); //установка контейнеров
            GridPane.setConstraints(backgroundImage2, i%4,i/4);
            gridPane1.getChildren().add(backgroundImage1);
            gridPane2.getChildren().add(backgroundImage2);
        }

        gridPane2.setLayoutY(-720); //вторую панель выносим за карту
        gamePane.getChildren().addAll(gridPane1,gridPane2);
    }
    /**
     * Створення івентів для клаавіатури та миші
     */
    public void createListener(){

        gameStage.setOnCloseRequest(event -> {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error on closing");
            }
        });

        gameStage.setOnCloseRequest(event -> {
            try {
                in.close();
                out.close();
                socket.close();
                serverSocket.close();
            } catch (IOException e) {

            }
            menuStage.show();
            gameStage.close();
        });

        gameScene.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY) {
                shootPlayer(event);
                //shootSound.play();
                //shootSound.seek(Duration.ZERO);
            }
        });


        gameScene.setOnKeyPressed(event -> {    //стреляем на пробел
            if(event.getCode() == KeyCode.LEFT){
                isLeftKeyPressed = true;
                //ship.setLayoutX(ship.getX() - 5);
            }else if(event.getCode()== KeyCode.RIGHT){
                //ship.setLayoutX(ship.getX() - 5);
                isRightKeyPressed = true;
            }else if(event.getCode()== KeyCode.UP){
                //ship.setLayoutX(ship.getX() - 5);
                isUpKeyPressed = true;
            }else if(event.getCode()== KeyCode.DOWN){
                //ship.setLayoutX(ship.getX() - 5);
                isDownKeyPressed = true;
            }
        });
        gameScene.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.LEFT){
                isLeftKeyPressed = false;
                //ship.setLayoutX(ship.getX() - 5);
            }else if(event.getCode()== KeyCode.RIGHT){
                //ship.setLayoutX(ship.getX() - 5);
                isRightKeyPressed = false;
            }else if(event.getCode()== KeyCode.UP){
                //ship.setLayoutX(ship.getX() - 5);
                isUpKeyPressed = false;
            }else if(event.getCode()== KeyCode.DOWN){
                //ship.setLayoutX(ship.getX() - 5);
                isDownKeyPressed = false;
            }
        });
    }
    /**
     * Оновлення місцезнаходження космоліту гравця
     */
    public void movePlayerShip(){

        if(isRightKeyPressed && isDownKeyPressed){
            if(shipPlayer.getLayoutX()+shipPlayer.getImage().getWidth() < GAME_WIDTH && shipPlayer.getLayoutY() + shipPlayer.getImage().getHeight() < GAME_HEIGHT){
                shipPlayer.setLayoutX(shipPlayer.getLayoutX() + 3 );
                shipPlayerBox.setX(shipPlayerBox.getX() + 3);
                shipPlayer.setLayoutY(shipPlayer.getLayoutY() + 3);
                shipPlayerBox.setY(shipPlayerBox.getY() + 3);
                byte[] data = new byte[1+8*4];
                ByteBuffer.wrap(data).put(PLAYER_MOVE) .putDouble(shipPlayer.getLayoutX()) .putDouble(shipPlayer.getLayoutY()) .putDouble(shipPlayerBox.getX()) .putDouble(shipPlayerBox.getY());
                try {
                    //System.out.println("[CLIENT] moved player");
                    out.write(data);
                    out.flush();
                } catch (IOException e) {
                    //System.out.println("[CLIENT] ERROR MOVING " + e.getMessage());
                }

            }
        }else if(isLeftKeyPressed && isDownKeyPressed){
            if(shipPlayer.getLayoutX() > 0 && shipPlayer.getLayoutY() + shipPlayer.getImage().getHeight() < GAME_HEIGHT){
                shipPlayer.setLayoutX(shipPlayer.getLayoutX() - 3 );
                shipPlayerBox.setX(shipPlayerBox.getX() - 3);
                shipPlayer.setLayoutY(shipPlayer.getLayoutY() + 3);
                shipPlayerBox.setY(shipPlayerBox.getY() + 3);
                byte[] data = new byte[1+8*4];
                ByteBuffer.wrap(data).put(PLAYER_MOVE) .putDouble(shipPlayer.getLayoutX()) .putDouble(shipPlayer.getLayoutY()) .putDouble(shipPlayerBox.getX()) .putDouble(shipPlayerBox.getY());
                try {
                    //System.out.println("[CLIENT] moved player");
                    out.write(data);
                    out.flush();
                } catch (IOException e) {
                    //System.out.println("[CLIENT] ERROR MOVING " + e.getMessage());
                }

            }
        }else if(isRightKeyPressed && isUpKeyPressed){
            if(shipPlayer.getLayoutX()+shipPlayer.getImage().getWidth() < GAME_WIDTH && shipPlayer.getLayoutY() > 0){
                shipPlayer.setLayoutX(shipPlayer.getLayoutX() + 3 );
                shipPlayerBox.setX(shipPlayerBox.getX() + 3);
                shipPlayer.setLayoutY(shipPlayer.getLayoutY() - 3);
                shipPlayerBox.setY(shipPlayerBox.getY() - 3);
                byte[] data = new byte[1+8*4];
                ByteBuffer.wrap(data).put(PLAYER_MOVE) .putDouble(shipPlayer.getLayoutX()) .putDouble(shipPlayer.getLayoutY()) .putDouble(shipPlayerBox.getX()) .putDouble(shipPlayerBox.getY());
                try {
                    //System.out.println("[CLIENT] moved player");
                    out.write(data);
                    out.flush();
                } catch (IOException e) {
                    //System.out.println("[CLIENT] ERROR MOVING " + e.getMessage());
                }

            }
        }else if(isLeftKeyPressed && isUpKeyPressed){
            if(shipPlayer.getLayoutX() > 0 && shipPlayer.getLayoutY() > 0){
                shipPlayer.setLayoutX(shipPlayer.getLayoutX() - 3 );
                shipPlayerBox.setX(shipPlayerBox.getX() - 3);
                shipPlayer.setLayoutY(shipPlayer.getLayoutY() - 3);
                shipPlayerBox.setY(shipPlayerBox.getY() - 3);
                byte[] data = new byte[1+8*4];
                ByteBuffer.wrap(data).put(PLAYER_MOVE) .putDouble(shipPlayer.getLayoutX()) .putDouble(shipPlayer.getLayoutY()) .putDouble(shipPlayerBox.getX()) .putDouble(shipPlayerBox.getY());
                try {
                    //System.out.println("[CLIENT] moved player");
                    out.write(data);
                    out.flush();
                } catch (IOException e) {
                    //System.out.println("[CLIENT] ERROR MOVING " + e.getMessage());
                }

            }
        }else if(isLeftKeyPressed){
            if(shipPlayer.getLayoutX() > 0 ){
                shipPlayer.setLayoutX(shipPlayer.getLayoutX() - 4 );
                shipPlayerBox.setX(shipPlayerBox.getX() - 4);
                byte[] data = new byte[1+8*4];
                ByteBuffer.wrap(data).put(PLAYER_MOVE) .putDouble(shipPlayer.getLayoutX()) .putDouble(shipPlayer.getLayoutY()) .putDouble(shipPlayerBox.getX()) .putDouble(shipPlayerBox.getY());
                try {
                    //System.out.println("[CLIENT] moved player");
                    out.write(data);
                    out.flush();
                } catch (IOException e) {
                    //System.out.println("[CLIENT] ERROR MOVING " + e.getMessage());
                }

            }
        }else if(isRightKeyPressed){

            if(shipPlayer.getLayoutX()+shipPlayer.getImage().getWidth() < GAME_WIDTH ){
                shipPlayer.setLayoutX(shipPlayer.getLayoutX() + 4 );
                shipPlayerBox.setX(shipPlayerBox.getX() + 4);
                byte[] data = new byte[1+8*4];
                ByteBuffer.wrap(data).put(PLAYER_MOVE) .putDouble(shipPlayer.getLayoutX()) .putDouble(shipPlayer.getLayoutY()) .putDouble(shipPlayerBox.getX()) .putDouble(shipPlayerBox.getY());
                try {
                   // System.out.println("[CLIENT] moved player");
                    out.write(data);
                    out.flush();
                } catch (IOException e) {
                    //System.out.println("[CLIENT] ERROR MOVING" + e.getMessage());
                }
            }
        } else if(isDownKeyPressed){
            if(shipPlayer.getLayoutY() + shipPlayer.getImage().getHeight() < GAME_HEIGHT) {
                shipPlayer.setLayoutY(shipPlayer.getLayoutY() + 4);
                shipPlayerBox.setY(shipPlayerBox.getY() + 4);
                byte[] data = new byte[1+8*4];
                ByteBuffer.wrap(data).put(PLAYER_MOVE) .putDouble(shipPlayer.getLayoutX()) .putDouble(shipPlayer.getLayoutY()) .putDouble(shipPlayerBox.getX()) .putDouble(shipPlayerBox.getY());

                try {
                    //System.out.println("[CLIENT] moved player");
                    out.write(data);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }else if(isUpKeyPressed){
            if(shipPlayer.getLayoutY() > 0) {
                shipPlayer.setLayoutY(shipPlayer.getLayoutY() - 4);
                shipPlayerBox.setY(shipPlayerBox.getY() - 4);
                byte[] data = new byte[1+8*4];
                ByteBuffer.wrap(data).put(PLAYER_MOVE) .putDouble(shipPlayer.getLayoutX()) .putDouble(shipPlayer.getLayoutY()) .putDouble(shipPlayerBox.getX()) .putDouble(shipPlayerBox.getY());
                try {
                    //System.out.println("[CLIENT] moved player");
                    out.write(data);
                    out.flush();
                    //System.out.println("SENDED INFO");

                } catch (IOException e) {
                   // System.out.println("[CLIENT] ERROR MOVING" + e.getMessage());
                }
            }
        }

    }
    /**
     * Перевірка на колізії двох елементів
     * @param firstNode
     * @param secondNode
     * @return
     */
    public boolean checkCollision(Node firstNode, Node secondNode){
        if(firstNode.getBoundsInParent().intersects(secondNode.getBoundsInParent())){
            //System.out.println("yep");
            return true;
        }else{
            //System.out.println("no");
            return  false;
        }
    }
    /**
     * Перевірка того, чи завершена гра
     */
    public void checkVictory(){
        if(hpPlayer <= 0 ){

            try {
                in.close();
                out.close();
                socket.close();
                InfoLabel youLoose = new InfoLabel("ВИ ПРОГРАЛИ!");
                youLoose.setLayoutX((GAME_WIDTH - youLoose.getPrefWidth())/2);
                youLoose.setLayoutY(GAME_HEIGHT/3);
                gamePane.getChildren().add(youLoose);
                currentThread().stop();
            } catch (IOException e) {
                System.out.println("Error in victory");
            }
        }else if(hpOpponent <= 0){
            try {
                in.close();
                out.close();
                socket.close();
                InfoLabel youLoose = new InfoLabel("ВИ ПЕРЕМОГЛИ!");
                youLoose.setLayoutX((GAME_WIDTH - youLoose.getPrefWidth())/2);
                youLoose.setLayoutY(GAME_HEIGHT/3);
                gamePane.getChildren().add(youLoose);
                currentThread().stop();
            } catch (IOException e) {

                System.out.println("Error in victory");
            }
        }
    }
    /**
     * Початок роботи потіку для оновлення вікна гри
     */
    public  void startAnimation(){


        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                while (true) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            checkVictory();
                            infoPane.setVisible(false);
                            moveOpponentBullets();
                            movePlayerShip();
                            movePlayerBullets();
                        }
                    });

                    //shipPlayer.setLayoutX(shipPlayer.getLayoutX() + 0.1);
                    try {
                        sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    /**
     * Початок потіку для отримання даних від супротивника
     */
    public  void startConnectionListener() {
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                while (true) {

                    try {
                        if (in.available() > 0) {
                            byte[] data = new byte[1 + 8 * 4];
                            in.read(data);
                            switch (data[0]) {
                                case 1:
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            shipOpponent.setLayoutX(ByteBuffer.wrap(data).getDouble(1));
                                            shipOpponent.setLayoutY(ByteBuffer.wrap(data).getDouble(9));
                                            shipOpponentBox.setX(ByteBuffer.wrap(data).getDouble(17));
                                            shipOpponentBox.setY(ByteBuffer.wrap(data).getDouble(25));

                                        }
                                    });
                                    break;
                                case 2:
                                    //System.out.println("Player shooted");
                                    MultiplayerBullet opBullet = new MultiplayerBullet(ByteBuffer.wrap(data).getDouble(17), ByteBuffer.wrap(data).getDouble(25), 1);
                                    opBullet.setTarget(ByteBuffer.wrap(data).getDouble(1), ByteBuffer.wrap(data).getDouble(9));
                                    //System.out.println(opBullet.getTranslateX());
                                    //System.out.println(opBullet.getLayoutX());
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            gamePane.getChildren().add(opBullet);
                                        }
                                    });
                                    opponentBullets.add(opBullet);
                                    //shootSound.play();
                                    //shootSound.seek(Duration.ZERO);
                                    //opBullet.toFront();
                                    //System.out.println((double) infoGet[2] + " | " + (double) infoGet[3]);
                                    break;
                                default:
                                    System.out.println("Got smth strange");
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        //System.out.println("[SERVER] Error witch input steam: " + e.getMessage());
                    }

                }
            }
        }).start();

    }

}
