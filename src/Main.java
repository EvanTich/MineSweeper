import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private MinesweeperGUI gui;
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        gui = new MinesweeperGUI(9, 9, 10);
        Group group = new Group();
        Scene scene = new Scene(group);
        group.getChildren().add(gui);

        Main.primaryStage = primaryStage;

        primaryStage.setTitle("Minesweeper v.5");
        primaryStage.setScene(scene);

        ChangeListener<Number> sizeListener = (ob, oldVal, newVal) -> {
            gui.resize(primaryStage.getWidth(), primaryStage.getHeight());
        };

        primaryStage.widthProperty().addListener(sizeListener);
        primaryStage.heightProperty().addListener(sizeListener);

        // for the timer
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                gui.update();
            }
        }.start();

        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
