package com.fachriza.imagequadtree.utils;

import java.util.ArrayDeque;
import java.util.Deque;

public class TimeProfiler {

    private class Section {

        private String title;
        private float time;
        private long tempTime;

        public Section(String title) {
            this.title = title;
            tempTime = System.nanoTime();
        }

        public void stop() {
            time = (System.nanoTime() - tempTime) * 1e-6f;
        }
    }

    private Deque<Section> sections;

    public TimeProfiler() {
        sections = new ArrayDeque<Section>();
    }

    public void startSection(String sectionTitle) {
        sections.offerLast(new Section(sectionTitle));
    }

    public void stopSection() {
        sections.peekLast().stop();
    }

    public void print() {
        // Find lengthiest title
        int maxLength = 0;
        for (Section section : sections) {
            int length = section.title.length();
            maxLength = length > maxLength ? length : maxLength;
        }

        // Add some padding
        maxLength += 2;

        // Print column title
        System.out.format("%" + (maxLength / 2 + 3) + "s", "Proses");
        System.out.format("%" + (8 + maxLength / 2) + "s%n", "Waktu");

        float totalTime = 0.0f;
        for (Section section : sections) {
            System.out.format("%-" + maxLength + "s : ", section.title);
            System.out.format("%10.2fms%n", section.time);
            totalTime += section.time;
        }

        // Print total exectution time
        System.out.format("%-" + maxLength + "s : ", "Total Waktu");
        System.out.format("%10.2fms%n", totalTime);
    }
}
