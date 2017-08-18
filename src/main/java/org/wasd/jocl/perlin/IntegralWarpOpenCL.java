package org.wasd.jocl.perlin;

import org.wasd.jocl.core.KernelArgumentSetter;
import org.wasd.jocl.core.KernelFile;
import org.wasd.jocl.core.OpenCLBase;
import org.wasd.jocl.wrappers.image.OpenCLInputImage;
import org.wasd.jocl.wrappers.image.OpenCLOutputImage;

import java.awt.image.BufferedImage;

public class IntegralWarpOpenCL extends OpenCLBase {

    private final BufferedImage inputHostImage;
    private final BufferedImage outputHostImage;

    private final int sizeX;
    private final int sizeY;

    private OpenCLInputImage inputImage;
    private OpenCLOutputImage outputImage;

    public float step = 0f;

    public IntegralWarpOpenCL(BufferedImage inputImage, BufferedImage outputImage) {
        super(KernelFile.INTEGRAL_WARP, false);
        this.inputHostImage = inputImage;
        this.outputHostImage = outputImage;

        this.sizeX = outputHostImage.getWidth();
        this.sizeY = outputHostImage.getHeight();
    }

    @Override
    public void afterInitCL() {
        inputImage = createInputImage(inputHostImage);
        outputImage = createOutputImage(outputHostImage);
    }

    protected long[][] getGlobalWorkSizePerKernel() {
        return new long[][]{{sizeX, sizeY}};
    }

    @Override
    protected void beforeExecute(int kernelIndex) {
        KernelArgumentSetter argumentSetter = resetAndGetArgumentSetter(kernelIndex);
        argumentSetter.setArgMemObject(inputImage);
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
