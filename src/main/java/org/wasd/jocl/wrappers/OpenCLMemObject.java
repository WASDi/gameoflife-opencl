package org.wasd.jocl.wrappers;

import org.jocl.Pointer;
import org.jocl.cl_mem;

public class OpenCLMemObject {

    private final int sizeX;
    private final int sizeY;
    private final cl_mem primitiveMemObject;
    private final Pointer clDataPointer;

    public OpenCLMemObject(int sizeX, int sizeY, cl_mem primitiveMemObject) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.primitiveMemObject = primitiveMemObject;

        this.clDataPointer = Pointer.to(primitiveMemObject);
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public Pointer getClDataPointer() {
        return clDataPointer;
    }

    public cl_mem getPrimitiveMemObject() {
        return primitiveMemObject;
    }
}
