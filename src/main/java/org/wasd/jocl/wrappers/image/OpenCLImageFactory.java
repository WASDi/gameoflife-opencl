package org.wasd.jocl.wrappers.image;

import org.jocl.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class OpenCLImageFactory {

    private static final long INPUT_FLAGS = CL.CL_MEM_READ_ONLY | CL.CL_MEM_USE_HOST_PTR;
    private static final long OUTPUT_FLAGS = CL.CL_MEM_WRITE_ONLY;
    private static final cl_image_format IMAGE_FORMAT;

    static {
        IMAGE_FORMAT = new cl_image_format();
        IMAGE_FORMAT.image_channel_order = CL.CL_RGBA;
        IMAGE_FORMAT.image_channel_data_type = CL.CL_UNSIGNED_INT8;
    }

    public static OpenCLInputImage createInputImage(cl_context clContext, BufferedImage inputImage) {
        int sizeX = inputImage.getWidth();
        int sizeY = inputImage.getHeight();

        DataBufferInt dataBufferSrc =
                (DataBufferInt) inputImage.getRaster().getDataBuffer();
        int dataSrc[] = dataBufferSrc.getData();

        cl_mem imageMemory = createImage2D(clContext, INPUT_FLAGS, sizeX, sizeY,
                Pointer.to(dataSrc), sizeX * Sizeof.cl_uint);

        return new OpenCLInputImage(sizeX, sizeY, imageMemory);
    }

    public static OpenCLOutputImage createOutputImage(cl_context clContext, BufferedImage outputImage) {
        int sizeX = outputImage.getWidth();
        int sizeY = outputImage.getHeight();

        cl_mem imageMemory = createImage2D(clContext, OUTPUT_FLAGS, sizeX, sizeY, null, 0);

        return new OpenCLOutputImage(sizeX, sizeY, imageMemory, outputImage);
    }

    private static cl_mem createImage2D(cl_context clContext, long flags, int sizeX, int sizeY, Pointer pointerToHost, int rowPitch) {
        return CL.clCreateImage2D(
                clContext, flags,
                new cl_image_format[]{IMAGE_FORMAT}, sizeX, sizeY,
                rowPitch, pointerToHost, null);
    }
}
