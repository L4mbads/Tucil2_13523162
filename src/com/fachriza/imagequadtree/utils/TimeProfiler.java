package com.fachriza.imagequadtree.utils;

import java.util.ArrayDeque;
import java.util.Deque;

public class TimeProfiler {
    private float[] times;
    private long tempTime;
    private int currSection;
    private String[] titles;

    public TimeProfiler(String... string) {
        titles = string;
        times = new float[titles.length];
        currSection = 0;
    }

    public void startNext() {
        tempTime = System.nanoTime();
    }

    public void stop() {
        times[currSection] = (System.nanoTime() - tempTime) * 1e-6f;
        currSection++;
    }

    public void print() {
        Deque<Integer> length = new ArrayDeque<Integer>();
        for (final String title : titles) {
            int l = title.length() + 2;
            length.offer(l);
            System.out.format("%-" + String.valueOf(l) + "s", title);
        }
        System.out.format("%-15s", "Total");
        System.out.println();
        float totalTime = 0;
        for (final float time : times) {
            totalTime += time;
            System.out.format("%-" + String.valueOf(length.removeFirst()) + ".2f", time);
        }
        System.out.format("%.2fms%n", totalTime);
    }
}
