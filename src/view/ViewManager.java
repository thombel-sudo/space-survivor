package view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;
import network.Client;
import network.Server;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Клас обробки вікна головного меню
 */
public class ViewManager {

    private static final int WIDTH = 1054;
    private static final int HEIGHT = 750;
    private AnchorPane mainPane;
    private Scene mainScene;
    private Stage mainStage;

    private final static int MENU_BUTTONS_START_X = 100;
    private final static int MENU_BUTTONS_START_Y = 150;


    private SpaceSurvivorSubscene helpSubscene;
    private SpaceSurvivorSubscene startGameSubscene;
    private SpaceSurvivorSubscene settingsSubscene;
    private SpaceSurvivorSubscene multiplayerSubscene;
    private SpaceSurvivorSubscene joinGameSubscene;
    private SpaceSurvivorSubscene SubsceneToHide;
    private TextField ipTextField;

    List<SpaceSurvivorButton>  menuButtons;
    List<ShipPicker> shipsList;

    private SHIP choosenShip;

    MediaPlayer note;
    private double volumeMusic = 1;
    private double volumeSounds = 1;

    /**
     * Ініціалізація класу
      */
    public ViewManager(){
        menuButtons = new ArrayList<>();
        mainPane = new AnchorPane();
        mainScene = new Scene(mainPane,WIDTH,HEIGHT);
        mainStage = new Stage();
        mainStage.setTitle("Space Survivor");
        mainStage.setResizable(false);
        Image gameIcon = new Image("model/resources/game_icon.png");
        mainStage.getIcons().add(gameIcon);
        mainStage.setScene(mainScene);
        createSubscenes();
        createButtons();
        createBackground();
        createLogo();
        //play_audio();
        setVolumeMusic(0.0);
        mainStage.setOnShowing(event -> {
            play_audio();
        });
        mainStage.setOnHiding(event -> {
            note.stop();
        });
 //       SpaceRunnerSubscene newSubscene = new SpaceRunnerSubscene();
//        newSubscene.setLayoutX(100);
//        newSubscene.setLayoutY(100);
//        mainPane.getChildren().add(newSubscene);
    }

    /**
     * Створення фонової музики
     */
    public void play_audio(){
        URL resource = getClass().getResource("/view/resources/sound/menu.wav");

        try {
            note = new MediaPlayer(new Media(resource.toURI().toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        note.setVolume(getVolumeMusic());
        note.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                note.seek(Duration.ZERO);
            }
        });
        note.setAutoPlay(true);

    }

    /**
     *
     * @return рівень гучності музики
     */
    public double getVolumeMusic() {
        return volumeMusic;
    }

    /**
     *
     * @return рівень гучності звуків
     */
    public double getVolumeSounds() {
        return volumeSounds;
    }

    /**
     * Встановлення гучності музики
     * @param volumeMusic
     */
    public void setVolumeMusic(double volumeMusic) {
        //note.setVolume(volumeMusic);
        this.volumeMusic = volumeMusic;
    }

    /**
     * Встановлення гучності звуків
     * @param volumeSounds
     */
    public void setVolumeSounds(double volumeSounds) {
        this.volumeSounds = volumeSounds;
    }

    /**
     * Створення всіх панелей меню
     */
    public void createSubscenes(){
        createSettingsSubscene();
        createHelpSubscene();
        createMultiplayerSubscene();
        createJoinGameSubscene();
        createShipChooserSubscene();
    }

    /**
     * Створення панелі для вибору космоліту
     */
    public void createShipChooserSubscene(){
        startGameSubscene = new SpaceSurvivorSubscene();
        mainPane.getChildren().add(startGameSubscene);
        InfoLabel chooseShipLabel = new InfoLabel("ОБЕРIТЬ КОСМОЛIТ");
        chooseShipLabel.setLayoutX(110);
        chooseShipLabel.setLayoutY(25);
        startGameSubscene.getPane().getChildren().add(chooseShipLabel);
        startGameSubscene.getPane().getChildren().add(createShipsToChoose());
        startGameSubscene.getPane().getChildren().add(createButtonTostart());
    }

