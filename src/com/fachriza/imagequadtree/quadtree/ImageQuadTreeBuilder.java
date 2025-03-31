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

    public ImageQuadTreeBuilder(ErrorMeasurementMethod emm, ImageData imageData, float threshold, int minimumBlockSize,
            float compressionLevel) {
        this.emm = emm;
        this.imageData = imageData;
        this.threshold = threshold;
        this.minimumBlockSize = minimumBlockSize;
        this.compressionLevel = compressionLevel;
    }

    public ImageQuadTree build(int x, int y, int width, int height) {
        float[] mean = ImageUtil.getAverageColor(imageData, x, y, width, height);
        ImageQuadTree node = new ImageQuadTree((byte) mean[0], (byte) mean[1], (byte) mean[2]);
        if (width * height == 1)
            return node;

        float error = emm.getErrorValue(mean, x, y, width, height);

        // System.out.println(error);

        int halfLowerWidth = width / 2;
        int halfLowerHeight = height / 2;
        int halfUpperWidth = width - halfLowerWidth;
        int halfUpperHeight = height - halfLowerHeight;
        int halfLowerSize = halfLowerHeight * halfLowerWidth;
        int halfUpperSize = halfUpperHeight * halfUpperWidth;

        if (error > threshold && halfLowerSize >= minimumBlockSize && halfUpperSize >= minimumBlockSize) {
            ImageQuadTree[] children = { build(x, y, halfLowerWidth, halfLowerHeight),
                    build(x + halfLowerWidth, y, halfUpperWidth, halfUpperHeight),
                    build(x, y + halfLowerHeight, halfUpperWidth, halfUpperHeight),
                    build(x + halfLowerWidth, y + halfLowerHeight, halfUpperWidth, halfUpperHeight) };

            node.setChildrenArray(children);
        }
        return node;

    }

    public ImageData getImageData() {
        return imageData;
    }
}