package com.fachriza.imagequadtree.quadtree;

import java.util.concurrent.RecursiveTask;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.image.errormeasuremethod.ErrorMeasurementMethod;
import com.fachriza.imagequadtree.utils.ImageUtil;

public class ImageQuadTreeBuilder {
    private ErrorMeasurementMethod emm;
    private ImageData imageData;
    private float threshold;
    private int minimumBlockSize;

    private int nodeCount;

    public ImageQuadTreeBuilder(
            ErrorMeasurementMethod emm,
            ImageData imageData,
            float threshold,
            int minimumBlockSize) {

        this.emm = emm;
        this.imageData = imageData;
        this.threshold = threshold;
        this.minimumBlockSize = minimumBlockSize;
        this.nodeCount = 0;
    }

    public ImageData getImageData() {
        return imageData;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void resetNodeCount() {
        this.nodeCount = 0;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public ImageQuadTree build(int x, int y, int width, int height) {
        nodeCount++;

        float[] mean = ImageUtil.getAverageColor(imageData, x, y, width, height);
        ImageQuadTree node = new ImageQuadTree((byte) mean[0], (byte) mean[1], (byte) mean[2], -1.0f);

        int size = width * height;
        if (size == 1 || size <= minimumBlockSize)
            return node;

        float error = emm.getErrorValue(mean, x, y, width, height);
        node.setError(error);

        int halfLowerWidth = width / 2;
        int halfLowerHeight = height / 2;
        int halfUpperWidth = width - halfLowerWidth;
        int halfUpperHeight = height - halfLowerHeight;
        int halfUpperSize = halfUpperHeight * halfUpperWidth;

        if (error > threshold && halfUpperSize >= minimumBlockSize) {

            ImageQuadTree n1, n2, n3, n4;
            if (halfUpperSize > 100000) {

                // do 2 tasks in other thread if blocks are big enough
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

                n1 = build(
                        x,
                        y,
                        halfLowerWidth,
                        halfLowerHeight);

                n2 = build(
                        x + halfLowerWidth,
                        y,
                        halfUpperWidth,
                        halfLowerHeight);

                n3 = task3.join();
                n4 = task4.join();
            } else {
                // else build all in current thread
                n1 = build(
                        x,
                        y,
                        halfLowerWidth,
                        halfLowerHeight);

                n2 = build(
                        x + halfLowerWidth,
                        y,
                        halfUpperWidth,
                        halfLowerHeight);

                n3 = build(
                        x,
                        y + halfLowerHeight,
                        halfLowerWidth,
                        halfUpperHeight);

                n4 = build(
                        x + halfLowerWidth,
                        y + halfLowerHeight,
                        halfUpperWidth,
                        halfUpperHeight);

            }

            ImageQuadTree[] children = {
                    n1,
                    n2,
                    n3,
                    n4
            };

            node.setChildrenArray(children);
        }
        return node;

    }

    public void adjust(ImageQuadTree node, int x, int y, int width, int height) {
        int halfLowerWidth = width / 2;
        int halfLowerHeight = height / 2;
        int halfUpperWidth = width - halfLowerWidth;
        int halfUpperHeight = height - halfLowerHeight;

        ImageQuadTree[] children = node.getChildrenArray();
        if (node.getError() < threshold) {
            if (children != null) {
                nodeCount -= 4;
                // node.setChildrenArray(null);
            }
            // node.isChildrenValid = false;
        } else {
            if (children == null) {
                ImageQuadTree[] new_children = {
                        build(x, y, halfLowerWidth, halfLowerHeight),
                        build(x + halfLowerWidth, y, halfUpperWidth, halfLowerHeight),
                        build(x, y + halfLowerHeight, halfLowerWidth, halfUpperHeight),
                        build(x + halfLowerWidth, y + halfLowerHeight, halfUpperWidth, halfUpperHeight)
                };

                node.setChildrenArray(new_children);

            } else {
                adjust(children[0], x, y, halfLowerWidth, halfLowerHeight);
                adjust(children[1], x + halfLowerWidth, y, halfUpperWidth, halfLowerHeight);
                adjust(children[2], x, y + halfLowerHeight, halfLowerWidth, halfUpperHeight);
                adjust(children[3], x + halfLowerWidth, y + halfLowerHeight, halfUpperWidth, halfUpperHeight);
            }
            // node.isChildrenValid = true;
        }
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