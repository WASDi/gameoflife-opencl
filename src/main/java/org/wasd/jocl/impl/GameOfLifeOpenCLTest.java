package org.wasd.jocl.impl;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GameOfLifeOpenCLTest {

    @Test
    public void testGlobalSizeRequired14Based() throws Exception {
        assertEquals(GameOfLifeOpenCL.globalSizeRequired14Based(14), 16);
        assertEquals(GameOfLifeOpenCL.globalSizeRequired14Based(15), 32);

        assertEquals(GameOfLifeOpenCL.globalSizeRequired14Based(28), 32);
        assertEquals(GameOfLifeOpenCL.globalSizeRequired14Based(29), 48);
    }

    @Test
    public void testGlobalSizeRequired16Based() throws Exception {
        assertEquals(GameOfLifeOpenCL.globalSizeRequired16Based(16), 16);
        assertEquals(GameOfLifeOpenCL.globalSizeRequired16Based(17), 32);

        assertEquals(GameOfLifeOpenCL.globalSizeRequired16Based(32), 32);
        assertEquals(GameOfLifeOpenCL.globalSizeRequired16Based(33), 48);
    }
}