package org.wasd.jocl.recursively;

import org.jocl.CL;
import org.jocl.Sizeof;
import org.wasd.jocl.core.KernelArgumentSetter;
import org.wasd.jocl.core.KernelFile;
import org.wasd.jocl.core.OpenCLBase;
import org.wasd.jocl.wrappers.OpenCLMemObject;
import org.wasd.jocl.wrappers.image.OpenCLInputImage;
import org.wasd.jocl.wrappers.image.OpenCLOutputImage;

import java.awt.image.BufferedImage;
import java.util.Random;

public class PickRandomPixelRecursivelyOpenCL extends OpenCLBase {

    private final BufferedImage inputHostImage;
    private final BufferedImage outputHostImage;

    private final int sizeX;
    private final int sizeY;

    private OpenCLInputImage inputImage;
    private OpenCLOutputImage outputImage;

    public int step = 0;

    private static final Random RANDOM = new Random();
    private OpenCLMemObject seedMemory;


    public PickRandomPixelRecursivelyOpenCL(BufferedImage inputImage, BufferedImage outputImage) {
        super(KernelFile.PICK_RANDOM_PIXEL_RECURSIVELY, false);
        this.inputHostImage = inputImage;
        this.outputHostImage = outputImage;

        this.sizeX = outputHostImage.getWidth();
        this.sizeY = outputHostImage.getHeight();
    }

    @Override
    public void afterInitCL() {
        inputImage = createInputImage(inputHostImage);
        outputImage = createOutputImage(outputHostImage);

        seedMemory = createMemObject(sizeX, sizeY, Sizeof.cl_int);

        int[] buffer = new int[sizeX * sizeY];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = RANDOM.nextInt();
        }
        writeIntBuffer(seedMemory, buffer);
    }

    protected long[][] getGlobalWorkSizePerKernel() {
        return new long[][]{{sizeX, sizeY}};
    }

    @Override
    protected void beforeExecute(int kernelIndex) {
        step++;

        KernelArgumentSetter argumentSetter = resetAndGetArgumentSetter(kernelIndex);
        argumentSetter.setArgMemObject(inputImage);
        argumentSetter.setArgMemObject(outputImage);
        argumentSetter.setArgInt(sizeX);
        argumentSetter.setArgInt(sizeY);
        argumentSetter.setArgInt(step);
        argumentSetter.setArgMemObject(seedMemory);

    }

    @Override
    protected void afterExecute(int kernelIndex) {
        readImage(outputImage);

        CL.clReleaseMemObject(inputImage.getPrimitiveMemObject());
        inputImage = createInputImage(outputHostImage);
    }

}
