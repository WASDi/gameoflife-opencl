package org.wasd.gameoflife;

import java.util.Random;
import java.util.logging.Logger;

public class InitialFieldSetter {

    private static final Logger logger = Logger.getLogger(InitialFieldSetter.class.getName());

    public void setFor(boolean[][] field) {
        long seed = System.nanoTime();
//        seed = 1337;
        Random r = new Random(seed);
        logger.info("Seed: " + seed);
        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[x].length; y++) {
                field[x][y] = r.nextBoolean();
            }
        }
    }

}
