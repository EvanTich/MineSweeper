public class Minesweeper {

    private int[][] mineNumber; // -1 = boom
    private byte[][] tileFlags; // first bit is if the tile is revealed, second bit is if the tile is flagged

    private int numberOfMines;

    private long startTimeMillis;

    private boolean hasLost; // if boom
    private int timeAtEnd; // in seconds

    public Minesweeper(int width, int height, int numberOfMines) {
        mineNumber = new int[height][width];
        tileFlags = new byte[height][width];

        startTimeMillis = -1;
        hasLost = false;
        timeAtEnd = -1;

        this.numberOfMines = numberOfMines;

        while(numberOfMines-- > 0) {
            int i, j;
            do {
                i = (int)(Math.random() * height);
                j = (int)(Math.random() * width);
            } while(mineNumber[i][j] == -1);

            mineNumber[i][j] = -1;
        }

        putNumbersOnField();
    }

    private void putNumbersOnField() {
        for(int i = 0; i < mineNumber.length; i++) {
            for(int j = 0; j < mineNumber[i].length; j++) {
                // get number of adjacent mines on this tile, skipping mines
                if(mineNumber[i][j] == -1)
                    continue;

                int adjacentMines = 0;
                for(int x = i - 1; x <= i + 1; x++)
                    for(int y = j - 1; y <= j + 1; y++)
                        if(x >= 0 && x < mineNumber.length && y >= 0 && y < mineNumber[i].length && mineNumber[x][y] == -1)
                            adjacentMines++;

                mineNumber[i][j] = adjacentMines;
            }
        }
    }

    public boolean hasLost() {
        return hasLost;
    }

    public boolean hasWon() {
        // I am going by the rule that if you clear every non-bomb tile, then you win
        for(int i = 0; i < mineNumber.length; i++)
            for(int j = 0; j < mineNumber[i].length; j++)
                if(!hasRevealedTile(i, j) && mineNumber[i][j] != -1)
                    return false;

        timeAtEnd = getCurrentPlayTime();
        return true;
    }

    public int bombsLeft() {
        int bombs = 0;
        for(int i = 0; i < mineNumber.length; i++)
            for(int j = 0; j < mineNumber[i].length; j++)
                if(!hasRevealedTile(i, j) && hasFlaggedTile(i, j))
                    bombs++;
        return numberOfMines - bombs;
    }

    public boolean hasRevealedTile(int r, int c) {
        return (tileFlags[r][c] & 1) == 1;
    }

    public boolean hasFlaggedTile(int r, int c) {
        return (tileFlags[r][c] & 2) == 2;
    }

    public int getTile(int r, int c) {
        return mineNumber[r][c];
    }

    public int revealTile(int r, int c) {
        if(r < 0 || r >= getHeight() || c < 0 || c >= getWidth() || hasRevealedTile(r, c))
            return -2; // error handling

        if(startTimeMillis == -1) {
            startTimeMillis = System.currentTimeMillis();
        }

        tileFlags[r][c] |= 1;
        if(getTile(r, c) == 0) {
            revealAdjacent(r, c);
        } else if(getTile(r, c) == -1) {
            hasLost = true;
            if(timeAtEnd == -1) { // if just lost
                timeAtEnd = getCurrentPlayTime();
                showAll();
            }
        }
        return mineNumber[r][c];
    }

    public void showAll() {
        for(int i = 0; i < mineNumber.length; i++)
            for(int j = 0; j < mineNumber[i].length; j++)
                revealTile(i, j);
    }

    /**
     * Reveals adjacent tiles (including the one selected)
     * @param r row
     * @param c column
     */
    private void revealAdjacent(int r, int c) {
        for(int i = r - 1; i <= r + 1; i++)
            for(int j = c - 1; j <= c + 1; j++)
                if(i >= 0 && i < mineNumber.length && j >= 0 && j < mineNumber[i].length)
                    revealTile(i, j);
    }

    public void flagTile(int r, int c) {
        tileFlags[r][c] ^= 2;

        if(startTimeMillis == -1) {
            startTimeMillis = System.currentTimeMillis();
        }
    }

    public int getTotalMines() {
        return numberOfMines;
    }

    public int getHeight() {
        return mineNumber.length;
    }

    public int getWidth() {
        return mineNumber[0].length;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public int getCurrentPlayTime() {
        if(startTimeMillis == -1)
            return 0;
        if(timeAtEnd != -1)
            return getTimeAtEnd();
        return (int) (System.currentTimeMillis() - startTimeMillis) / 1000;
    }

    public int getTimeAtEnd() {
        return timeAtEnd;
    }

    /**
     * purely for development ease
     */
    public void printBoard() {
        for(int i = 0; i < mineNumber.length; i++) {
            for (int j = 0; j < mineNumber[i].length; j++)
                System.out.print(getTile(i, j) + " ");
            System.out.println();
        }
    }

}
