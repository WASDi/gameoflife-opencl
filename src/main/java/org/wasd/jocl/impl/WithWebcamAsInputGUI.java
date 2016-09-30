package org.wasd.jocl.impl;

import org.wasd.Util;
import org.wasd.WebcamPictureTaker;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;

public class WithWebcamAsInputGUI {

    private static final int SLEEP_PER_FRAME = 16;

    private final BufferedImage inputImage;
    private final BufferedImage outputImage;
    private final WarpImpl openCLApplication;
    private final WebcamPictureTaker webcamPictureTaker;

    private final JLabel inputLabel;
    private final JLabel outputLabel;

    public WithWebcamAsInputGUI() {
        webcamPictureTaker = WebcamPictureTaker.INSTANCE;
        webcamPictureTaker.makeFreshImage(true);
        inputImage = webcamPictureTaker.getNewImageOrNull();
        outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        JPanel mainPanel = new JPanel(new GridLayout(1, 0));
        inputLabel = new JLabel(new ImageIcon(inputImage));
        mainPanel.add(inputLabel, BorderLayout.CENTER);
        outputLabel = new JLabel(new ImageIcon(outputImage));
        mainPanel.add(outputLabel, BorderLayout.CENTER);

        JFrame frame = new JFrame("WASD OpenCL GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        openCLApplication = new WarpImpl(inputImage, outputImage);
        openCLApplication.init();
        startAnimation();
    }

    private void startAnimation() {
        System.out.println("Starting animation...");

        Executors.newSingleThreadExecutor().submit(webcamPictureTaker);

        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                boolean wasUpdated = updateInputImage();
                if (wasUpdated) {
                    openCLApplication.updateInputImage(inputImage);
                }
                openCLApplication.execute();

                if (wasUpdated) {
                    inputLabel.repaint();
                }
                outputLabel.repaint();
                try {
                    Thread.sleep(SLEEP_PER_FRAME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
    }

    private boolean updateInputImage() {
        BufferedImage webcamImage = webcamPictureTaker.getNewImageOrNull();
        if (webcamImage == null) {
            return false;
        }
        inputImage.getGraphics().drawImage(webcamImage, 0, 0, null);
        //FIXME ovan tar 30ms, gånger 4 fps. 19% CPU med, 14% utan. Behövs ej om kerneln klarar rawformatet!
        return true;
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(WithWebcamAsInputGUI::new);
    }

}
