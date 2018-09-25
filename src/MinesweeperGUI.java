import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MinesweeperGUI extends Canvas {

    public static final Color BACKGROUND_COLOR = Color.LIGHTGRAY;
    public static final Color GRID_COLOR = Color.DARKGRAY;
    public static final double GRID_LINE_WIDTH = 1;
    public static final Color RECT_COLOR = Color.BLACK;
    public static final Color RECT_STRING_COLOR = Color.WHITE;
    public static final Color STRING_COLOR = Color.BLACK;

    public static final Color FACE_COLOR = Color.YELLOW;
    public static final Color FACE_FEATURE_COLOR = Color.BLACK;
    public static final double FACE_SIZE_MULTIPLIER = .9;
    public static final double EYE_SIZE_DIVISOR = 6;

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

        if(game.hasLost() || game.hasWon())
            return;

        int r = (int)(e.getY() / tileSize) - 1;
        int c = (int)(e.getX() / tileSize) - 2;

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

        HBox group = new HBox(5);
        group.setPadding(new Insets(3));
        group.getChildren().addAll(
                newTextArea(width), newLabel(" by ", 25),
                newTextArea(height), newLabel(" grid with ", 65),
                newTextArea(numBombs), newLabel(" bombs.", 50)
        );

        Scene scene = new Scene(group);

        window.setTitle("Game Settings");
        window.setScene(scene);

        window.initModality(Modality.WINDOW_MODAL);
        window.initOwner(Main.getPrimaryStage());

        window.setX(Main.getPrimaryStage().getX() + Main.getPrimaryStage().getWidth());
        window.setY(Main.getPrimaryStage().getY());

        window.setOnCloseRequest(e -> {
            game = new Minesweeper(Integer.parseInt(width.get()), Integer.parseInt(height.get()), Integer.parseInt(numBombs.get()));
            fitWindowToGame();
        });

        window.showAndWait();

    }

    private TextArea newTextArea(SimpleStringProperty p) {
        TextArea textArea = new TextArea(p.get());
        textArea.setPrefSize(50, 20);
        textArea.setMaxHeight(25);
        p.bind(textArea.textProperty());
        textArea.textProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                textArea.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
        return textArea;
    }

    private Label newLabel(String s, int width) {
        Label l = new Label(s);
        l.setMinSize(width, 20);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    /**
     * Update after every tile press
     */
    public void update() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFont(font);

        drawGrid(gc);
        drawTiles(gc);

        // draw # of bombs left and other things
        gc.setFill(STRING_COLOR);
        gc.fillText(String.format("%03d         %03d" , game.bombsLeft(), game.getCurrentPlayTime()), tileSize * 5.075, tileSize / 1.45);

        if(game.hasLost()) {
            gc.setFill(STRING_COLOR);
            drawDeadFace(gc, tileSize * 6 - tileSize * (FACE_SIZE_MULTIPLIER - 1), 2); // x and y are good enough
        } else  {
            if(game.hasWon()) {
                drawWonFace(gc, tileSize * 6 - tileSize * (FACE_SIZE_MULTIPLIER - 1), 2);
                gc.setFill(STRING_COLOR);
            } else {
                drawSmile(gc, tileSize * 6 - tileSize * (FACE_SIZE_MULTIPLIER - 1), 2);
            }
        }

        drawRectWithString(gc, changeGameRect, "GAME");
        drawRectWithString(gc, resetRect, "RESET");
    }

    private void drawGrid(GraphicsContext gc) {
        // draw area/grid
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(GRID_LINE_WIDTH);
        for(int x = 2; x <= (game.getWidth() + 2); x++) {
            gc.strokeLine(x * tileSize, tileSize,
                    x * tileSize, tileSize * (1 + game.getHeight()));
        }

        for(int y = 1; y <= game.getHeight() + 1; y++) {
            gc.strokeLine(tileSize * 2, y * tileSize,
                    tileSize * (2 + game.getWidth()), y * tileSize);
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
            if(mines == -1) { // if there is a mine
                gc.setFill(Color.BLACK);
                gc.fillRect(x + 4, y + 4, tileSize - 8, tileSize - 8);
            } else if(mines != 0) {
                if(game.hasFlaggedTile(r, c) && game.hasLost()) {
                    // show x
                    drawX(gc, x + 4, y + 4, tileSize - 8);
                }

                gc.setFill(getNumberColor(mines));
                gc.fillText(mines + "", x + tileSize * 2 / 5f, y + tileSize / 2f);
            }
        } else if(game.hasFlaggedTile(r, c)) {
            gc.setFill(Color.RED);
            gc.fillRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
        }
    }

    private void drawSmile(GraphicsContext gc, double x, double y) {
        // (:))

        final double faceSize = tileSize * FACE_SIZE_MULTIPLIER;
        final double eyeSize = faceSize / EYE_SIZE_DIVISOR;
        final double eyeLocationY = y + faceSize * 5 / 16;

        gc.setFill(FACE_COLOR);
        gc.fillArc(x, y, faceSize, faceSize, 0, 360, ArcType.ROUND); // face
        gc.setFill(FACE_FEATURE_COLOR);
        gc.fillArc(x + faceSize / 4 - eyeSize / 4, eyeLocationY, eyeSize, eyeSize,
                0, 360, ArcType.ROUND); // left eye
        gc.fillArc(x + faceSize * 3 / 4 - eyeSize * 3 / 4, eyeLocationY, eyeSize, eyeSize,
                0, 360, ArcType.ROUND); // right eye
        gc.setStroke(FACE_FEATURE_COLOR);
        gc.setLineWidth(faceSize / 60);
        gc.strokeArc(x + faceSize / 4, y + faceSize * 5 / 8, faceSize / 2, faceSize / 6,
                200, 135, ArcType.OPEN); // smile
    }

    private void drawDeadFace(GraphicsContext gc, double x, double y) {
        // (X() two Xs

        final double faceSize = tileSize * FACE_SIZE_MULTIPLIER;
        final double eyeSize = faceSize / EYE_SIZE_DIVISOR;
        final double eyeLocationY  = y + faceSize * 5 / 16;

        gc.setFill(FACE_COLOR);
        gc.fillArc(x, y, faceSize, faceSize, 0, 360, ArcType.ROUND); // face
        gc.setStroke(FACE_FEATURE_COLOR);
        drawX(gc, x + faceSize / 4 - eyeSize / 4, eyeLocationY, eyeSize); // left eye
        drawX(gc, x + faceSize * 3 / 4 - eyeSize * 3 / 4, eyeLocationY, eyeSize); // right eye
        gc.setLineWidth(faceSize / 60);
        gc.strokeArc(x + faceSize / 4, y + faceSize * 3 / 4, faceSize / 2, faceSize / 6,
                30, 135, ArcType.OPEN); // frown
    }

    private void drawX(GraphicsContext gc, double x, double y, double size) {
        gc.strokeLine(x, y, x + size, y + size);
        gc.strokeLine(x + size, y, x, y + size);
    }

    private void drawWonFace(GraphicsContext gc, double x, double y) {
        // sunglasses boy with smile

        final double faceSize = tileSize * FACE_SIZE_MULTIPLIER;
        final double eyeSize = faceSize / EYE_SIZE_DIVISOR;
        final double eyeLocationY  = y + faceSize * 5 / 16;

        gc.setFill(FACE_COLOR);
        gc.fillArc(x, y, faceSize, faceSize, 0, 360, ArcType.ROUND); // face
        gc.setFill(FACE_FEATURE_COLOR);
        drawSunglass(gc, x + faceSize * 0.2 - eyeSize / 4, eyeLocationY, eyeSize, true);
        drawSunglass(gc, x + faceSize * 0.7 - eyeSize * 3 / 4, eyeLocationY, eyeSize, false);
        gc.fillRect(x + faceSize * 0.2, eyeLocationY, eyeSize * 3, eyeSize / 10); // sunglasses middle top
        gc.fillRect(x + faceSize * 0.2, eyeLocationY + eyeSize / 5, eyeSize * 3, eyeSize / 10); // sunglasses middle bottom
        gc.setStroke(FACE_FEATURE_COLOR);
        gc.setLineWidth(faceSize / 60);
        gc.strokeLine(x, y + faceSize / 2, x + faceSize * 0.2 - eyeSize / 4, eyeLocationY * 1.0495); // left ear thing
        gc.strokeLine(x + faceSize, y + faceSize / 2,
                x + faceSize * 0.7 + eyeSize * 3 / 4, eyeLocationY * 1.0495); // right ear thing

        gc.strokeArc(x + faceSize / 4, y + faceSize * 5 / 8, faceSize / 2, faceSize / 6,
                200, 135, ArcType.OPEN); // smile
    }

    private void drawSunglass(GraphicsContext gc, double x, double y, double eyeSize, boolean left) {
        // stupid, complex sunglasses...
        if(left) {
            gc.fillRect(x, y, eyeSize / 2, eyeSize / 3 + 1);
            gc.fillArc(x, y - eyeSize / 3, eyeSize, eyeSize * 4 / 3, 180, 90, ArcType.ROUND);
            gc.fillArc(x - eyeSize / 2 - 1, y - eyeSize, eyeSize * 2 + 1, eyeSize * 2, 270, 90, ArcType.ROUND);
        } else {
            gc.fillArc(x, y - eyeSize, eyeSize * 2 + 1, eyeSize * 2, 180, 90, ArcType.ROUND);
            gc.fillRect(x + eyeSize, y, eyeSize / 2, eyeSize /3 + 1);
            gc.fillArc(x + eyeSize / 2, y - eyeSize / 3, eyeSize, eyeSize * 4 / 3, 270, 90, ArcType.ROUND);
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
