package network;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Bullet;
import model.InfoLabel;
import model.MultiplayerBullet;
import view.ViewManager;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Клас обробки користувача
 */
public class Client extends Thread{

    private AnchorPane gamePane;
    private Scene gameScene;
    private Stage gameStage;
    private ViewManager rootWindow;

    private Stage menuStage;


    private ImageView shipPlayer;
    private Rectangle shipPlayerBox;

    private ImageView shipOpponent;
    private Rectangle shipOpponentBox;


    private boolean isLeftKeyPressed;
    private boolean isRightKeyPressed;
    private boolean isUpKeyPressed;
    private boolean isDownKeyPressed;

    private static final int GAME_WIDTH = 600;
    private static final int GAME_HEIGHT = 720;

    private AnimationTimer gameTimer;

    private GridPane gridPane1;
    private GridPane gridPane2;
    private final static String BACKGROUND_IMAGE = "view/resources/gameBackgrounds/";
    private InputStream in = null;
    private OutputStream  out = null;
    private static final byte PLAYER_MOVE = 1, SHOOT_PLAYER = 2;
    private List<MultiplayerBullet> playerBullets = new ArrayList<>();

    private List<MultiplayerBullet> opponentBullets = new ArrayList<>();
    private int port = 8001;
    private InetAddress inetAddress;
    private ServerSocket serverSocket;
    private Socket socket;

    private int hpPlayer = 5;
    private int hpOpponent = 5;

