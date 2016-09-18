package org.wasd.jocl.wrappers.image;

import org.jocl.cl_mem;
import org.wasd.jocl.wrappers.OpenCLMemObject;

public class OpenCLInputImage extends OpenCLMemObject {

    public OpenCLInputImage(int sizeX, int sizeY, cl_mem clImage) {
        super(sizeX, sizeY, clImage);
    }
}
