package org.wasd.jocl.impl;

import org.wasd.jocl.core.KernelArgumentSetter;
import org.wasd.jocl.core.KernelFile;
import org.wasd.jocl.core.OpenCLBase;
import org.wasd.jocl.wrappers.image.OpenCLInputImage;
import org.wasd.jocl.wrappers.image.OpenCLOutputImage;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class WarpImpl extends OpenCLBase {

    private final BufferedImage inputHostImage;
    private final BufferedImage outputHostImage;

    private OpenCLInputImage inputImage;
    private OpenCLOutputImage outputImage;

    private float step = 0f;

    public WarpImpl(BufferedImage inputImage, BufferedImage outputImage) {
        super(KernelFile.WARP, false);
        this.inputHostImage = inputImage;
        this.outputHostImage = outputImage;
    }

    @Override
    protected Map<String, String> getBuildArguments() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("SHRINK", "6.0f");
        return arguments;
    }

    @Override
    public void afterInitCL() {
        inputImage = createInputImage(inputHostImage);
        outputImage = createOutputImage(outputHostImage);
    }

    public void updateInputImage(BufferedImage newImageData) {
        writeImage(inputImage, newImageData);
    }

    protected long[][] getGlobalWorkSizePerKernel() {
        return new long[][]{{inputImage.getSizeX(), inputImage.getSizeY()}};
    }

    @Override
    protected void beforeExecute(int kernelIndex) {
        step += 0.01f;

        KernelArgumentSetter argumentSetter = resetAndGetArgumentSetter(kernelIndex);
        argumentSetter.setArgMemObject(inputImage);
        argumentSetter.setArgMemObject(outputImage);
        argumentSetter.setArgInt(inputImage.getSizeX());
        argumentSetter.setArgInt(inputImage.getSizeY());
        argumentSetter.setArgFloat(step);
    }

    @Override
    protected void afterExecute(int kernelIndex) {
        readImage(outputImage);
    }
}
