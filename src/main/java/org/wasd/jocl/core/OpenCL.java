package org.wasd.jocl.core;

public interface OpenCL {

    void init();

    void execute();

    void execute(int kernelIndex);

}
