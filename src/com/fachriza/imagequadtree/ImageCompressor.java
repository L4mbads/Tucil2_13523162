package com.fachriza.imagequadtree;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.image.errormeasuremethod.ErrorMeasurementMethod;
import com.fachriza.imagequadtree.image.errormeasuremethod.VarianceError;
import com.fachriza.imagequadtree.quadtree.ImageQuadTree;
import com.fachriza.imagequadtree.quadtree.ImageQuadTreeBuilder;
import com.fachriza.imagequadtree.utils.ImageUtil;
import com.fachriza.imagequadtree.utils.SafeScanner;

public class ImageCompressor {
    private float threshold;
    private int minimumBlockSize;
    private float compressionLevel;

    private ErrorMeasurementMethod emm;

    private ImageData imageData;

    public ImageCompressor(File inputFile) throws IOException {
        imageData = new ImageData(ImageIO.read(inputFile));
    }

    public ImageCompressor setMethod(int method) {
        switch (method) {
            default:
                emm = new VarianceError(imageData);
                break;
        }
        return this;
    }

    public ImageCompressor setThreshold(float threshold) {
        this.threshold = threshold;
        return this;
    }

    public ImageCompressor setMinimumBlockSize(int minimumBlockSize) {
        this.minimumBlockSize = minimumBlockSize;
        return this;
    }

    public ImageCompressor setCompressionLevel(float compressionLevel) {
        this.compressionLevel = compressionLevel;
        return this;
    }

    public void compress(File outputFile, File outputGif) throws IOException {
        ImageQuadTreeBuilder builder = new ImageQuadTreeBuilder(emm, imageData, threshold, minimumBlockSize,
                compressionLevel);

        ImageQuadTree iqt = builder.build(0, 0, imageData.getWidth(), imageData.getHeight());
        int[] buffer = ImageQuadTreeBuilder.getCompressedImageBuffer(iqt, imageData);
        // test
        // byte[] avg = ImageUtil.getAverageColor(imageData, 0, 0, imageData.getWidth(),
        // imageData.getHeight());
        // ImageQuadTreeBuilder.printQuadTree(iqt, 0);

        int imageSize = (int) Math.pow(2, iqt.getDepth() - 1);
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        // int[] packed = { 0 };

        // packed[0] = (0b11111111 << 24) | ((avg[0] & 0xff) << 16) | ((avg[1] & 0xff)
        // << 8) | (avg[2] & 0xff);

        image.setRGB(0, 0, imageSize, imageSize, buffer, 0, imageSize);

        ImageIO.write(image, "png", outputFile);

        return;
    }

    public static void main(String[] args) {
        SafeScanner safeScanner = new SafeScanner(new Scanner(System.in));
        String fileAbsolutePath = null;
        File inputFile = null;

        while (inputFile == null || !inputFile.isFile()) {
            fileAbsolutePath = safeScanner.getInput("Enter input file path", String.class);
            inputFile = new File(fileAbsolutePath);
        }

        ImageCompressor imageCompressor = null;
        try {
            imageCompressor = new ImageCompressor(inputFile);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        int method = safeScanner.getBoundedInput("method: ", Integer.class, 0, 4);
        float threshold = safeScanner.getBoundedInput("threshold", Float.class, 0.0f, Float.MAX_VALUE);
        int minimumBlockSize = safeScanner.getBoundedInput("minimum block size: ", Integer.class, 1, Integer.MAX_VALUE);
        float compressionLevel = safeScanner.getBoundedInput("compression target level: ", Float.class, 0.0f, 1.0f);

        imageCompressor.setMethod(method)
                .setCompressionLevel(compressionLevel)
                .setMinimumBlockSize(minimumBlockSize)
                .setThreshold(threshold);

        String outputFileAbsolutePath = null;
        File outputFile = null;
        while (outputFile == null || outputFile.getParentFile() == null || !outputFile.getParentFile().isDirectory()) {
            outputFileAbsolutePath = safeScanner.getInput("Enter output file path", String.class);
            outputFile = new File(outputFileAbsolutePath);
        }
        if (outputFile.isFile()) {
            System.out.println("File already exists. Will overwrite later");
        }

        String outputGifAbsolutePath = null;
        File outputGif = null;
        while (outputGif == null || outputGif.getParentFile() == null || !outputGif.getParentFile().isDirectory()) {
            outputGifAbsolutePath = safeScanner.getInput("Enter output GIF path", String.class);
            outputGif = new File(outputGifAbsolutePath);
        }
        if (outputGif.isFile()) {
            System.out.println("File already exists. Will overwrite later");
        }

        try {
            long startTime = System.nanoTime();
            imageCompressor.compress(outputFile, outputGif);

            System.out.println((System.nanoTime() - startTime) * 1e-6);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
