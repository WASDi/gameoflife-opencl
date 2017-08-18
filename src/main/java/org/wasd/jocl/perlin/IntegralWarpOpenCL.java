package org.wasd.jocl.perlin;

import org.jocl.Sizeof;
import org.wasd.jocl.core.KernelArgumentSetter;
import org.wasd.jocl.core.KernelFile;
import org.wasd.jocl.core.OpenCLBase;
import org.wasd.jocl.wrappers.OpenCLMemObject;
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

    private OpenCLMemObject displacements;

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

        displacements = createMemObject(sizeX, sizeY, Sizeof.cl_float2);
        float[] emptyBuffer = new float[2 * sizeX * sizeY];
        writeFloatBuffer(displacements, emptyBuffer);
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
        argumentSetter.setArgMemObject(displacements);
    }

    @Override
    protected void afterExecute(int kernelIndex) {
        readImage(outputImage);
    }

}
