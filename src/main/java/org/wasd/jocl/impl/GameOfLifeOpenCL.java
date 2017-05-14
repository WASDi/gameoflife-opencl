package org.wasd.jocl.impl;

import org.jocl.Sizeof;
import org.wasd.Swapper;
import org.wasd.gameoflife.GameOfLife;
import org.wasd.gameoflife.initialfieldsetter.InitialFieldSetter;
import org.wasd.jocl.core.KernelArgumentSetter;
import org.wasd.jocl.core.KernelFile;
import org.wasd.jocl.core.OpenCLBase;
import org.wasd.jocl.wrappers.OpenCLMemObject;
import org.wasd.jocl.wrappers.image.OpenCLOutputImage;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class GameOfLifeOpenCL extends OpenCLBase implements GameOfLife {

    private static final boolean CHAR4 = false;

    private static final int STEP_KERNEL = 0;
    private static final int RENDER_KERNEL = 1;
    private static final int LOCAL_SIZE = 16;

    private static final int S0 = 0;
    private static final int S1 = 8;
    private static final int S2 = 16;
    private static final int S3 = 24;

    private final int fieldSizeX;
    private final int fieldSizeY;
    private final InitialFieldSetter initialFieldSetter;
    private final int pixelSize;

    private Swapper<OpenCLMemObject> fieldSwapper;
    private OpenCLOutputImage outputImage;
    private int stepNum = 0;

    public GameOfLifeOpenCL(int fieldSizeX, int fieldSizeY, int pixelSize,
                            InitialFieldSetter initialFieldSetter) {
        super(CHAR4 ? KernelFile.GAME_OF_LIFE_CHAR4 : KernelFile.GAME_OF_LIFE_V2, false);
        this.fieldSizeX = fieldSizeX;
        this.fieldSizeY = fieldSizeY;
        this.initialFieldSetter = initialFieldSetter;
        this.pixelSize = pixelSize;
        init();
    }

    @Override
    public void step() {
        int stepsPerStep = 1000;
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
    protected Map<String, String> getBuildArguments() {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("SIZE_X", String.valueOf(fieldSizeX));
        arguments.put("SIZE_Y", String.valueOf(fieldSizeY));
        return arguments;
    }

    @Override
    protected void afterInitCL() {
        if (CHAR4) {
            initChar4();
        } else {
            initInt();
        }
    }

    private void initInt() {
        OpenCLMemObject input = createMemObject(fieldSizeX, fieldSizeY, Sizeof.cl_int);
        OpenCLMemObject output = createMemObject(fieldSizeX, fieldSizeY, Sizeof.cl_int);
        fieldSwapper = new Swapper<>(input, output);

        int[] buffer = new int[fieldSizeX * fieldSizeY];
        boolean[][] field = new boolean[input.getSizeX()][input.getSizeY()];
        initialFieldSetter.setFor(field);
        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field[y].length; x++) {
                buffer[y * input.getSizeX() + x] = field[x][y] ? 1 : 0;
            }
        }
        writeIntBuffer(input, buffer);
    }

    private void initChar4() {
        if (fieldSizeY % 4 != 0) {
            System.out.println("char4 kernel not supported where fieldSizeY mod 4 != 0");
            System.exit(-1);
        }

        int memorySizeY = fieldSizeY / 4;

        OpenCLMemObject input = createMemObject(fieldSizeX, memorySizeY, Sizeof.cl_char4);
        OpenCLMemObject output = createMemObject(fieldSizeX, memorySizeY, Sizeof.cl_char4);
        fieldSwapper = new Swapper<>(input, output);

        int[] buffer = new int[fieldSizeX * memorySizeY];
        boolean[][] field = new boolean[fieldSizeX][fieldSizeY];
        initialFieldSetter.setFor(field);
        for (int y = 0; y < memorySizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                buffer[y * fieldSizeX + x]
                        = (field[x][y * 4] ? 1 : 0) //  << S0
                        | (field[x][y * 4 + 1] ? 1 : 0) << S1
                        | (field[x][y * 4 + 2] ? 1 : 0) << S2
                        | (field[x][y * 4 + 3] ? 1 : 0) << S3;
            }
        }
        writeIntBuffer(input, buffer);
    }

    @Override
    protected long[][] getGlobalWorkSizePerKernel() {
        int memorySizeY = CHAR4 ? fieldSizeY / 4 : fieldSizeY;
        long[] kernel1 = new long[]{fieldSizeX, memorySizeY};
        kernel1[0] = globalSizeRequired14Based(kernel1[0]);
        kernel1[1] = globalSizeRequired14Based(kernel1[1]);
        long[] kernel2 = new long[]{fieldSizeX, memorySizeY};
        return new long[][]{kernel1, kernel2};
    }

    static long globalSizeRequired14Based(long fieldSize) {
        int cellsPerLocalGroup = LOCAL_SIZE - 2;
        int localGroupsRequired = (int) Math.ceil((double) fieldSize / cellsPerLocalGroup);
        int globalSize = localGroupsRequired * LOCAL_SIZE;
        System.out.println("Work size... " + globalSize);
        return globalSize;
    }

    static long globalSizeRequired16Based(long fieldSize) {
        int localGroupsRequired = (int) Math.ceil((double) fieldSize / LOCAL_SIZE);
        int globalSize = localGroupsRequired * LOCAL_SIZE;
        System.out.println("Work size... " + globalSize);
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
    }

    private void setArgumentsForRenderKernel(KernelArgumentSetter argumentSetter) {
        argumentSetter.setArgMemObject(fieldSwapper.getOne());
        argumentSetter.setArgMemObject(outputImage);
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
