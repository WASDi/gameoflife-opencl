package org.wasd.jocl.shit;


import org.jocl.*;
import org.jocl.samples.ExecutionStatistics;
import org.wasd.jocl.core.KernelFile;

import static org.jocl.CL.*;

/**
 * Right now, for (float, float) -> (float)
 */
public class OpenCLFacade {

    private static final int DIMENSIONS = 1;

    public cl_context context;
    public cl_command_queue commandQueue;

    private final int n;
    private final KernelFile kernelFile;
    private final float[] inputA;
    private final float[] inputB;
    private final float[] resultArray;

    private cl_mem memObjects[];
    private cl_kernel kernel;
    private cl_program program;

    private final boolean profiling;
    private ExecutionStatistics executionStatistics = new ExecutionStatistics();

    public OpenCLFacade(float[] inputA,
                        float[] inputB,
                        KernelFile kernelFile,
                        boolean profiling) {
        if (inputA.length != inputB.length) {
            throw new IllegalArgumentException("In-data of different length");
        }
        this.inputA = inputA;
        this.inputB = inputB;
        this.kernelFile = kernelFile;
        this.profiling = profiling;

        this.n = inputA.length;
        this.resultArray = new float[n];
    }

    public void init() {
        CL.setExceptionsEnabled(true);

        cl_platform_id platform = getPlatform();
        cl_context_properties contextProperties = getContextProperties(platform);
        int numDevices = getNumDevices(platform);
        cl_device_id device = getDeviceId(platform, numDevices);

        context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);
        commandQueue = clCreateCommandQueue(context,
                device,
                profiling ? CL.CL_QUEUE_PROFILING_ENABLE : 0,
                null);

        allocateMemoryObjects();
        createAndBuildProgram();
        createKernel();
    }


    public void execute() {
        long global_work_size[] = new long[]{n};
        long local_work_size[] = new long[]{1};

        cl_event kernelEvent = null;
        cl_event readEvent = null;
        if (profiling) {
            kernelEvent = new cl_event();
            readEvent = new cl_event();
        }

        clEnqueueNDRangeKernel(commandQueue, kernel, DIMENSIONS, null,
                global_work_size, local_work_size, 0, null, kernelEvent);

        if (profiling) {
            CL.clWaitForEvents(1, new cl_event[]{kernelEvent});
        }

        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
                n * Sizeof.cl_float, Pointer.to(resultArray), 0, null, readEvent);

        if (profiling) {
            CL.clWaitForEvents(1, new cl_event[]{readEvent});
        }


        if (profiling) {
            executionStatistics.addEntry("kernel", kernelEvent);
            executionStatistics.addEntry("read", readEvent);
            executionStatistics.print();
        }

    }

    public void clean() {
        for (cl_mem memObject : memObjects) {
            clReleaseMemObject(memObject);
        }
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }

    public float[] getResult() {
        return resultArray;
    }

    public String getResultAsString() {
        return java.util.Arrays.toString(resultArray);
    }


    private cl_platform_id getPlatform() {
        cl_platform_id platforms[] = new cl_platform_id[getNumPlatforms()];
        clGetPlatformIDs(platforms.length, platforms, null);
        return platforms[0];
    }

    private int getNumPlatforms() {
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        return numPlatformsArray[0];
    }

    private cl_context_properties getContextProperties(cl_platform_id platform) {
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        return contextProperties;
    }

    private int getNumDevices(cl_platform_id platform) {
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, null, numDevicesArray);
        return numDevicesArray[0];
    }

    private cl_device_id getDeviceId(cl_platform_id platform, int numDevices) {
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, numDevices, devices, null);
        return devices[0];
    }

    private void allocateMemoryObjects() {
        memObjects = new cl_mem[3];
        memObjects[0] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, Pointer.to(inputA), null);
        memObjects[1] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, Pointer.to(inputB), null);
        memObjects[2] = clCreateBuffer(context,
                CL_MEM_READ_WRITE,
                Sizeof.cl_float * n, null, null);
    }

    private void createAndBuildProgram() {
        program = clCreateProgramWithSource(context, 1, new String[]{kernelFile.load()}, null, null);

        clBuildProgram(program, 0, null, null, null, null);
    }

    private void createKernel() {
        kernel = clCreateKernel(program, kernelFile.getFunctionName(), null);

        clSetKernelArg(kernel, 0,
                Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1,
                Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2,
                Sizeof.cl_mem, Pointer.to(memObjects[2]));
    }

}
