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
import com.fachriza.imagequadtree.quadtree.ImageQuadTreeDrawer;
import com.fachriza.imagequadtree.utils.ImageUtil;
import com.fachriza.imagequadtree.utils.SafeScanner;
import com.fachriza.imagequadtree.utils.TimeProfiler;

public class ImageCompressor {
    private float threshold;
    private int minimumBlockSize;
    private float compressionLevel;

    private ImageQuadTree root;
    private ImageQuadTreeBuilder builder;
    private ErrorMeasurementMethod emm;

    private ImageData imageData;

    public ImageCompressor(File inputFile) throws IOException {
        imageData = new ImageData(ImageIO.read(inputFile), ImageUtil.getFormatName(inputFile));
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

    public ImageQuadTree compress() {
        builder = new ImageQuadTreeBuilder(emm, imageData, threshold, minimumBlockSize,
                compressionLevel);

        root = builder.build(0, 0, imageData.getWidth(), imageData.getHeight());

        return root;
    }

    public void draw(File outputFile, File outputGif) throws IOException {

        ImageQuadTreeDrawer.draw(root, builder, outputFile);
    }

    /* ============================================ */
    /* ==========MAIN PROGRAM ENTRY POINT========== */
    /* ============================================ */
    public static void main(String[] args) {
        try (SafeScanner safeScanner = new SafeScanner(new Scanner(System.in))) {

            TimeProfiler timeProfiler = new TimeProfiler("Image loading", "Tree construction", "Image saving");

            String fileAbsolutePath = null;
            File inputFile = null;

            while (inputFile == null || !inputFile.isFile()) {
                fileAbsolutePath = safeScanner.getInput("Enter input file path", String.class);
                inputFile = new File(fileAbsolutePath);
            }

            ImageCompressor imageCompressor = null;
            timeProfiler.startNext();
            try {
                imageCompressor = new ImageCompressor(inputFile);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
            timeProfiler.stop();

            int method = safeScanner.getBoundedInput("method: ", Integer.class, 0, 4);
            float threshold = safeScanner.getBoundedInput("threshold", Float.class, 0.0f, Float.MAX_VALUE);
            int minimumBlockSize = safeScanner.getBoundedInput("minimum block size: ", Integer.class, 1,
                    Integer.MAX_VALUE);
            float compressionLevel = safeScanner.getBoundedInput("compression target level: ", Float.class, 0.0f, 1.0f);

            imageCompressor.setMethod(method)
                    .setCompressionLevel(compressionLevel)
                    .setMinimumBlockSize(minimumBlockSize)
                    .setThreshold(threshold);

            String outputFileAbsolutePath = null;
            File outputFile = null;
            try {
                while (outputFile == null
                        || outputFile.getParentFile() == null
                        || !outputFile.getParentFile().isDirectory()
                        || ImageUtil.getFormatName(outputFile) != ImageUtil.getFormatName(inputFile)) {
                    outputFileAbsolutePath = safeScanner.getInput("Enter output file path", String.class);
                    outputFile = new File(outputFileAbsolutePath);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return;
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

            timeProfiler.startNext();
            imageCompressor.compress();
            timeProfiler.stop();

            timeProfiler.startNext();
            try {
                imageCompressor.draw(outputFile, outputGif);
            } catch (IOException e) {
                e.printStackTrace();
            }
            timeProfiler.stop();

            timeProfiler.print();
        }
    }
}
