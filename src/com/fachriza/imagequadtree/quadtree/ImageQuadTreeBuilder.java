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
        if (x + width > imageData.getWidth()) {
            width = imageData.getWidth() - 1 - x;
        }
        if (y + height > imageData.getHeight()) {
            height = imageData.getHeight() - 1 - y;
        }

        float[] mean = ImageUtil.getAverageColor(imageData, x, y, width, height);
        ImageQuadTree node = new ImageQuadTree((byte) mean[0], (byte) mean[1], (byte) mean[2]);
        if (width * height == 1)
            return node;

        float error = emm.getErrorValue(mean, x, y, width, height);

        System.out.println(error);

        int halfWidth = (int) (Math.round((float) width / 2));
        int halfHeight = (int) (Math.round((float) height / 2));
        int halfSize = halfWidth * halfHeight;

        if (error > threshold && halfSize >= minimumBlockSize) {
            ImageQuadTree[] children = { build(x, y, halfWidth, halfHeight),
                    build(x + halfWidth, y, halfWidth, halfHeight),
                    build(x, y + halfHeight, halfWidth, halfHeight),
                    build(x + halfWidth, y + halfHeight, halfWidth, halfHeight) };

            node.setChildrenArray(children);
        }
        return node;

    }

    public ImageData getImageData() {
        return imageData;
    }
}