package com.fachriza.imagequadtree.quadtree;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.image.errormeasuremethod.ErrorMeasurementMethod;
import com.fachriza.imagequadtree.utils.ImageUtil;

public class ImageQuadTreeBuilder {
    private ErrorMeasurementMethod emm;
    private ImageData imageData;
    private float threshold;
    private int minimumBlockSize;
    private float compressionLevel;

    public int[] lowestDimension = { Integer.MAX_VALUE, Integer.MAX_VALUE };

    public ImageQuadTreeBuilder(ErrorMeasurementMethod emm, ImageData imageData, float threshold, int minimumBlockSize,
            float compressionLevel) {
        this.emm = emm;
        this.imageData = imageData;
        this.threshold = threshold;
        this.minimumBlockSize = minimumBlockSize;
        this.compressionLevel = compressionLevel;
    }

    public ImageQuadTree build(int x, int y, int width, int height) {

        if (width < lowestDimension[0])
            lowestDimension[0] = width;
        if (height < lowestDimension[1])
            lowestDimension[1] = height;

        float[] mean = ImageUtil.getAverageColor(imageData, x, y, width, height);
        float error = emm.getErrorValue(mean, x, y, width, height);

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        ImageQuadTree node = new ImageQuadTree((byte) mean[0], (byte) mean[1], (byte) mean[2]);
        if (error > threshold && halfHeight >= minimumBlockSize && halfWidth >= minimumBlockSize) {
            ImageQuadTree[] children = { build(x, y, halfWidth, halfHeight),
                    build(x + halfWidth, y, halfWidth, halfHeight),
                    build(x, y + halfHeight, halfWidth, halfHeight),
                    build(x + halfWidth, y + halfHeight, halfWidth, halfHeight) };

            node.setChildrenArray(children);
        }
        return node;

    }
}