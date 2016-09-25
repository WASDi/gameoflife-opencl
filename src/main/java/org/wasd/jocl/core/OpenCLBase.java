/*
 * JOCL - Java bindings for OpenCL
 *
 * Copyright 2010 Marco Hutter - http://www.jocl.org/
 */

package org.wasd.jocl.core;

import org.jocl.*;
import org.wasd.Util;
import org.wasd.jocl.wrappers.image.OpenCLImageFactory;
import org.wasd.jocl.wrappers.image.OpenCLInputImage;
import org.wasd.jocl.wrappers.OpenCLMemObject;
import org.wasd.jocl.wrappers.image.OpenCLOutputImage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Optional;

import static org.jocl.CL.*;

public abstract class OpenCLBase implements OpenCL {

    private final KernelFile kernelFile;
    private final boolean useProfiling;


    private cl_context clContext;
    private cl_command_queue clCommandQueue;
    private cl_kernel[] clKernels;
    private long[][] globalWorkSizePerKernel;
    private long[][] localWorkSizePerKernel;
    private KernelArgumentSetter[] argumentSetters;


    public OpenCLBase(KernelFile kernelFile, boolean useProfiling) {
        this.kernelFile = kernelFile;
        this.useProfiling = useProfiling;
    }

    public void init() {
        initCL();
        afterInitCL();
        globalWorkSizePerKernel = getGlobalWorkSizePerKernel();
        localWorkSizePerKernel = getLocalWorkSizePerKernel();

        argumentSetters = new KernelArgumentSetter[clKernels.length];
        for (int i = 0; i < clKernels.length; i++) {
            argumentSetters[i] = new KernelArgumentSetter(clKernels[i]);
        }
    }

    protected abstract void afterInitCL();

    protected abstract long[][] getGlobalWorkSizePerKernel();

    protected long[][] getLocalWorkSizePerKernel() {
        return new long[][]{null};
    }

    protected abstract void beforeExecute(int kernelIndex);

    protected abstract void afterExecute(int kernelIndex);

    @Override
    public void execute() {
        execute(0);
    }

    @Override
    public void execute(int kernelIndex) {
        beforeExecute(kernelIndex);

        Optional<cl_event> kernelEvent = getEventIfUseProfiling();

        clEnqueueNDRangeKernel(clCommandQueue, clKernels[kernelIndex], globalWorkSizePerKernel[kernelIndex].length, null,
                globalWorkSizePerKernel[kernelIndex], localWorkSizePerKernel[kernelIndex], 0, null, kernelEvent.orElse(null));

        printTimingIfPresent("kernel_" + kernelIndex, kernelEvent);

        afterExecute(kernelIndex);
    }

    protected KernelArgumentSetter resetAndGetArgumentSetter(int index) {
        argumentSetters[index].reset();
        return argumentSetters[index];
    }

    //BEGIN creators. Om det blir många av dessa, gör klass med clContext likt KernelArgumentSetter
    protected OpenCLInputImage createInputImage(BufferedImage inputImage) {
        return OpenCLImageFactory.createInputImage(clContext, inputImage);
    }

    protected OpenCLOutputImage createOutputImage(BufferedImage inputImage) {
        return OpenCLImageFactory.createOutputImage(clContext, inputImage);
    }

    protected OpenCLMemObject createMemObject(int sizeX, int sizeY, int sizeof) {
        cl_mem primitiveMemObject = clCreateBuffer(clContext, CL_MEM_READ_WRITE, sizeX * sizeY * sizeof, null, null);
        return new OpenCLMemObject(sizeX, sizeY, primitiveMemObject);
    }
    //END creators

    protected void readImage(OpenCLOutputImage outputImage) {
        Optional<cl_event> readEvent = getEventIfUseProfiling();

        clEnqueueReadImage(
                clCommandQueue, outputImage.getPrimitiveMemObject(), CL_BLOCKING, new long[3],
                new long[]{outputImage.getSizeX(), outputImage.getSizeY(), 1},
                outputImage.getSizeX() * Sizeof.cl_uint, 0,
                outputImage.getPointerToHostImage(), 0, null, readEvent.orElse(null));

        printTimingIfPresent("read", readEvent);
    }

