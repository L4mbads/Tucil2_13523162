package com.fachriza.imagequadtree.quadtree;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fachriza.imagequadtree.image.ImageData;
import com.github.dragon66.AnimatedGIFWriter;

public class ImageQuadTreeExporter {

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
        fillImageBuffer(buffer, iqt.getChildren(1), x + halfLowerWidth, y, halfUpperWidth, halfLowerHeight, imageData,
                iteration + 1,
                targetDepth);
        fillImageBuffer(buffer, iqt.getChildren(2), x, y + halfLowerHeight, halfLowerWidth, halfUpperHeight, imageData,
                iteration + 1,
                targetDepth);
        fillImageBuffer(buffer, iqt.getChildren(3), x + halfLowerWidth, y + halfLowerHeight, halfUpperWidth,
                halfUpperHeight, imageData,
                iteration + 1, targetDepth);

    }

    public static int[] getCompressedImageBuffer(ImageQuadTree root, ImageData imageData, int targetDepth) {

        int width = imageData.getWidth();
        int height = imageData.getHeight();
        int[] compressedImageBuffer = new int[width * height];
        fillImageBuffer(compressedImageBuffer, root, 0, 0, width, height, imageData, 1, targetDepth);

        return compressedImageBuffer;

    }

    public static void exportImage(ImageQuadTree iqt, ImageQuadTreeBuilder builder, File outputFile)
            throws IOException {

        ImageData imageData = builder.getImageData();

        String format = imageData.getFormat();

        int width = imageData.getWidth();
        int height = imageData.getHeight();

        int[] buffer = getCompressedImageBuffer(iqt, imageData, 0);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, width, height, buffer, 0, width);

        ImageIO.write(image, format, outputFile);
    }

    public static void exportGIF(ImageQuadTree iqt, ImageQuadTreeBuilder builder, File outputFile) throws Exception {
        ImageData imageData = builder.getImageData();

        int width = imageData.getWidth();
        int height = imageData.getHeight();

        AnimatedGIFWriter writer = new AnimatedGIFWriter(false);
        OutputStream os = new FileOutputStream(outputFile);
        writer.prepareForWrite(os, -1, -1);
        int depth = iqt.getDepth();
        for (int i = 1; i <= depth; i++) {
            int[] buffer = getCompressedImageBuffer(iqt, imageData, i);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            image.setRGB(0, 0, width, height, buffer, 0, width);
            writer.writeFrame(os, image, 1000);
        }
        writer.finishWrite(os);
    }
}
