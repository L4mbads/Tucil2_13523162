package com.fachriza.imagequadtree.quadtree;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.utils.ImageUtil;
import com.github.dragon66.AnimatedGIFWriter;

public class ImageQuadTreeExporter {

    private static void fillImageBuffer(
            int[] buffer,
            ImageQuadTree node,
            int x,
            int y,
            int width,
            int height,
            ImageData imageData,
            int iteration,
            int targetDepth) {

        if (node.isLeafNode() || (iteration == targetDepth)) {
            int imageWidth = imageData.width;
            int imageHeight = imageData.height;
            int heightBound = y + height;
            int widthBound = x + width;
            int packed = ImageUtil.pack24BitColors(node.averageColor);
            for (int i = y; i <= heightBound && i < imageHeight; i++) {
                for (int j = x; j <= widthBound && j < imageWidth; j++) {
                    buffer[i * imageWidth + j] = packed;
                }
            }
            return;
        }

        int halfLowerWidth = width / 2;
        int halfLowerHeight = height / 2;
        int halfUpperWidth = width - halfLowerWidth;
        int halfUpperHeight = height - halfLowerHeight;

        int nextIteration = ++iteration;
        fillImageBuffer(
                buffer,
                node.getChildren(0),
                x,
                y,
                halfLowerWidth,
                halfLowerHeight,
                imageData,
                nextIteration,
                targetDepth);
        fillImageBuffer(
                buffer,
                node.getChildren(1),
                x + halfLowerWidth,
                y,
                halfUpperWidth,
                halfLowerHeight,
                imageData,
                nextIteration,
                targetDepth);
        fillImageBuffer(
                buffer,
                node.getChildren(2),
                x,
                y + halfLowerHeight,
                halfLowerWidth,
                halfUpperHeight,
                imageData,
                nextIteration,
                targetDepth);
        fillImageBuffer(
                buffer,
                node.getChildren(3),
                x + halfLowerWidth,
                y + halfLowerHeight,
                halfUpperWidth,
                halfUpperHeight,
                imageData,
                nextIteration,
                targetDepth);

    }

    public static int[] getCompressedImageBuffer(
            ImageQuadTree root,
            ImageData imageData,
            int targetDepth) {

        int width = imageData.width;
        int height = imageData.height;
        int[] compressedImageBuffer = new int[width * height];
        fillImageBuffer(compressedImageBuffer, root, 0, 0, width, height, imageData, 1, targetDepth);

        return compressedImageBuffer;

    }

    public static void exportImage(
            ImageQuadTree iqt,
            ImageQuadTreeBuilder builder,
            File outputFile)
            throws IOException {

        ImageData imageData = builder.getImageData();

        String format = imageData.format;

        int width = imageData.width;
        int height = imageData.height;

        int[] buffer = getCompressedImageBuffer(iqt, imageData, 0);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        image.setRGB(0, 0, width, height, buffer, 0, width);

        ImageIO.write(image, format, outputFile);
    }

    public static void exportGIF(
            ImageQuadTree iqt,
            ImageQuadTreeBuilder builder,
            File outputFile)
            throws Exception {

        ImageData imageData = builder.getImageData();

        int width = imageData.width;
        int height = imageData.height;

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
