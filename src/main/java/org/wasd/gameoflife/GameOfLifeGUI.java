package org.wasd.gameoflife;

import org.wasd.gameoflife.initialfieldsetter.CheckerboardFieldSetter;
import org.wasd.gameoflife.initialfieldsetter.RandomFieldSetter;
import org.wasd.jocl.impl.GameOfLifeOpenCL;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.Executors;

public class GameOfLifeGUI extends JFrame implements ActionListener {

    private static final int FIELD_SIZE = 1000;
    private static final int FIELD_SIZE_X = FIELD_SIZE;
    private static final int FIELD_SIZE_Y = FIELD_SIZE;
    private static final int PIXEL_SIZE = 1;
    private static final int IMAGE_SIZE_X = FIELD_SIZE_X * PIXEL_SIZE;
    private static final int IMAGE_SIZE_Y = FIELD_SIZE_Y * PIXEL_SIZE;
    private static final String TITLE = "Game Of Life";

    private final GameOfLife game;
    private final BufferedImage image;
    private final JLabel mainPanel;

    public GameOfLifeGUI(GameOfLife game) {
        super(TITLE);
        this.game = game;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(null);

        image = new BufferedImage(IMAGE_SIZE_X, IMAGE_SIZE_Y, BufferedImage.TYPE_INT_RGB); //TODO other type?
        game.renderImage(image);

        mainPanel = new JLabel(new ImageIcon(image));
        mainPanel.setBounds(10, 10, IMAGE_SIZE_X, IMAGE_SIZE_Y);

        JButton button = new JButton("Step");
        button.setBounds(IMAGE_SIZE_X / 2 - 25, IMAGE_SIZE_Y + 20, 70, 30);
        button.addActionListener(this);

        add(mainPanel);
        add(button);

        setSize(IMAGE_SIZE_X + 20, IMAGE_SIZE_Y + 80);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (ONE_CLICK_MEANS_ONLY_ONE_RUN) {
            step();
            return;
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                step();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        ((JButton) e.getSource()).setEnabled(false);
    }

    private void step() {
        long startTime = System.nanoTime();

        game.step();
        game.renderImage(image);

        long endTime = System.nanoTime() - startTime;
        System.out.printf("Total time: %.2fms\n", endTime / 1e6f);

        mainPanel.repaint();
        setTitle(TITLE + ", iteration " + game.getStepNum());
    }

    private static final boolean ONE_CLICK_MEANS_ONLY_ONE_RUN = true;

    public static void main(String[] args) {
        boolean gpu = true;
        //960x960, 1000 körningar
        //cpu = 32322ms
        //GPU = 1000+ms (no optimization) (32x)
        //v1 = 744ms (local work groups) (43x)
        //v2 = 733ms (one work item per memory instead of per cell) (44x)
        //v2 = 454ms (remove modulo) (71x)
        //v3 = 630ms (with byte instead of int???)

        //CPU = about 32ms per frame, 31fps
        //best = about 0.45ms per frame, 2200fps

        //BENCHMARKs
        // without global memory read = 332ms (-122ms)
        // without aliveNeighbours count = 356ms (-98ms)
        //    without both = 341ms (???)
        //        AND ALSO without global memory write = 150ms
        // without global memory write = 268ms
        // empty step method but render image = 33ms
        // ONLY read global to local = 367ms (-10 without barrier)
        // ignore read global to local = 307ms (samma som första?)

        //Varför är byte/char långsammare?
        //totalt = +176ms
        //inläsning från global till local = speedup 26ms mindre
        //utan global write = 276ms ungefär samma tid <-- boven?
        //without aliveNeighbours count = 363, också ungefär samma <-- boven?
        //Om de ovan blir ungefär samma med char, borde vara boven. För att ta bort dem ger större speedup
        //Så det var de som tog störst extratid vid char

        //Next up, undersök padding och övriga saker som berör performance
        GameOfLife game;
        if (gpu) {
            //bästaste lösningen https://www.olcf.ornl.gov/tutorials/opencl-game-of-life/
            game = new GameOfLifeOpenCL(FIELD_SIZE_X, FIELD_SIZE_Y, PIXEL_SIZE, new RandomFieldSetter());
        } else {
            game = new GameOfLifeCPU(FIELD_SIZE_X, FIELD_SIZE_Y, PIXEL_SIZE,
                    Optional.of(new RandomFieldSetter()));
        }
        System.out.println("GPU == " + gpu);
        new GameOfLifeGUI(game);
    }
}
