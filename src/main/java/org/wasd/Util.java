package org.wasd;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_event;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Util {

    public static BufferedImage createBufferedImage(String fileName) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int sizeX = image.getWidth();
        int sizeY = image.getHeight();

        BufferedImage result = new BufferedImage(
                sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
        Graphics g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

    public static long timeForClEvent(cl_event event) {
        long startTime[] = new long[1];
        long endTime[] = new long[1];
        CL.clGetEventProfilingInfo(
                event, CL.CL_PROFILING_COMMAND_START,
                Sizeof.cl_ulong, Pointer.to(startTime), null);
        CL.clGetEventProfilingInfo(
                event, CL.CL_PROFILING_COMMAND_END,
                Sizeof.cl_ulong, Pointer.to(endTime), null);

        return endTime[0] - startTime[0];
    }

    public static boolean imagesEqual(BufferedImage imgA, BufferedImage imgB) {
        if (imgA.getWidth() == imgB.getWidth() && imgA.getHeight() == imgB.getHeight()) {
            int width = imgA.getWidth();
            int height = imgA.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

}
