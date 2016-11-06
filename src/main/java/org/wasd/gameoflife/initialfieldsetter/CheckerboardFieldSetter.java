package org.wasd.gameoflife.initialfieldsetter;

public class CheckerboardFieldSetter implements InitialFieldSetter {
    @Override
    public void setFor(boolean[][] field) {
        for (int y = 0; y < field.length; y++) {
            int n = y;
            for (int x = 0; x < field[y].length; x++) {
                field[x][y] = n++ % 2 == 0;
            }
        }
    }
}
