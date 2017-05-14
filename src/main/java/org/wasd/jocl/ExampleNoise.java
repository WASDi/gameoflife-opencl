package org.wasd.jocl;

import org.wasd.jocl.core.OpenCL;
import org.wasd.jocl.impl.ExampleNoiseImpl;
import org.wasd.jocl.impl.SandboxImpl;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;

public class ExampleNoise {

    private static final int SLEEP_PER_FRAME = 20;

    private final BufferedImage outputImage;
    private final OpenCL openCLApplication;

    public ExampleNoise() {
        outputImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);

        JLabel outputLabel = new JLabel(new ImageIcon(outputImage));

        JFrame frame = new JFrame("WASD OpenCL GUI");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(outputLabel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        openCLApplication = new ExampleNoiseImpl(outputImage);
        openCLApplication.init();
        startAnimation(outputLabel);
    }

    private void startAnimation(final Component outputComponent) {
        System.out.println("Starting animation...");

        //ExecutorService?
        Thread thread = new Thread(() -> {
            while (true) {
                openCLApplication.execute();
                outputComponent.repaint();

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
        SwingUtilities.invokeLater(ExampleNoise::new);
    }

}
