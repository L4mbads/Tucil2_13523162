package com.fachriza.imagequadtree.quadtree;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.image.errormeasuremethod.ErrorMeasurementMethod;
import com.fachriza.imagequadtree.utils.ImageUtil;

public class ImageQuadTreeBuilder {
    private ErrorMeasurementMethod emm;
    private ImageData imageData;
    private float threshold;
    private int minimumBlockSize;

    public ImageQuadTreeBuilder(ErrorMeasurementMethod emm, ImageData imageData, float threshold, int minimumBlockSize,
            float compressionLevel) {
        this.emm = emm;
        this.imageData = imageData;
        this.threshold = threshold;
        this.minimumBlockSize = minimumBlockSize;
    }

    public ImageQuadTree build(int x, int y, int width, int height) {
        float[] mean = ImageUtil.getAverageColor(imageData, x, y, width, height);
        ImageQuadTree node = new ImageQuadTree((byte) mean[0], (byte) mean[1], (byte) mean[2], -1.0f);
        if (width * height == 1)
            return node;

        float error = emm.getErrorValue(mean, x, y, width, height);
        node.setError(error);

        int halfLowerWidth = width / 2;
        int halfLowerHeight = height / 2;
        int halfUpperWidth = width - halfLowerWidth;
        int halfUpperHeight = height - halfLowerHeight;
        int halfLowerSize = halfLowerHeight * halfLowerWidth;
        int halfUpperSize = halfUpperHeight * halfUpperWidth;

        if (error > threshold && halfLowerSize >= minimumBlockSize && halfUpperSize >= minimumBlockSize) {
            ImageQuadTree[] children = {
                    build(x, y, halfLowerWidth, halfLowerHeight),
                    build(x + halfLowerWidth, y, halfUpperWidth, halfUpperHeight),
                    build(x, y + halfLowerHeight, halfUpperWidth, halfUpperHeight),
                    build(x + halfLowerWidth, y + halfLowerHeight, halfUpperWidth, halfUpperHeight)
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

        if (node.getError() < threshold) {
            node.setChildrenArray(null);
        } else {
            ImageQuadTree[] children = node.getChildrenArray();
            if (children == null) {
                ImageQuadTree[] new_children = {
                        build(x, y, halfLowerWidth, halfLowerHeight),
                        build(x + halfLowerWidth, y, halfUpperWidth, halfUpperHeight),
                        build(x, y + halfLowerHeight, halfUpperWidth, halfUpperHeight),
                        build(x + halfLowerWidth, y + halfLowerHeight, halfUpperWidth, halfUpperHeight)
                };

                node.setChildrenArray(new_children);

            } else {
                adjust(children[0], x, y, halfLowerWidth, halfLowerHeight);
                adjust(children[1], x + halfLowerWidth, y, halfUpperWidth, halfUpperHeight);
                adjust(children[2], x, y + halfLowerHeight, halfUpperWidth, halfUpperHeight);
                adjust(children[3], x + halfLowerWidth, y + halfLowerHeight, halfUpperWidth, halfUpperHeight);
            }
        }
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public ImageData getImageData() {
        return imageData;
    }
}