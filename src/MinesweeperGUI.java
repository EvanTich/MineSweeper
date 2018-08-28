import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MinesweeperGUI extends Canvas {

    public static final Color RECT_COLOR = Color.BLACK;
    public static final Color RECT_STRING_COLOR = Color.WHITE;

    private Minesweeper game;

    private double tileSize;
    private Font font;
    private Rectangle resetRect;
    private Rectangle changeGameRect;

    public MinesweeperGUI(int width, int height, int numberOfMines) {
        game = new Minesweeper(width, height, numberOfMines);

        tileSize = 25; // initial tile size needed
        font = new Font(0); // set in resize
        resetRect = new Rectangle(); // set in resize
        changeGameRect = new Rectangle(); // set in resize

        this.setOnMouseClicked(this::onMouseClicked);

        resize((width + 4) * tileSize, (height + 4) * tileSize);
    }

    private void onMouseClicked(MouseEvent e) {
        if(resetRect.contains(e.getX(), e.getY()))
            reset();
        else if(changeGameRect.contains(e.getX(), e.getY()))
            changeGame();

        // FIXME: r, c wrong near the boundaries
        int r = (int)((e.getY() - tileSize) / tileSize);
        int c = (int)((e.getX() - tileSize * 2) / tileSize);

        if(r < 0 || r >= game.getHeight() || c < 0 || c >= game.getWidth())
            return;

        if(e.getButton() == MouseButton.PRIMARY && !game.hasRevealedTile(r, c)) {
            game.revealTile(r, c);
        } else if(e.getButton() == MouseButton.SECONDARY) {
            game.flagTile(r, c);
        }

        update();
    }

    @Override
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
        tileSize = (width > height ? height / (game.getHeight() + 4) : width / (game.getWidth() + 4));

        font = new Font(tileSize / 2);

        resetRect.setX(game.getWidth() * tileSize);
        resetRect.setY(game.getHeight() * tileSize + tileSize);
        resetRect.setWidth(tileSize * 2);
        resetRect.setHeight(tileSize);

        changeGameRect.setX((game.getWidth() - 3) * tileSize);
        changeGameRect.setY(game.getHeight() * tileSize + tileSize);
        changeGameRect.setWidth(tileSize * 2);
        changeGameRect.setHeight(tileSize);
    }

    public void fitWindowToGame() {
        Stage window = Main.getPrimaryStage();
        window.setWidth((game.getWidth() + 4) * tileSize);
        window.setHeight((game.getHeight() + 4) * tileSize);
    }

    public void reset() {
        game = new Minesweeper(game.getWidth(), game.getHeight(), game.getTotalMines());
    }

    public void changeGame() {
        // open gui for changing game values (w, h, # of mines)
        Stage window = new Stage();

        // set up the scene
        SimpleStringProperty width = new SimpleStringProperty(game.getWidth() + "");
        SimpleStringProperty height = new SimpleStringProperty(game.getHeight() + "");
        SimpleStringProperty numBombs = new SimpleStringProperty(game.getTotalMines() + "");

        TextArea textWidth = new TextArea(width.get());
        TextArea textHeight = new TextArea(height.get());
        TextArea textBombs = new TextArea(numBombs.get());

        width.bind(textWidth.textProperty());
        height.bind(textHeight.textProperty());
        numBombs.bind(textBombs.textProperty());

        // note: bad, but works
        final ChangeListener<String> numOnly = (observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    textWidth.setText(newValue.replaceAll("[^\\d]", ""));
                    textHeight.setText(newValue.replaceAll("[^\\d]", ""));
                    textBombs.setText(newValue.replaceAll("[^\\d]", ""));
                }
        };

        textWidth.textProperty().addListener(numOnly);
        textHeight.textProperty().addListener(numOnly);
        textBombs.textProperty().addListener(numOnly);

        // FIXME: bad ui look
        StackPane group = new StackPane();
        group.getChildren().addAll(
                new HBox(textWidth, new Label(" x "),
                        textHeight, new Label("Bombs: "), textBombs) /* stuff*/);

        Scene scene = new Scene(group);

        window.setTitle("Game Settings");
        window.setScene(scene);

        window.initModality(Modality.WINDOW_MODAL);
        window.initOwner(Main.getPrimaryStage());

        window.setX(Main.getPrimaryStage().getX() + Main.getPrimaryStage().getWidth());
        window.setY(Main.getPrimaryStage().getY());
        window.setWidth(200);
        window.setHeight(100);

        window.setOnCloseRequest(e -> {
            game = new Minesweeper(Integer.parseInt(width.get()), Integer.parseInt(height.get()), Integer.parseInt(numBombs.get()));
            fitWindowToGame();
        });

        window.showAndWait();

    }

    /**
     * Update after every tile press
     */
    public void update() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFont(font);

        drawGrid(gc);
        drawTiles(gc);

        // draw # of bombs left and other things at the bottom
        gc.setFill(Color.BLACK);
        // TODO: make this look better + put at the top of the screen
        gc.fillText(game.bombsLeft() + (game.hasLost() ? " ;( " : " :) ") +
                        (game.hasLost() ? game.getTimeAtLoss() : game.getCurrentPlayTime()),
                tileSize * 2, resetRect.getY() + tileSize / 1.45);

        drawRectWithString(gc, changeGameRect, "GAME");
        drawRectWithString(gc, resetRect, "RESET");

        gc.setFill(Color.BLACK);
        if(game.hasLost()) {
            gc.fillText("You Lose!", tileSize * 2, tileSize / 1.45);
        } else if(game.hasWon()) {
            gc.fillText("You Win!", tileSize * 2, tileSize / 1.45);
        }
    }

    private void drawGrid(GraphicsContext gc) {
        // draw area/grid
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setFill(Color.DARKGRAY);
        for(int x = 2; x <= (game.getWidth() + 2); x++) {
            gc.strokeLine(x * tileSize, tileSize, x * tileSize, tileSize * (1 + game.getHeight()));
        }

        for(int y = 1; y <= game.getHeight() + 1; y++) {
            gc.strokeLine(tileSize * 2, y * tileSize, tileSize * (2 + game.getWidth()), y * tileSize);
        }
    }

    private void drawTiles(GraphicsContext gc) {
        // draw individual tiles
        for(int i = 0; i < game.getHeight(); i++)
            for(int j = 0; j < game.getWidth(); j++)
                drawTile(gc, i, j);
    }

    private void drawTile(GraphicsContext gc, int r, int c) {
        double x = (c + 2) * tileSize, y = (r + 1) * tileSize;

        if(game.hasRevealedTile(r, c)) {
            gc.setFill(Color.WHITESMOKE);
            gc.fillRect(x + 1, y + 1, tileSize - 2, tileSize - 2);

            int mines = game.getTile(r, c);
            if(mines == -1) {
                gc.setFill(Color.BLACK);
                gc.fillRect(x + 4, y + 4, tileSize - 8, tileSize - 8);
            } else if(mines != 0) {
                gc.setFill(getNumberColor(mines));
                gc.fillText(mines + "", x + tileSize * 2 / 5f, y + tileSize / 2f);
            }
        } else if(game.hasFlaggedTile(r, c)) {
            gc.setFill(Color.RED);
            gc.fillRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
        }
    }

    private void drawRectWithString(GraphicsContext gc, Rectangle rect, String str) {
        gc.setFill(RECT_COLOR);
        gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        gc.setFill(RECT_STRING_COLOR);
        gc.fillText(str, rect.getX() + tileSize / 3.75, rect.getY() + tileSize / 1.45);
    }

    private Color getNumberColor(int num) {
        switch(num) {
            default: return Color.BLACK; // unknown #s and 6s are black
            case 1: return Color.BLUE;
            case 2: return Color.GREEN;
            case 3: return Color.RED; // idk from here on out
            case 4: return Color.VIOLET;
            case 5: return Color.CORAL;
        }
    }
}
