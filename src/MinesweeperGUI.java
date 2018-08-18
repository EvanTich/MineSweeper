import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

public class MinesweeperGUI extends Canvas {

    public static final int TILE_SIZE = 25;

    private Minesweeper game;

    private long time; // millis

    private boolean firstPlay;

    private boolean justLost;

    public MinesweeperGUI(int width, int height, int numberOfMines) {
        super((width + 4) * TILE_SIZE, (height + 4) * TILE_SIZE);
        maxWidth(getWidth()); maxHeight(getHeight());
        minWidth(getWidth()); minHeight(getHeight());
        reset(width, height, numberOfMines);

        this.setOnMouseClicked(e -> {
            if(e.getX() > getWidth() - 100 && e.getY() > getHeight() - 50)
                reset(game.getWidth(), game.getHeight(), game.getNumberOfMines());

            int r = (int) (e.getY() - TILE_SIZE) / TILE_SIZE;
            int c = (int) (e.getX() - TILE_SIZE * 2) / TILE_SIZE;

            if(r < 0 || r >= game.getHeight() || c < 0 || c >= game.getWidth())
                return;

            if(e.getButton() == MouseButton.PRIMARY && !game.hasRevealedTile(r, c)) {
                game.revealTile(r, c);
                if(firstPlay) {
                    firstPlay = false;
                    time = System.currentTimeMillis();
                }
            } else if(e.getButton() == MouseButton.SECONDARY) {
                game.flagTile(r, c);
            }

            update();
        });
    }

    public void reset(int width, int height, int mines) {
        game = new Minesweeper(width, height, mines);
        firstPlay = true;
        time = -1;
        justLost = false;
    }

    public long getStartTime() {
        return time;
    }

    public int getTimePlaying() {
        if(time == -1)
            return 0;
        return (int) (System.currentTimeMillis() - time) / 1000;
    }

    /**
     * Update after every tile press
     */
    public void update() {
        GraphicsContext gc = getGraphicsContext2D();

        // draw area/grid
        gc.setFill(Color.LIGHTGRAY);
//        gc.fillRect(TILE_SIZE * 2, TILE_SIZE, TILE_SIZE * game.getWidth(), TILE_SIZE * game.getHeight());
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setFill(Color.DARKGRAY);
        for(int x = 2; x <= (game.getWidth() + 2); x++) {
            gc.strokeLine(x * TILE_SIZE, TILE_SIZE, x * TILE_SIZE, TILE_SIZE * (1 + game.getHeight()));
        }

        for(int y = 1; y <= game.getHeight() + 1; y++) {
            gc.strokeLine(TILE_SIZE * 2, y * TILE_SIZE, TILE_SIZE * (2 + game.getWidth()), y * TILE_SIZE);
        }

        // draw individual tiles
        for(int i = 0; i < game.getHeight(); i++)
            for(int j = 0; j < game.getWidth(); j++)
                drawTile(gc, i, j);

        // draw # of bombs left and other things at the bottom
        gc.setFill(Color.BLACK);
        gc.fillText(game.bombsLeft() + (game.hasLost() ? " ;( " : " :) ") + getTimePlaying(), TILE_SIZE * 2, getHeight() - TILE_SIZE * 2);

        gc.fillRect(getWidth() - 100, getHeight() - 50, 100, 50);
        gc.setFill(Color.WHITE);
        gc.fillText("RESET", getWidth() - 80, getHeight() - 25);

        if(game.hasLost()) {
            if(!justLost) {
                game.showAll();
                justLost = true;
            }
            gc.fillText("You Lose!", TILE_SIZE * 2, 10);
        } else if(game.hasWon()) {
            gc.fillText("You Win!", TILE_SIZE * 2, 10);
        }
    }

    private void drawTile(GraphicsContext gc, int r, int c) {
        int x = (c + 2) * TILE_SIZE, y = (r + 1) * TILE_SIZE;

        if(game.hasRevealedTile(r, c)) {
            gc.setFill(Color.WHITESMOKE);
            gc.fillRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);

            int mines = game.getTile(r, c);
            if(mines == -1) {
                gc.setFill(Color.BLACK);
                gc.fillRect(x + 4, y + 4, TILE_SIZE - 8, TILE_SIZE - 8);
            } else if(mines != 0) {
                gc.setFill(getNumberColor(mines));
                gc.fillText(mines + "", x + TILE_SIZE * 2 / 5f, y + TILE_SIZE / 2f);
            }
        } else if(game.hasFlaggedTile(r, c)) {
            gc.setFill(Color.RED);
            gc.fillRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        }
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
