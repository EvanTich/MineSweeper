import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private MinesweeperGUI gui;

    @Override
    public void start(Stage primaryStage) throws Exception {
        gui = new MinesweeperGUI(9, 9, 10);
        Group group = new Group();
        Scene scene = new Scene(group);
        group.getChildren().add(gui);

        primaryStage.setTitle("Minesweeper v.1");
        primaryStage.setScene(scene);


        // for the timer
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                gui.update();
            }
        }.start();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
