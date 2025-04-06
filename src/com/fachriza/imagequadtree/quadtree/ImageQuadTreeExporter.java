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
            ImageData imageData,
            int iteration,
            int targetDepth) {

        boolean isTargetIteration = iteration == targetDepth;

        if (node.isLeafNode() || isTargetIteration) {
            int imageWidth = imageData.width;
            int imageHeight = imageData.height;
            int heightBound = node.y + node.height;
            int widthBound = node.x + node.width;

            int packed = ImageUtil.pack24BitColors(node.averageColor);

            for (int i = node.y; i <= heightBound && i < imageHeight; i++) {
                for (int j = node.x; j <= widthBound && j < imageWidth; j++) {
                    buffer[i * imageWidth + j] = packed;
                }
            }
            return;
        }

        int nextIteration = ++iteration;
        for (ImageQuadTree child : node.getChildrenArray()) {
            fillImageBuffer(buffer, child, imageData,
                    nextIteration, targetDepth);
        }
    }

    public static int[] getCompressedImageBuffer(
            ImageQuadTree root,
            ImageData imageData,
            int targetDepth) {

        int[] compressedImageBuffer = new int[imageData.width * imageData.height];
        fillImageBuffer(compressedImageBuffer, root, imageData, 1, targetDepth);

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
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 1; i <= depth; i++) {
            int[] buffer = getCompressedImageBuffer(iqt, imageData, i);
            image.setRGB(0, 0, width, height, buffer, 0, width);
            writer.writeFrame(os, image, 1000);
        }

        writer.finishWrite(os);
    }

}
