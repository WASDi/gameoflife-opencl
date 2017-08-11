package org.wasd.jocl.recursively;

import org.wasd.Util;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

public class PickRandomPixelRecursively {

    private final PickRandomPixelRecursivelyOpenCL openCLApplication;

    private final JLabel outputLabel;

    private static final int SLEEP_PER_FRAME = 20;


    public PickRandomPixelRecursively() {

        BufferedImage inputImage = Util.createBufferedImage("duck640.jpg");
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        outputLabel = new JLabel(new ImageIcon(outputImage));

        JFrame frame = new JFrame("PickRandomPixelRecursively");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(outputLabel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        openCLApplication = new PickRandomPixelRecursivelyOpenCL(inputImage, outputImage);
        openCLApplication.init();
        startAnimation();
    }

    private void startAnimation() {
        System.out.println("Starting animation...");

        //ExecutorService?
        Thread thread = new Thread(() -> {
            while (true) {
                openCLApplication.execute();
                outputLabel.repaint();

                try {
                    Thread.sleep(SLEEP_PER_FRAME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(PickRandomPixelRecursively::new);
    }
}
