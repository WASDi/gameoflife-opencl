package org.wasd.jocl.wrappers.image;

import org.jocl.Pointer;
import org.jocl.cl_mem;
import org.wasd.jocl.wrappers.OpenCLMemObject;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class OpenCLOutputImage extends OpenCLMemObject {

    private final Pointer pointerToHostImage;

    public OpenCLOutputImage(int sizeX, int sizeY, cl_mem clImage, BufferedImage hostImage) {
        super(sizeX, sizeY, clImage);

        DataBufferInt dataBufferDst =
                (DataBufferInt) hostImage.getRaster().getDataBuffer();
        int dataDst[] = dataBufferDst.getData();
        pointerToHostImage = Pointer.to(dataDst);
    }

    public Pointer getPointerToHostImage() {
        return pointerToHostImage;
    }
}
