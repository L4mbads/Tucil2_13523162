package com.fachriza.imagequadtree.quadtree;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.image.errormeasuremethod.ErrorMeasurementMethod;
import com.fachriza.imagequadtree.utils.ImageUtil;

public class ImageQuadTreeBuilder {

    private ErrorMeasurementMethod emm;
    private ImageData imageData;
    private float threshold;
    private int minimumBlockSize;
    // Use atomic to prevent race condition
    private final AtomicInteger nodeCount;

    public ImageQuadTreeBuilder(
            ErrorMeasurementMethod emm,
            ImageData imageData,
            float threshold,
            int minimumBlockSize) {

        this.emm = emm;
        this.imageData = imageData;
        this.threshold = threshold;
        this.minimumBlockSize = minimumBlockSize;
        this.nodeCount = new AtomicInteger(0);
    }

    public ImageData getImageData() {
        return imageData;
    }

    public int getNodeCount() {
        return nodeCount.get();
    }

    public void resetNodeCount() {
        this.nodeCount.set(0);
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public ImageQuadTree build(int x, int y, int width, int height) {
        nodeCount.incrementAndGet();

        /*
         * Always calculate the mean, even if its not a leaf node.
         * We need the mean for error calculation and GIF anyway.
         */
        float[] mean = ImageUtil.getAverageColor(imageData, x, y, width, height);
        ImageQuadTree node = new ImageQuadTree(mean, x, y, width, height);

        int size = width * height;
        if (size <= minimumBlockSize)
            return node;

        float error = emm.getErrorValue(mean, x, y, width, height);

        /*
         * Calculate children regions.
         *
         * "Why don't you make a Region class for better encapsulation?", you ask?
         * I don't want Java class overhead, i want performance
         *
         * "Then why do you use Java?",
         * Good question.
         */
        int halfLowerWidth = width / 2;
        int halfLowerHeight = height / 2;
        int halfUpperWidth = width - halfLowerWidth;
        int halfUpperHeight = height - halfLowerHeight;
        int halfUpperSize = halfUpperHeight * halfUpperWidth;

        /*
         * Check if future children size are big enough to split.
         * No need to check halfLowerSize.
         */
        if (error > threshold && halfUpperSize >= minimumBlockSize) {
            ImageQuadTree n1, n2, n3, n4;
            if (halfUpperSize > 100000) {

                // do 2 build tasks in other thread if blocks are big enough
                BuildQuadTreeAsync task3 = new BuildQuadTreeAsync(
                        x,
                        y + halfLowerHeight,
                        halfLowerWidth,
                        halfUpperHeight);

                BuildQuadTreeAsync task4 = new BuildQuadTreeAsync(
                        x + halfLowerWidth,
                        y + halfLowerHeight,
                        halfUpperWidth,
                        halfUpperHeight);

                // build asynchrounously
                task3.fork();
                task4.fork();

                // do 2 build task in current working thread
                n1 = build(x, y, halfLowerWidth, halfLowerHeight);

                n2 = build(x + halfLowerWidth, y, halfUpperWidth, halfLowerHeight);

                n3 = task3.join();
                n4 = task4.join();
            } else {
                // else do all build task in current working thread
                n1 = build(x, y, halfLowerWidth, halfLowerHeight);

                n2 = build(x + halfLowerWidth, y, halfUpperWidth, halfLowerHeight);

                n3 = build(x, y + halfLowerHeight,
                        halfLowerWidth, halfUpperHeight);

                n4 = build(x + halfLowerWidth, y + halfLowerHeight,
                        halfUpperWidth, halfUpperHeight);
            }
            ImageQuadTree[] children = { n1, n2, n3, n4 };
            node.setChildrenArray(children);
        }
        return node;
    }

    private class BuildQuadTreeAsync extends RecursiveTask<ImageQuadTree> {

        private int x;
        private int y;
        private int width;
        private int height;

        public BuildQuadTreeAsync(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        protected ImageQuadTree compute() {
            return build(x, y, width, height);
        }
    }

}