    /**
     * Створення панелі налаштувань
     */
    public void createSettingsSubscene(){
        settingsSubscene = new SpaceSurvivorSubscene();
        mainPane.getChildren().add(settingsSubscene);
        Button settingButton = createSaveSettingsButton();
        InfoLabel settingsLabel = new InfoLabel("НАЛАШТУВАННЯ");
        InfoLabel musicVolumeLabel = new InfoLabel("МУЗИКА");
        InfoLabel soundVolumeLabel = new InfoLabel("ЕФЕКТИ");

        musicVolumeLabel.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        musicVolumeLabel.setPrefWidth(100);
        soundVolumeLabel.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        soundVolumeLabel.setPrefWidth(100);
        settingsLabel.setLayoutY(25);
        settingsLabel.setLayoutX((settingsSubscene.getWidth() - settingsLabel.getPrefWidth())/2);
        //System.out.println(settingsSubscene.getWidth() + " " + settingsLabel.getPrefWidth());

        Slider gameMusicSlider = new Slider(0,1,getVolumeMusic());
        Slider gameSoundSlider = new Slider(0,1,getVolumeSounds());

        gameMusicSlider.setPrefWidth(200);
        gameSoundSlider.setPrefWidth(200);
        //gameMusicSlider.setShowTickLabels(true);
        gameMusicSlider.setShowTickMarks(true);
        gameSoundSlider.setShowTickMarks(true);
        gameMusicSlider.setLayoutX((settingsSubscene.getWidth() - gameMusicSlider.getPrefWidth()*2));
        gameMusicSlider.setLayoutY(settingsSubscene.getHeight()/2 - 65);

        gameSoundSlider.setLayoutX((settingsSubscene.getWidth() - gameMusicSlider.getPrefWidth()*2));
        gameSoundSlider.setLayoutY(settingsSubscene.getHeight()/2 + 80 - settingsLabel.getPrefHeight());

        musicVolumeLabel.setLayoutY(gameMusicSlider.getLayoutY() - musicVolumeLabel.getPrefHeight()/2);
        soundVolumeLabel.setLayoutY(gameSoundSlider.getLayoutY() - soundVolumeLabel.getPrefHeight()/2);

        musicVolumeLabel.setLayoutX((gameMusicSlider.getLayoutX() - musicVolumeLabel.getPrefWidth())/2);
        soundVolumeLabel.setLayoutX((gameSoundSlider.getLayoutX() - soundVolumeLabel.getPrefWidth())/2);


        settingButton.setOnAction(event -> {
            setVolumeMusic(gameMusicSlider.getValue());
            setVolumeSounds(gameSoundSlider.getValue());
            note.setVolume(getVolumeMusic());
            //note.stop();
            //play_audio();
        });

        settingsSubscene.getPane().getChildren().addAll(settingsLabel,gameMusicSlider,gameSoundSlider,musicVolumeLabel,soundVolumeLabel,settingButton);
    }

