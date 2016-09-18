package org.wasd.jocl.shit;

import org.wasd.jocl.core.KernelFile;

public class HelloMeep {

    public static void main(String[] args) {
        float inputA[] = range(10);
        float inputB[] = range(10);

        OpenCLFacade cl = new OpenCLFacade(inputA, inputB, KernelFile.ADD, true);

        cl.init();
        cl.execute();
        cl.clean();

        System.out.println("Result: " + cl.getResultAsString());
    }

    private static float[] range(int n) {
        float[] retVal = new float[n];
        for (int i = 0; i < n; i++) {
            retVal[i] = i;
        }
        return retVal;
    }

}
