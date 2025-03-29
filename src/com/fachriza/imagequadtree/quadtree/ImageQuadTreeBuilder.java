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
        float error = emm.getErrorValue(mean, x, y, width, height);

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        // System.out.println("error: " + error);
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

    public static void printQuadTree(ImageQuadTree node, int depth) {
        if (node == null)
            return;

        // Indentation to represent tree depth
        String indent = "  ".repeat(depth);

        // Get the average color and format it as (R, G, B)
        byte[] color = node.getAverageColor();
        String colorStr = String.format("(%d, %d, %d)", color[0] & 0xFF, color[1] & 0xFF, color[2] & 0xFF);

        // Print current node
        System.out.println(indent + "Node Color: " + colorStr);

        // Recursively print children if present
        ImageQuadTree[] children = node.getChildrenArray();
        if (children != null) {
            for (int i = 0; i < 4; i++) {
                System.out.println(indent + " Child " + i + ":");
                printQuadTree(children[i], depth + 1);
            }
        }
    }

    private static void fillImageBuffer(int[] buffer, ImageQuadTree iqt, int x, int y, int width,
            int height, int imageWidth) {
        byte[] mean = iqt.getAverageColor();
        int packed = (0b11111111 << 24) | ((mean[0] & 0xff) << 16) | ((mean[1] & 0xff) << 8) | (mean[2] & 0xff);
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                // System.out.println("Filling " + x + " and " + y);
                buffer[i * imageWidth + j] = packed;
            }
        }

        if (iqt.isLeafNode())
            return;

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        fillImageBuffer(buffer, iqt.getChildren(0), x, y, halfWidth, halfHeight, imageWidth);
        fillImageBuffer(buffer, iqt.getChildren(1), x + halfWidth, y, halfWidth, halfHeight, imageWidth);
        fillImageBuffer(buffer, iqt.getChildren(2), x, y + halfHeight, halfWidth, halfHeight, imageWidth);
        fillImageBuffer(buffer, iqt.getChildren(3), x + halfWidth, y + halfHeight, halfWidth, halfHeight, imageWidth);

    }

    public static int[] getCompressedImageBuffer(ImageQuadTree iqt, ImageData id) {
        int imageSize = (int) Math.round(Math.pow(2, iqt.getDepth() - 1));
        int[] compressedImageBuffer = new int[imageSize * imageSize];

        fillImageBuffer(compressedImageBuffer, iqt, 0, 0, imageSize, imageSize, imageSize);

        return compressedImageBuffer;

    }

}
