package org.wasd.jocl.impl;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import org.wasd.WebcamPictureTaker;
import org.wasd.jocl.core.OpenCL;
import org.wasd.jocl.impl.WarpImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.Executors;

public class WithWebcamAsInputGUI {

    private static final int SLEEP_PER_FRAME = 16;

    private final BufferedImage inputImage;
    private final BufferedImage outputImage;
    private final WarpImpl openCLApplication;
    private final WebcamPictureTaker webcam;

    public WithWebcamAsInputGUI() {
        webcam = WebcamPictureTaker.INSTANCE;
        inputImage = webcam.getFreshImage();
        outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);

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

        Executors.newSingleThreadExecutor().submit(() -> {
            openCLApplication.execute();
            outputComponent.repaint();

            while (true) {
                try {
                    Thread.sleep(SLEEP_PER_FRAME);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                openCLApplication.updateInputImage(webcam.getFreshImage());
                openCLApplication.execute();
                outputComponent.repaint();
            }
        });
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(WithWebcamAsInputGUI::new);
    }

}
