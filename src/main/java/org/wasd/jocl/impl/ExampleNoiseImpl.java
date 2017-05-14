package org.wasd.jocl.impl;

import org.wasd.jocl.core.KernelArgumentSetter;
import org.wasd.jocl.core.KernelFile;
import org.wasd.jocl.core.OpenCLBase;
import org.wasd.jocl.wrappers.image.OpenCLOutputImage;

import java.awt.image.BufferedImage;

public class ExampleNoiseImpl extends OpenCLBase {

    private final BufferedImage outputHostImage;

    private final int sizeX;
    private final int sizeY;

    private OpenCLOutputImage outputImage;


    private float step = 0f;

    public ExampleNoiseImpl(BufferedImage outputImage) {
        super(KernelFile.EXAMPLE_NOISE, false);
        this.outputHostImage = outputImage;

        this.sizeX = outputHostImage.getWidth();
        this.sizeY = outputHostImage.getHeight();
    }

    @Override
    public void afterInitCL() {
        outputImage = createOutputImage(outputHostImage);

    }

    protected long[][] getGlobalWorkSizePerKernel() {
        return new long[][]{{sizeX, sizeY}};
    }

    @Override
    protected void beforeExecute(int kernelIndex) {
        step += .03f;

        KernelArgumentSetter argumentSetter = resetAndGetArgumentSetter(kernelIndex);
        argumentSetter.setArgMemObject(outputImage);
        argumentSetter.setArgInt(sizeX);
        argumentSetter.setArgInt(sizeY);
        argumentSetter.setArgFloat(step);
    }

    @Override
    protected void afterExecute(int kernelIndex) {
        readImage(outputImage);
    }

}
