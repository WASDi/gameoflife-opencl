package org.wasd;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.image.BufferedImage;
import java.util.List;

public class WebcamPictureTaker implements Runnable {

    public static final WebcamPictureTaker INSTANCE = new WebcamPictureTaker();

    private final Webcam webcam;
    private BufferedImage latestImage;

    private WebcamPictureTaker() {
        List<Webcam> webcams = Webcam.getWebcams();
        if (webcams.isEmpty()) {
            throw new IllegalStateException("No webcams !!!");
        }
        webcam = webcams.get(0);
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        System.out.println("opening webcam...");
        webcam.open();
        System.out.println("webcam open");
    }

    public BufferedImage getLatestImage() {
        synchronized (webcam) {
            return latestImage;
        }
    }

    public BufferedImage getFreshImage() {
        synchronized (webcam) {
            BufferedImage webcamImage = webcam.getImage();
            BufferedImage freshImage = new BufferedImage(webcamImage.getWidth(), webcamImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            freshImage.getGraphics().drawImage(webcamImage, 0, 0, null);
            return freshImage;
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (webcam) {
                latestImage = getFreshImage();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
