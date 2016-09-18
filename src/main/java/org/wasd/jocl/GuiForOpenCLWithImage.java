package org.wasd.jocl;

import org.wasd.Util;
import org.wasd.jocl.core.OpenCL;
import org.wasd.jocl.impl.WarpImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GuiForOpenCLWithImage {

    private static final int SIZE = 640;
    private static final int SLEEP_PER_FRAME = 16;

    private final BufferedImage inputImage;
    private final BufferedImage outputImage;
    private final OpenCL openCLApplication;

    public GuiForOpenCLWithImage() {
        String fileName = "duck" + SIZE + ".jpg";

        inputImage = Util.createBufferedImage(fileName);
        outputImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

        JPanel mainPanel = new JPanel(new GridLayout(1, 0));
        JLabel inputLabel = new JLabel(new ImageIcon(inputImage));
        mainPanel.add(inputLabel, BorderLayout.CENTER);
        JLabel outputLabel = new JLabel(new ImageIcon(outputImage));
        mainPanel.add(outputLabel, BorderLayout.CENTER);

        JFrame frame = new JFrame("WASD OpenCL GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        openCLApplication = new WarpImpl(inputImage, outputImage);
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
        SwingUtilities.invokeLater(GuiForOpenCLWithImage::new);
    }

}