    /**
     * Створення панелі допомоги
     */
    public void createHelpSubscene(){

        helpSubscene = new SpaceSurvivorSubscene();
        mainPane.getChildren().add(helpSubscene);
        InfoLabel helpLabel = new InfoLabel("ДОПОМОГА");
        helpLabel.setLayoutY(25);
        helpLabel.setLayoutX((helpSubscene.getWidth() - helpLabel.getPrefWidth())/2);

        ImageView helpImage = new ImageView(new Image("view/resources/help/help.png"));
        helpImage.setLayoutY(85);
        helpImage.setLayoutX((helpSubscene.getWidth() - helpImage.getImage().getWidth())/2);
        Label controls = new Label("Для переміщення космоліту використовуйте стрілки на вашій клавіатурі\n\n" +
                "Для пострілу використовуйте пробіл");
        controls.setWrapText(true);
        controls.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 19));
        controls.setTextFill(Color.WHITE);
        controls.setPrefWidth(400);
        controls.setLayoutX((helpSubscene.getWidth() - controls.getPrefWidth())/2);
        controls.setLayoutY(helpImage.getLayoutY() + controls.getPrefHeight() + helpImage.getImage().getHeight());
        controls.setTextAlignment(TextAlignment.CENTER);
        helpSubscene.getPane().getChildren().addAll(helpLabel,helpImage, controls);
    }

    /**
     * Створення панелі мультиплеєру
     */
    public void createMultiplayerSubscene(){
        multiplayerSubscene = new SpaceSurvivorSubscene();
        //добавляем на главное окно (все так же по анологии делаются )
        mainPane.getChildren().add(multiplayerSubscene);
        InfoLabel helpLabel = new InfoLabel("2 ГРАВЦІ");
        helpLabel.setLayoutY(25);
        helpLabel.setLayoutX((multiplayerSubscene.getWidth() - helpLabel.getPrefWidth())/2);

        multiplayerSubscene.getPane().getChildren().addAll(helpLabel,createHostGameButton(),createJoinGameButton());
    }

    /**
     * Створення панелі для підключення до серверу
     */
    public void createJoinGameSubscene(){
        joinGameSubscene = new SpaceSurvivorSubscene();
        mainPane.getChildren().add(joinGameSubscene);
        InfoLabel joinLabel = new InfoLabel("ПІД'ЄДНАТИСЬ ДО ГРИ");
        joinLabel.setLayoutY(25);
        joinLabel.setLayoutX((joinGameSubscene.getWidth() - joinLabel.getPrefWidth())/2);


        Label ipLabel = new Label("Введіть IP адресу:");
        ipLabel.setWrapText(true);
        ipLabel.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 19));
        ipLabel.setTextFill(Color.WHITE);
        ipLabel.setPrefWidth(200);
        ipLabel.setLayoutX((joinGameSubscene.getWidth() - ipLabel.getPrefWidth())/2);
        ipLabel.setLayoutY(joinLabel.getLayoutY() + 80);
        ipLabel.setTextAlignment(TextAlignment.CENTER);

        ipTextField = new TextField();
        ipTextField.setPromptText("Введіть IP...");
        ipTextField.setPrefWidth(300);
        ipTextField.setLayoutX((joinGameSubscene.getWidth() - ipTextField.getPrefWidth())/2);
        ipTextField.setLayoutY(ipLabel.getLayoutY() + 40);
        ipTextField.setEffect(new DropShadow(5,Color.BLACK));


        joinGameSubscene.getPane().getChildren().addAll(joinLabel, ipLabel,ipTextField,createConnectGameButton() );
    }

    /**
     * Відображення потрібної панелі
     * @param newSubScene
     */
    public void showSubscene(SpaceSurvivorSubscene newSubScene){
        if(SubsceneToHide != null){
            SubsceneToHide.moveSubscene();
        }
        newSubScene.moveSubscene();
        SubsceneToHide = newSubScene;
    }

    /**
     *
     * @return набіл космолітів для вибору
     */
    public HBox createShipsToChoose(){
        HBox box = new HBox();
        box.setSpacing(20);
        shipsList =  new ArrayList<>();
        for(SHIP ship : SHIP.values()){
            ShipPicker shipToPick = new ShipPicker(ship);
            shipsList.add(shipToPick);
            box.getChildren().add(shipToPick);
            shipToPick.setOnMouseClicked(event -> {
                for(ShipPicker ship2 : shipsList){
                    ship2.setIsCircleChoosen(false);
                }
                shipToPick.setIsCircleChoosen(true);
                choosenShip = shipToPick.getShip();
            });
        }
        box.setLayoutX(300-(118*2));
        box.setLayoutY(100);
        return box;
    }

    /**
     *
     * @return вікно головного меню
     */
    public Stage getMainStage(){
        return mainStage;
    }

    /**
     * Створення нової кнопки для головного меню
     * @param button
     */
    public void addMenuButton(SpaceSurvivorButton button){
        button.setLayoutX(MENU_BUTTONS_START_X);
        button.setLayoutY(MENU_BUTTONS_START_Y + menuButtons.size() * 100);
        menuButtons.add(button);
        mainPane.getChildren().add(button);
    }

    /**
     * Створення всіх кнопок
     */
    public void createButtons(){
        createNewGameButton();
        createMultGameButton();
        createSettingsButton();
        createHelpButton();
        createExitButton();
    }

    /**
     * Створення кнопки нової гри
     */
    public void createNewGameButton(){
        SpaceSurvivorButton button = new SpaceSurvivorButton("ГРАТИ");
        addMenuButton(button);
        button.setOnAction(event -> {
            showSubscene(startGameSubscene);
        });
    }

    /**
     * Створення кнопки мультиплеєру
     */
    public void createMultGameButton(){
        SpaceSurvivorButton button = new SpaceSurvivorButton("2 ГРАВЦI");
        addMenuButton(button);
        button.setOnAction(event -> {
            showSubscene(multiplayerSubscene);
        });
    }

    /**
     * Створення кнопки налаштувань
     */
    public void createSettingsButton(){
        SpaceSurvivorButton button = new SpaceSurvivorButton("НАЛАШТУВАННЯ");
        addMenuButton(button);
        button.setOnAction(event -> {
            showSubscene(settingsSubscene);
        });
    }

    /**
     * Створення кнопки інформації
     */
    public void createHelpButton(){
        SpaceSurvivorButton button = new SpaceSurvivorButton("ДОПОМОГА");
        addMenuButton(button);
        button.setOnAction(event -> {
            showSubscene(helpSubscene);
        });
    }

    /**
     * Створення кнопки виходу
     */
    public void createExitButton(){
        SpaceSurvivorButton button = new SpaceSurvivorButton("ВИХIД");
        button.setOnAction(event -> {
            System.exit(0);
        });
        addMenuButton(button);
    }

    /**
     * Створення кнопки для збереження налаштувань
     * @return кнопку налаштувань
     */
    public SpaceSurvivorButton createSaveSettingsButton(){
        SpaceSurvivorButton save = new SpaceSurvivorButton("ЗБЕРЕГТИ");
        save.setLayoutY(300);
        save.setLayoutX((settingsSubscene.getWidth()-save.getWidth())/3);
        return save;
    }

    /**
     * Створення кнопки для початку одиночної гри
     * @return кнопку гри
     */
    public SpaceSurvivorButton createButtonTostart(){
        SpaceSurvivorButton start = new SpaceSurvivorButton("ГРАТИ");
        start.setLayoutY(300);
        start.setLayoutX((startGameSubscene.getWidth()-start.getWidth())/3);
        //System.out.println(start.getLayoutX());
        start.setOnAction(event -> {
            if(choosenShip != null){
                GameViewManager newGame = new GameViewManager(this);
                newGame.createNewGame(mainStage,choosenShip);
                note.stop();
            }
        });
        return start;
    }

    /**
     * Створення кнопки для хосту\сервера гри
     * @return кнопку хосту
     */
    public SpaceSurvivorButton createHostGameButton(){
        SpaceSurvivorButton createHostGame = new SpaceSurvivorButton("НОВИЙ СЕРВЕР");
        createHostGame.setLayoutY(multiplayerSubscene.getHeight()/2 - 30 - createHostGame.getPrefHeight());
        createHostGame.setLayoutX((multiplayerSubscene.getWidth()-createHostGame.getWidth())/3);
        //System.out.println(start.getLayoutX());
        createHostGame.setOnAction(event -> {
            Server newGame = new Server(this, this.getMainStage() );


        });
        return createHostGame;
    }

    /**
     * Створення кнопки для відображення панелі вводу IP адреси
     * @return кнопку зміни вікна
     */
    public SpaceSurvivorButton createJoinGameButton(){
        SpaceSurvivorButton createJoinGameButton = new SpaceSurvivorButton("ПІД'ЄДНАТИСЬ");
        createJoinGameButton.setLayoutY(multiplayerSubscene.getHeight()/2 + createJoinGameButton.getPrefHeight());
        createJoinGameButton.setLayoutX((multiplayerSubscene.getWidth()-createJoinGameButton.getWidth())/3);
        //System.out.println(start.getLayoutX());
        createJoinGameButton.setOnAction(event -> {
            showSubscene(joinGameSubscene);
        });
        return createJoinGameButton;
    }

    /**
     * Створює кнопку для підключення до мережевої гри
     * @return кнопку підключення
     */
    public SpaceSurvivorButton createConnectGameButton(){
        SpaceSurvivorButton connectGameButton = new SpaceSurvivorButton("ПІД'ЄДНАТИСЬ");
        connectGameButton.setLayoutY(joinGameSubscene.getHeight()/2 + connectGameButton.getPrefHeight());
        connectGameButton.setLayoutX((joinGameSubscene.getWidth()-connectGameButton.getWidth())/3);
        //System.out.println(start.getLayoutX());
        connectGameButton.setOnAction(event -> {
            try {
                Client client = new Client(this, this.getMainStage(), ipTextField.getText());
            }catch(Exception ex){
                //System.out.println("this shieeet" + ex);
                this.getMainStage().show();
            }
        });
        return connectGameButton;
    }


    /**
     * Створення фону
     */
    public void createBackground(){
        Image image = new Image("view/resources/background.png",256,256,false,true);
        mainPane.setBackground(new Background
                (new BackgroundImage
                        (image, BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,null)));
    }

    /**
     * Створення назви гри
     */
    public void createLogo(){
        ImageView image = new ImageView("view/resources/text.png");
        image.setLayoutX(WIDTH - image.getImage().getWidth());
        image.setLayoutY(0);
        mainPane.getChildren().add(image);
        image.setOnMouseEntered(event -> {
            image.setEffect(new DropShadow(20, Color.DARKBLUE));
        });

        image.setOnMouseExited(event -> {
            image.setEffect(null);
        });
    }
}
