package org.wasd.gameoflife;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class GameOfLifeCPU implements GameOfLife {

    private final int fieldSizeX;
    private final int fieldSizeY;
    private final int pixelSize;
    private final boolean[][] field;
    private int stepNum = 0;

    public GameOfLifeCPU(int fieldSizeX, int fieldSizeY, int pixelSize, Optional<InitialFieldSetter> initialFieldSetter) {
        this.fieldSizeX = fieldSizeX;
        this.fieldSizeY = fieldSizeY;
        this.pixelSize = pixelSize;
        field = new boolean[fieldSizeX][fieldSizeY];

        if (initialFieldSetter.isPresent()) {
            initialFieldSetter.get().setFor(field);
        }
    }

    @Override
    public void step() {
        int stepsPerStep = 1;
        for (int i = 0; i < stepsPerStep; i++) {
            stepUpdate();
            stepNum++;
        }
    }

    @Override
    public int getStepNum() {
        return stepNum;
    }

    public void renderImage(BufferedImage image) {
        long startTime = System.nanoTime();
        Graphics graphics = image.getGraphics();
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                graphics.setColor(field[x][y] ? Color.BLACK : Color.WHITE);
                graphics.fillRect(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
            }
        }
        long endTime = System.nanoTime() - startTime;
//        System.out.printf("Image render time: %.2fms\n", endTime / 1e6f);
    }

    private void stepUpdate() {
        boolean[][] nextField = new boolean[fieldSizeX][fieldSizeY];
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                nextField[x][y] = forEachPixel(x, y);
            }
        }
        for (int x = 0; x < fieldSizeX; x++) {
            System.arraycopy(nextField[x], 0, field[x], 0, fieldSizeY);
        }
    }

    private boolean forEachPixel(int x, int y) {
        int aliveNeighbours = 0;
        boolean currentStatus = field[x][y];

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                if (getPixel(x + dx, y + dy)) {
                    aliveNeighbours++;
                }
            }
        }
        return shouldIBeAlive(currentStatus, aliveNeighbours);
    }

    private boolean shouldIBeAlive(boolean currentStatus, int aliveNeighbours) {
        if (!currentStatus) {
            return aliveNeighbours == 3;
        }
        return aliveNeighbours == 2 || aliveNeighbours == 3;
    }

    private boolean getPixel(int x, int y) {
        int xPos = x % fieldSizeX;
        int yPos = y % fieldSizeY;
        if (xPos < 0) {
            xPos += fieldSizeX;
        }
        if (yPos < 0) {
            yPos += fieldSizeY;
        }
        return field[xPos][yPos];
    }
}
