package org.wasd.jocl.perlin;

import org.wasd.Util;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

public class SuperImage implements ChangeListener {

    private static final int SLIDER_MIN = -100;
    private static final int SLIDER_MAX = 100;

    private final SuperImageOpenCL openCLApplication;

    private final JLabel outputLabel;
    private final JSlider slider;
    private int sliderValue = 0;

    public SuperImage() {

        BufferedImage inputImage = Util.createBufferedImage("duck640.jpg");
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        outputLabel = new JLabel(new ImageIcon(outputImage));

        slider = new JSlider(SLIDER_MIN, SLIDER_MAX);
        slider.addChangeListener(this);

        JFrame frame = new JFrame("WASD OpenCL GUI");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(outputLabel, BorderLayout.CENTER);
        frame.add(slider, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);

        openCLApplication = new SuperImageOpenCL(inputImage, outputImage);
        openCLApplication.init();
        //startAnimation();
        recalculate();
    }

    private void recalculate() {
        openCLApplication.step = sliderValue / 100f;
        openCLApplication.execute();
        outputLabel.repaint();
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(SuperImage::new);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        int newValue = slider.getValue();
        if (sliderValue != newValue) {
            sliderValue = newValue;
            recalculate();
        }
    }
}
