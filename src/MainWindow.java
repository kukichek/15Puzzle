import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MainWindow extends Application {
    private static int SETTINGS_PANEL_WIDTH = 300;
    private static int MIN_HEIGHT = 400;
    private static int MAX_HEIGHT = 600;
    private static int MAX_PREVIEW_WIDTH = 280;
    private static int MAX_PREVIEW_HEIGHT = 200;

    Puzzle15 puzzle15;
    Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("15 Puzzle");
        stage.setResizable(false);

        puzzle15 = new Puzzle15(this, MIN_HEIGHT, MAX_HEIGHT);
        ImageView imagePreview = new ImageView();

        Button startGameButton = new Button("Start Game");
        startGameButton.setPrefWidth(100);
        startGameButton.setOnAction(e -> {
            puzzle15.setShuffle();
            stage.sizeToScene();
        });

        BorderPane previewPane = new BorderPane();
        previewPane.setPrefWidth(SETTINGS_PANEL_WIDTH);
        previewPane.setMinHeight(MIN_HEIGHT);
        previewPane.setTop(imagePreview);
        previewPane.setCenter(getSettingsButtons(previewPane, puzzle15, imagePreview));
        previewPane.setBottom(startGameButton);
        previewPane.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setLeft(puzzle15);
        root.setRight(previewPane);

        stage.setScene(new Scene(root));
        stage.show();
    }

    public void showWinPanel() {
        ButtonType startNewGameButton = new ButtonType("Start New Game", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Congratulations! You win!",
                startNewGameButton,
                cancelButton);

        alert.setHeaderText(null);
        alert.setTitle("Win!");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.orElse(cancelButton) == startNewGameButton) {
            puzzle15.setShuffle();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Pane getSettingsButtons(BorderPane previewPane, Puzzle15 puzzle15, ImageView imagePreview) {
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.PNG", "*.jpg", "*.JPG", "*.jpeg", "*.JPEG");

        FileChooser fileChooser = new FileChooser();

        // TODO: Remove before building
        fileChooser.setInitialDirectory(new File("C:\\Users\\acer\\Downloads"));
        //

        fileChooser.getExtensionFilters().add(imageFilter);
        fileChooser.setSelectedExtensionFilter(imageFilter);

        Button openFileButton = new Button("Open Image");
        openFileButton.setDisable(true);
        openFileButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    File file = fileChooser.showOpenDialog(null);
                    Image image = SwingFXUtils.toFXImage(ImageIO.read(file), null);
                    imagePreview.setImage(image);
                    imagePreview.setPreserveRatio(true);
                    setDefaultImagePreview(previewPane);
                    previewPane.setTop(imagePreview);

                    if (image.getWidth() > MAX_PREVIEW_WIDTH) {
                        imagePreview.setFitWidth(MAX_PREVIEW_WIDTH);
                    }
                    if (image.getHeight() > MAX_PREVIEW_HEIGHT) {
                        imagePreview.setFitHeight(MAX_PREVIEW_HEIGHT);
                    }

                    puzzle15.setImage(image);
                    puzzle15.setShuffle();
                    stage.sizeToScene();
                }
                catch (IOException exception) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(null);
                    alert.setTitle("Open Image");
                    alert.setContentText("Unable to open an image, please, try again");

                    alert.showAndWait();
                }
            }
        });

        Spinner<Integer> columnValueSpinner = new Spinner(3, 7, 4);
        columnValueSpinner.setDisable(true);
        columnValueSpinner.setMaxWidth(60);
        columnValueSpinner.valueProperty().addListener(e -> {
            puzzle15.setColumnAmount(columnValueSpinner.getValue());
        });

        ToggleGroup group = new ToggleGroup();
        RadioButton ordinaryGameButton = new RadioButton("Ordinary game");
        ordinaryGameButton.setToggleGroup(group);
        ordinaryGameButton.setSelected(true);
        RadioButton customGameButton = new RadioButton("Custom game");
        customGameButton.setToggleGroup(group);

        ordinaryGameButton.setOnAction(e -> {
            openFileButton.setDisable(true);
            columnValueSpinner.setDisable(true);
            setDefaultImagePreview(previewPane);

            try {
                puzzle15.setDefaultImage();
                puzzle15.setColumnAmount(4);
            }
            catch (IOException exception) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText(null);
                alert.setTitle("Open Image");
                alert.setContentText("Unable to set default image, please, try again");

                alert.showAndWait();
            }
        });

        customGameButton.setOnAction(e -> {
            openFileButton.setDisable(false);
            columnValueSpinner.setDisable(false);
        });

        VBox buttonPane = new VBox(5);
        buttonPane.getChildren().addAll(ordinaryGameButton, customGameButton);
        FlowPane customSettingsPane = new FlowPane();
        customSettingsPane.setHgap(10);
        customSettingsPane.getChildren().addAll(openFileButton, columnValueSpinner);

        BorderPane finalPane = new BorderPane();
        finalPane.setMaxHeight(100);
        finalPane.setTop(buttonPane);
        finalPane.setBottom(customSettingsPane);

        return finalPane;
    }

    private void setDefaultImagePreview(BorderPane previewPane) {
        previewPane.setTop(null);
    }
}
