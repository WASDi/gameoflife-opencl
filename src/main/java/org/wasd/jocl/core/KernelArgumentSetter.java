package org.wasd.jocl.core;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import org.wasd.jocl.wrappers.OpenCLMemObject;

import static org.jocl.CL.clSetKernelArg;

public class KernelArgumentSetter {

    private final cl_kernel clKernel;
    private int argIndex = 0;

    public KernelArgumentSetter(cl_kernel clKernel) {
        this.clKernel = clKernel;
    }

    public void reset() {
        argIndex = 0;
    }

    public void setArgFloat(float f) {
        clSetKernelArg(clKernel, argIndex++, Sizeof.cl_float, Pointer.to(new float[]{f}));
    }

    public void setArgInt(int i) {
        clSetKernelArg(clKernel, argIndex++, Sizeof.cl_int, Pointer.to(new int[]{i}));
    }

    public void setArgMemObject(OpenCLMemObject memObject) {
        clSetKernelArg(clKernel, argIndex++, Sizeof.cl_mem, memObject.getClDataPointer());
    }
}