    protected void writeIntBuffer(OpenCLMemObject memObject, int[] buffer) {
        clEnqueueWriteBuffer(clCommandQueue, memObject.getPrimitiveMemObject(),
                CL_BLOCKING, 0, buffer.length * Sizeof.cl_int,
                Pointer.to(buffer), 0, null, null);
    }

    protected void writeImage(OpenCLMemObject memObject, BufferedImage newImageData) {
        if (memObject.getSizeX() != newImageData.getWidth() || memObject.getSizeY() != newImageData.getHeight()) {
            throw new IllegalArgumentException("must update image with same resolution");
        }
        DataBufferInt dataBufferSrc =
                (DataBufferInt) newImageData.getRaster().getDataBuffer();
        int dataSrc[] = dataBufferSrc.getData();

        long[] origin = {0, 0, 0};
        long[] region = {memObject.getSizeX(), memObject.getSizeY(), 1};
        int rowPitch = memObject.getSizeX() * Sizeof.cl_uint;
        CL.clEnqueueWriteImage(
                clCommandQueue, memObject.getPrimitiveMemObject(),
                CL_NON_BLOCKING, origin, region,
                rowPitch, 0, Pointer.to(dataSrc), 0, null, null);
    }

    protected int[] readIntBuffer(OpenCLMemObject memObject) {
        Optional<cl_event> readEvent = getEventIfUseProfiling();

        int[] buffer = new int[memObject.getSizeX() * memObject.getSizeY()];
        clEnqueueReadBuffer(clCommandQueue, memObject.getPrimitiveMemObject(),
                CL_BLOCKING, 0, buffer.length * Sizeof.cl_int,
                Pointer.to(buffer), 0, null, readEvent.orElse(null));

        printTimingIfPresent("read", readEvent);

        return buffer;
    }

    private Optional<cl_event> getEventIfUseProfiling() {
        if (useProfiling) {
            return Optional.of(new cl_event());
        }
        return Optional.empty();
    }

    private void printTimingIfPresent(String name, Optional<cl_event> event) {
        if (event.isPresent()) {
            CL.clWaitForEvents(1, new cl_event[]{event.get()});
            long duration = Util.timeForClEvent(event.get());
            //TODO more benchmarks
            System.out.println("Event " + name + ": \tTime   : " +
                    String.format("%8.3f", duration / 1e6) + " ms");
        }
    }

    private void initCL() {
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        CL.setExceptionsEnabled(true);

        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        clContext = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        int imageSupport[] = new int[1];
        clGetDeviceInfo(device, CL.CL_DEVICE_IMAGE_SUPPORT,
                Sizeof.cl_int, Pointer.to(imageSupport), null);
        System.out.println("Images supported: " + (imageSupport[0] == 1));
        if (imageSupport[0] == 0) {
            System.out.println("Images are not supported");
            System.exit(1);
            return;
        }

        System.out.println("Creating command queue...");
        long properties = 0;
        //properties |= CL_QUEUE_PROFILING_ENABLE;
        //properties |= CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE;
        if (useProfiling) {
            properties |= CL_QUEUE_PROFILING_ENABLE;
        }
        clCommandQueue = clCreateCommandQueue(clContext, device, properties, null);

        System.out.println("Creating program...");
        cl_program program = clCreateProgramWithSource(clContext, 1, new String[]{kernelFile.load()}, null, null);

        System.out.println("Building program...");
        clBuildProgram(program, 0, null, null, null, null);
        //TODO kolla upp "mad-enable" osv https://www.khronos.org/registry/cl/sdk/1.0/docs/man/xhtml/clBuildProgram.html

        System.out.println("Creating kernel(s)...");

        clKernels = new cl_kernel[kernelFile.functionNames.length];
        for (int i = 0; i < clKernels.length; i++) {
            clKernels[i] = clCreateKernel(program, kernelFile.functionNames[i], null);
        }
    }
}
