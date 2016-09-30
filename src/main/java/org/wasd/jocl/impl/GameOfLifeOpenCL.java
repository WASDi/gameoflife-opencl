package org.wasd.jocl.impl;

import org.jocl.Sizeof;
import org.wasd.Swapper;
import org.wasd.gameoflife.GameOfLife;
import org.wasd.gameoflife.InitialFieldSetter;
import org.wasd.jocl.core.KernelArgumentSetter;
import org.wasd.jocl.core.KernelFile;
import org.wasd.jocl.core.OpenCLBase;
import org.wasd.jocl.wrappers.OpenCLMemObject;
import org.wasd.jocl.wrappers.image.OpenCLOutputImage;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class GameOfLifeOpenCL extends OpenCLBase implements GameOfLife {

    private static final int STEP_KERNEL = 0;
    private static final int RENDER_KERNEL = 1;
    private static final int LOCAL_SIZE = 16;

    private final int fieldSizeX;
    private final int fieldSizeY;
    private final Optional<InitialFieldSetter> initialFieldSetter;
    private final int pixelSize;

    private Swapper<OpenCLMemObject> fieldSwapper;
    private OpenCLOutputImage outputImage;
    private int stepNum = 0;

    public GameOfLifeOpenCL(int fieldSizeX, int fieldSizeY, int pixelSize,
                            Optional<InitialFieldSetter> initialFieldSetter) {
        super(KernelFile.GAME_OF_LIFE, false);
        this.fieldSizeX = fieldSizeX;
        this.fieldSizeY = fieldSizeY;
        this.initialFieldSetter = initialFieldSetter;
        this.pixelSize = pixelSize;

        init();
    }

    @Override
    public void step() {
        int stepsPerStep = 1;
        for (int i = 0; i < stepsPerStep; i++) {
            execute(STEP_KERNEL);
        }
    }

    @Override
    public int getStepNum() {
        return stepNum;
    }

    @Override
    public void renderImage(BufferedImage image) {
        if (outputImage == null) {
            outputImage = createOutputImage(image);
        }

        long startTime = System.nanoTime();

        execute(RENDER_KERNEL);

        long endTime = System.nanoTime() - startTime;
        //System.out.printf("Render kernel time: %.2fms\n", endTime / 1e6f);
    }

    @Override
    protected void afterInitCL() {
        OpenCLMemObject input = createMemObject(fieldSizeX, fieldSizeY, Sizeof.cl_int);
        OpenCLMemObject output = createMemObject(fieldSizeX, fieldSizeY, Sizeof.cl_int);
        fieldSwapper = new Swapper<>(input, output);

        int[] buffer = new int[fieldSizeX * fieldSizeY];
        if (initialFieldSetter.isPresent()) {
            boolean[][] field = new boolean[input.getSizeX()][input.getSizeY()];
            initialFieldSetter.get().setFor(field);
            for (int y = 0; y < field.length; y++) {
                for (int x = 0; x < field[y].length; x++) {
                    buffer[y * input.getSizeX() + x] = field[x][y] ? 1 : 0;
                }
            }
        }
        writeIntBuffer(input, buffer);
    }

    @Override
    protected long[][] getGlobalWorkSizePerKernel() {
        long[] kernel1 = new long[]{fieldSizeX, fieldSizeY};
        long[] kernel2 = new long[]{fieldSizeX, fieldSizeY};
        return new long[][]{kernel1, kernel2};
    }

    private int globalSizeRequiredForV2(int fieldSize) {
        int cellsPerLocalGroup = LOCAL_SIZE - 2;
        int localGroupsRequired = (int) Math.ceil((double) fieldSize / cellsPerLocalGroup);
        int globalSize = localGroupsRequired * cellsPerLocalGroup;
        return globalSize;
    }

    @Override
    protected long[][] getLocalWorkSizePerKernel() {
        long[] kernel1 = new long[]{LOCAL_SIZE, LOCAL_SIZE};
        return new long[][]{kernel1, null};
    }

    @Override
    protected void beforeExecute(int kernelIndex) {
        KernelArgumentSetter argumentSetter = resetAndGetArgumentSetter(kernelIndex);
        if (kernelIndex == STEP_KERNEL) {
            setArgumentsForStepKernel(argumentSetter);
        } else if (kernelIndex == RENDER_KERNEL) {
            setArgumentsForRenderKernel(argumentSetter);
        }
    }

    private void setArgumentsForStepKernel(KernelArgumentSetter argumentSetter) {
        argumentSetter.setArgMemObject(fieldSwapper.getOne());
        argumentSetter.setArgMemObject(fieldSwapper.getTwo());
        argumentSetter.setArgInt(fieldSizeX);
        argumentSetter.setArgInt(fieldSizeY);
    }

    private void setArgumentsForRenderKernel(KernelArgumentSetter argumentSetter) {
        argumentSetter.setArgMemObject(fieldSwapper.getOne());
        argumentSetter.setArgMemObject(outputImage);
        argumentSetter.setArgInt(fieldSizeX);
        argumentSetter.setArgInt(pixelSize);
    }

    @Override
    protected void afterExecute(int kernelIndex) {
        if (kernelIndex == STEP_KERNEL) {
            fieldSwapper.swap();
            stepNum++;
        } else if (kernelIndex == RENDER_KERNEL) {
            readImage(outputImage);
        }
    }
}
