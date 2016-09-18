/*
 * JOCL - Java bindings for OpenCL
 *
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */

package org.jocl.samples;

import java.util.*;

import org.jocl.*;


/**
 * A simple helper class for tracking cl_events and printing
 * timing information for the execution of the commands that
 * are associated with the events.
 */
public class ExecutionStatistics {
    /**
     * A single entry of the ExecutionStatistics
     */
    private static class Entry {
        private String name;
        private final long startTime[] = new long[1];
        private final long endTime[] = new long[1];

        Entry(String name, cl_event event) {
            this.name = name;
            CL.clGetEventProfilingInfo(
                    event, CL.CL_PROFILING_COMMAND_START,
                    Sizeof.cl_ulong, Pointer.to(startTime), null);
            CL.clGetEventProfilingInfo(
                    event, CL.CL_PROFILING_COMMAND_END,
                    Sizeof.cl_ulong, Pointer.to(endTime), null);
        }


        void print() {
            long duration = endTime[0] - startTime[0];
            System.out.println("Event " + name + ": \tTime   : " +
                    String.format("%8.3f", duration / 1e6) + " ms");
        }
    }

    /**
     * The list of entries in this instance
     */
    private List<Entry> entries = new ArrayList<Entry>();

    /**
     * Adds the specified entry to this instance
     *
     * @param name  A name for the event
     * @param event The event
     */
    public void addEntry(String name, cl_event event) {
        entries.add(new Entry(name, event));
    }

    /**
     * Print the statistics
     */
    public void print() {
        for (Entry entry : entries) {
            entry.print();
        }
    }

    public void clear() {
        entries.clear();
    }


}