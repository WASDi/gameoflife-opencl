package org.wasd.gameoflife;

import org.wasd.jocl.impl.GameOfLifeOpenCL;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.Executors;

public class GameOfLifeGUI extends JFrame implements ActionListener {

    private static final int FIELD_SIZE_X = 32;
    private static final int FIELD_SIZE_Y = 32;
    private static final int PIXEL_SIZE = 14;
    private static final int IMAGE_SIZE_X = FIELD_SIZE_X * PIXEL_SIZE;
    private static final int IMAGE_SIZE_Y = FIELD_SIZE_Y * PIXEL_SIZE;
    private static final String TITLE = "Game Of Life";

    private final GameOfLife game;
    private final BufferedImage image;
    private final JLabel mainPanel;

    public GameOfLifeGUI(GameOfLife game) {
        super(TITLE);
        this.game = game;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        setResizable(false);
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
        //960x960, 100 körningar
        //cpu = 32322ms
        //v1 = 744ms
        //v2 = 738ms
        GameOfLife game;
        if (gpu) {
            //bästaste lösningen https://www.olcf.ornl.gov/tutorials/opencl-game-of-life/
            game = new GameOfLifeOpenCL(FIELD_SIZE_X, FIELD_SIZE_Y, PIXEL_SIZE,
                    Optional.of(new InitialFieldSetter()));
        } else {
            game = new GameOfLifeCPU(FIELD_SIZE_X, FIELD_SIZE_Y, PIXEL_SIZE,
                    Optional.of(new InitialFieldSetter()));
        }
        System.out.println("GPU == " + gpu);
        new GameOfLifeGUI(game);
    }
}