    /**
     * Ініціалізація класу
     * @param root
     * @param menuStage
     * @param ipAddress
     */
    public Client(ViewManager root, Stage menuStage, String ipAddress) {
        port = 8001;
        try  {
            socket = new Socket(ipAddress, port);
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка піключення");
            alert.setHeaderText("Не вдалося підключитись до серверу. Спробуйте іншу IP адрессу");
            alert.setContentText("Заданий сервер не існує");
            alert.showAndWait();

        }
        if(socket.isConnected()){
            System.out.println("connected");
        }
        try {
            socket.setTcpNoDelay(true);
            //out = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        rootWindow = root;
        gamePane = new AnchorPane();
        gameScene = new Scene(gamePane,GAME_WIDTH,GAME_HEIGHT);
        gameStage = new Stage();
        gameStage.setResizable(false);
        gameStage.setScene(gameScene);
        gameStage.setTitle("Онлайн гра");
        Image gameIcon = new Image("model/resources/game_icon.png");
        gameStage.getIcons().add(gameIcon);
        this.menuStage = menuStage;
        this.menuStage.hide();
        createListener();
        createBackround();
        createPlayerShip();
        createOpponentShip();
        startAnimation();
        startConnectionListener();
        gameStage.show();
        start();
    }

    /**
     * Вистріл гравця
     * @param event
     */
    public void shootPlayer(MouseEvent event){

        MultiplayerBullet bullet = new MultiplayerBullet(shipPlayer.getLayoutX() + shipPlayer.getImage().getWidth() / 2, shipPlayer.getLayoutY() +  shipPlayer.getImage().getHeight()/2 , 1);
        bullet.setTarget(event.getSceneX(),event.getSceneY());
        playerBullets.add(bullet);  //добавляем в лист пули
        gamePane.getChildren().add(bullet); // добавляем на экран
        shipPlayer.toFront();

        byte[] data = new byte[1+8*4];
        ByteBuffer.wrap(data).put(SHOOT_PLAYER) .putDouble(event.getSceneX()) .putDouble(event.getSceneY()) .putDouble(bullet.getLayoutX()) .putDouble(bullet.getLayoutY());
        try {
            System.out.println("[CLIENT] shoted player");
            out.write(data);
            out.flush();
        } catch (IOException e) {
            System.out.println("some shit occured");
        }

        //System.out.println(event.getSceneX() + " | " + event.getScreenX()  + " | " +  event.getX());

    }

    /**
     * Оновлення місцезнаходження знарядів гравця
     */
    public void movePlayerBullets(){
        for(MultiplayerBullet bullet : playerBullets){ //проходимся по всему списку
            //System.out.println(bullet.getLayoutY());
            if(checkCollision(bullet,shipOpponentBox)){
                    hpOpponent--;
                bullet.setIsDestroyed(true); //устанавливаем что пулю можно удалить с списка пуль
                gamePane.getChildren().remove(bullet); //удаляем пулю с экрана
            }else if(bullet.getLayoutY() < -5 || bullet.getLayoutY() > GAME_HEIGHT + 5 || bullet.getLayoutX() < -5 || bullet.getLayoutX() > GAME_WIDTH + 5){ //выход за границу
                //System.out.println("-------------------BULLET OUT OF RANGE");
                bullet.setIsDestroyed(true); //устанавливаем что пулю можно удалить с списка пуль
                gamePane.getChildren().remove(bullet); //удаляем пулю с экрана
            }else{ //иначе двигаем выше
                bullet.move();
                shipPlayer.toFront();
            }

        }
        playerBullets.removeIf(Bullet::getExist); //удаление всех пуль которые вышли за границу
    }

    /**
     * Оновлення місцезнаходження знарядів супротивника
     */
    public void moveOpponentBullets(){
        for(MultiplayerBullet bullet : opponentBullets){ //проходимся по всему списку

            if(checkCollision(bullet,shipPlayerBox)){
                hpPlayer--;
                bullet.setIsDestroyed(true); //устанавливаем что пулю можно удалить с списка пуль
                gamePane.getChildren().remove(bullet); //удаляем пулю с экрана
            }else if(bullet.getLayoutY() < -5 || bullet.getLayoutY() > GAME_HEIGHT + 5 || bullet.getLayoutX() < -5 || bullet.getLayoutX() > GAME_WIDTH + 5){ //выход за границу
                //System.out.println("---------------------||||"+bullet.getLayoutY());
                bullet.setIsDestroyed(true); //устанавливаем что пулю можно удалить с списка пуль
                // System.out.println("enemy bullet out of range");
                gamePane.getChildren().remove(bullet); //удаляем пулю с экрана
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


        shipPlayer = new ImageView(new Image("view/resources/multiplayerShips/opponentShip.png")); //устанавливаем картинку
        shipPlayerBox = new Rectangle(70,70, Color.TRANSPARENT);
        //shipPlayerBox.setFill(Color.BLACK);
        shipPlayer.setLayoutY(GAME_HEIGHT / 2); //страшные формулы для позиции
        shipPlayer.setLayoutX(GAME_WIDTH - shipPlayer.getImage().getWidth() );
        shipPlayerBox.setX(shipPlayer.getLayoutX() + (shipPlayer.getImage().getWidth() - shipPlayerBox.getWidth())/2);
        shipPlayerBox.setY(shipPlayer.getLayoutY() + (shipPlayer.getImage().getHeight() - shipPlayerBox.getHeight())/2 );
        gamePane.getChildren().add(shipPlayerBox);
        gamePane.getChildren().add(shipPlayer); //добавляем на поле
    }

    /**
     * Створення космоліту супротивника
     */
    public void createOpponentShip(){

        shipOpponent = new ImageView(new Image("view/resources/multiplayerShips/playerShip.png")); //устанавливаем картинку
        shipOpponentBox = new Rectangle(70,70, Color.TRANSPARENT);
        //shipPlayerBox.setFill(Color.BLACK);
        shipOpponent.setLayoutY(GAME_HEIGHT / 2); //страшные формулы для позиции
        shipOpponent.setLayoutX(10);
        shipOpponentBox.setX(shipOpponent.getLayoutX() + (shipOpponent.getImage().getWidth() - shipOpponentBox.getWidth())/2);
        shipOpponentBox.setY(shipOpponent.getLayoutY() + (shipOpponent.getImage().getHeight() - shipOpponentBox.getHeight())/2 );
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
                in.close();
                out.close();
                socket.close();
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
                   // System.out.println("[CLIENT] moved player");
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
            if(shipPlayer.getLayoutX()+shipPlayer.getImage().getWidth() < GAME_WIDTH && shipPlayer.getLayoutY()  > 0){
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
                    //System.out.println("[CLIENT] moved player");
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
                    //System.out.println("[CLIENT] ERROR MOVING" + e.getMessage());
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
        try {
            out = socket.getOutputStream();
            in = socket.getInputStream();
        } catch (IOException e) {
            System.out.println("Problem in getting out stream");
        }
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                while (true) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            checkVictory();
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
                    try{
                        if(in.available() > 0){
                            byte[] data = new byte[1+8*4];
                            in.read(data);
                            switch (data[0]){
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
                                    MultiplayerBullet opBullet = new MultiplayerBullet(ByteBuffer.wrap(data).getDouble(17),ByteBuffer.wrap(data).getDouble(25),0 );
                                    opBullet.setTarget(ByteBuffer.wrap(data).getDouble(1),ByteBuffer.wrap(data).getDouble(9));
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
                    }catch(Exception e){
                        //System.out.println("[CLIENT] Problem in input stream " + e.getMessage());
                    }
                }
            }
        }).start();

    }

}