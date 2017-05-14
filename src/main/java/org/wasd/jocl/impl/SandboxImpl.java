package org.wasd.jocl.impl;

import org.wasd.jocl.core.KernelArgumentSetter;
import org.wasd.jocl.core.KernelFile;
import org.wasd.jocl.core.OpenCLBase;
import org.wasd.jocl.wrappers.image.OpenCLInputImage;
import org.wasd.jocl.wrappers.image.OpenCLOutputImage;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class SandboxImpl extends OpenCLBase {

    private final BufferedImage outputHostImage;

    private OpenCLOutputImage outputImage;

    private float step = 0f;

    public SandboxImpl(BufferedImage outputImage) {
        super(KernelFile.SANDBOX, false);
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
        outputImage = createOutputImage(outputHostImage);
    }

    protected long[][] getGlobalWorkSizePerKernel() {
        return new long[][]{{outputImage.getSizeX(), outputImage.getSizeY()}};
    }

    @Override
    protected void beforeExecute(int kernelIndex) {
        step += 0.03f;

        KernelArgumentSetter argumentSetter = resetAndGetArgumentSetter(kernelIndex);
        argumentSetter.setArgMemObject(outputImage);
        argumentSetter.setArgInt(outputImage.getSizeX());
        argumentSetter.setArgInt(outputImage.getSizeY());
        argumentSetter.setArgFloat(step);
    }

    @Override
    protected void afterExecute(int kernelIndex) {
        readImage(outputImage);
    }
}
