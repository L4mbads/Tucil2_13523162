package com.fachriza.imagequadtree.quadtree;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageQuadTreeDrawer {

    private static void fillImageBuffer(int[] buffer, ImageQuadTree iqt, int x, int y, int width,
            int height, int imageWidth) {
        if (iqt.isLeafNode()) {
            byte[] mean = iqt.getAverageColor();
            int packed = (0b11111111 << 24) | ((mean[0] & 0xff) << 16) | ((mean[1] & 0xff) << 8) | (mean[2] & 0xff);
            for (int i = y; i < y + height; i++) {
                for (int j = x; j < x + width; j++) {
                    buffer[i * imageWidth + j] = packed;
                }
            }
            return;
        }

        int halfWidth = width / 2;
        int halfHeight = height / 2;
        fillImageBuffer(buffer, iqt.getChildren(0), x, y, halfWidth, halfHeight, imageWidth);
        fillImageBuffer(buffer, iqt.getChildren(1), x + halfWidth, y, halfWidth, halfHeight, imageWidth);
        fillImageBuffer(buffer, iqt.getChildren(2), x, y + halfHeight, halfWidth, halfHeight, imageWidth);
        fillImageBuffer(buffer, iqt.getChildren(3), x + halfWidth, y + halfHeight, halfWidth, halfHeight, imageWidth);

    }

    public static int[] getCompressedImageBuffer(ImageQuadTree root, int width, int height) {

        int[] compressedImageBuffer = new int[width * height];
        fillImageBuffer(compressedImageBuffer, root, 0, 0, width, height, width);

        return compressedImageBuffer;

    }

    public static void draw(ImageQuadTree iqt, ImageQuadTreeBuilder builder, File outputFile) throws IOException {

        int factor = gcd(builder.lowestDimension[0], builder.lowestDimension[1]);
        int imageSize = (int) Math.round(Math.pow(2, iqt.getDepth() - 1));
        int finalWidth = builder.lowestDimension[0] / factor * imageSize;
        int finalHeight = builder.lowestDimension[1] / factor * imageSize;
        int[] buffer = getCompressedImageBuffer(iqt, finalWidth, finalHeight);
        BufferedImage image = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_ARGB);

        image.setRGB(0, 0, finalWidth, finalHeight, buffer, 0, finalWidth);

        ImageIO.write(image, "png", outputFile);
    }

    public static int gcd(int a, int b) {
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }
}
