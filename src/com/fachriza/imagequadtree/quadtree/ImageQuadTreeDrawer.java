package com.fachriza.imagequadtree.quadtree;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fachriza.imagequadtree.image.ImageData;

public class ImageQuadTreeDrawer {

    private static void fillImageBuffer(int[] buffer, ImageQuadTree iqt, int x, int y, int width,
            int height, ImageData imageData, int iteration, int targetDepth) {
        if (iqt.isLeafNode() || (iteration == targetDepth)) {
            byte[] mean = iqt.getAverageColor();
            int packed = (0b11111111 << 24) | ((mean[0] & 0xff) << 16) | ((mean[1] & 0xff) << 8) | (mean[2] & 0xff);
            for (int i = y; i <= y + height && i < imageData.getHeight(); i++) {
                for (int j = x; j <= x + width && j < imageData.getWidth(); j++) {
                    buffer[i * imageData.getWidth() + j] = packed;
                }
            }
            return;
        }

        int halfLowerWidth = width / 2;
        int halfLowerHeight = height / 2;
        int halfUpperWidth = width - halfLowerWidth;
        int halfUpperHeight = height - halfLowerHeight;

        fillImageBuffer(buffer, iqt.getChildren(0), x, y, halfLowerWidth, halfLowerHeight, imageData, iteration + 1,
                targetDepth);
        fillImageBuffer(buffer, iqt.getChildren(1), x + halfLowerWidth, y, halfUpperWidth, halfUpperHeight, imageData,
                iteration + 1,
                targetDepth);
        fillImageBuffer(buffer, iqt.getChildren(2), x, y + halfLowerHeight, halfUpperWidth, halfUpperHeight, imageData,
                iteration + 1,
                targetDepth);
        fillImageBuffer(buffer, iqt.getChildren(3), x + halfLowerWidth, y + halfLowerHeight, halfUpperWidth,
                halfUpperHeight, imageData,
                iteration + 1, targetDepth);

    }

    public static int[] getCompressedImageBuffer(ImageQuadTree root, ImageData imageData) {

        int width = imageData.getWidth();
        int height = imageData.getHeight();
        int[] compressedImageBuffer = new int[width * height];
        fillImageBuffer(compressedImageBuffer, root, 0, 0, width, height, imageData, 1, 0);

        return compressedImageBuffer;

    }

    public static void draw(ImageQuadTree iqt, ImageQuadTreeBuilder builder, File outputFile) throws IOException {

        ImageData imageData = builder.getImageData();

        String format = imageData.getFormat();

        int width = imageData.getWidth();
        int height = imageData.getHeight();

        int finalWidth = width;
        int finalHeight = height;

        int[] buffer = getCompressedImageBuffer(iqt, imageData);
        BufferedImage image = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, finalWidth, finalHeight, buffer, 0, finalWidth);

        ImageIO.write(image, format, outputFile);
    }

    // private static void writeImageWithSameFormat(BufferedImage image, File
    // originalFile, String outputPath)
    // throws IOException {
    // String format = getFormatName(originalFile);
    // if (format == null) {
    // throw new IOException("Could not determine image format.");
    // }

    // File outputFile = new File(outputPath);
    // ImageWriter writer = null;
    // Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
    // if (writers.hasNext()) {
    // writer = writers.next();
    // } else {
    // throw new IOException("No writer available for format: " + format);
    // }

    // try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
    // writer.setOutput(ios);
    // writer.write(image);
    // } finally {
    // writer.dispose();
    // }
    // }

    public static int gcd(int a, int b) {
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }
}
