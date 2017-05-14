package org.wasd.jocl.impl;

import org.jocl.Sizeof;
import org.wasd.jocl.core.KernelArgumentSetter;
import org.wasd.jocl.core.KernelFile;
import org.wasd.jocl.core.OpenCLBase;
import org.wasd.jocl.wrappers.OpenCLMemObject;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ExampleNoiseImpl extends OpenCLBase {

    private final BufferedImage outputHostImage;

    private final int sizeX;
    private final int sizeY;

    private OpenCLMemObject outputMemObject;

    private float step = 0f;

    public ExampleNoiseImpl(BufferedImage outputImage) {
        super(KernelFile.EXAMPLE_NOISE, false);
        this.outputHostImage = outputImage;

        this.sizeX = outputHostImage.getWidth();
        this.sizeY = outputHostImage.getHeight();
    }

    @Override
    public void afterInitCL() {
        outputMemObject = createMemObject(sizeX, sizeY, Sizeof.cl_float);
    }

    protected long[][] getGlobalWorkSizePerKernel() {
        return new long[][]{{sizeX, sizeY}};
    }

    @Override
    protected void beforeExecute(int kernelIndex) {
        step += .1f;

        KernelArgumentSetter argumentSetter = resetAndGetArgumentSetter(kernelIndex);
        argumentSetter.setArgMemObject(outputMemObject);
        argumentSetter.setArgInt(sizeX);
        argumentSetter.setArgInt(sizeY);
        argumentSetter.setArgFloat(1f / sizeX);
        argumentSetter.setArgFloat(1f / sizeY);
        argumentSetter.setArgFloat(step);
    }

    @Override
    protected void afterExecute(int kernelIndex) {
        float output[] = readFloatBuffer(outputMemObject);
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                float v = output[x + y * sizeX];
                outputHostImage.setRGB(x, y, new Color((int) v, (int) v, (int) v).getRGB());
            }
        }
    }
}
