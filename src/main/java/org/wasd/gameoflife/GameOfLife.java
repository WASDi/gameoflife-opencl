package org.wasd.gameoflife;

import java.awt.image.BufferedImage;

public interface GameOfLife {

    void step();

    void renderImage(BufferedImage image);

    int getStepNum();

}
