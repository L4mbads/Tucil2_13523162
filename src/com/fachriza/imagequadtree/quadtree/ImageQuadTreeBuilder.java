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

        float error = emm.getErrorValue(x, y, width, height);

        float[] mean = ImageUtil.getAverageColor(imageData, x, y, width, height);
        if (error < threshold) {
            return new ImageQuadTree((byte) mean[0], (byte) mean[1], (byte) mean[2]);
        } else {
            return new ImageQuadTree((byte) mean[0], (byte) mean[1], (byte) mean[2]);
        }

    }

    private static void fillImageBuffer(int[] buffer, ImageQuadTree iqt, int x, int y, int width,
            int height) {
        byte[] mean = iqt.getAverageColor();
        int packed = (0b11111111 << 24) | ((mean[0] & 0xff) << 16) | ((mean[1] & 0xff) << 8) | (mean[2] & 0xff);
        for (int i = x; i < x + height; i++) {
            for (int j = y; j < y + width; j++) {
                buffer[i * width + j] = packed;
            }
        }

        if (iqt.isLeafNode())
            return;

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        fillImageBuffer(buffer, iqt.getChildren(0), x, y, halfWidth, halfHeight);
        fillImageBuffer(buffer, iqt.getChildren(1), x + halfWidth, y, halfWidth, halfHeight);
        fillImageBuffer(buffer, iqt.getChildren(2), x, y + halfHeight, halfWidth, halfHeight);
        fillImageBuffer(buffer, iqt.getChildren(3), x + halfWidth, y + halfHeight, halfWidth, halfHeight);

    }

    public static int[] getCompressedImageBuffer(ImageQuadTree iqt, ImageData id) {
        int imageSize = (int) Math.pow(2, iqt.getDepth() - 1);
        int[] compressedImageBuffer = new int[imageSize * imageSize];

        fillImageBuffer(compressedImageBuffer, iqt, 0, 0, imageSize, imageSize);

        return compressedImageBuffer;

    }

}
