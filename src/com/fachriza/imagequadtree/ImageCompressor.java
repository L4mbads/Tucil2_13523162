package com.fachriza.imagequadtree;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.image.errormeasuremethod.EntropyError;
import com.fachriza.imagequadtree.image.errormeasuremethod.ErrorMeasurementMethod;
import com.fachriza.imagequadtree.image.errormeasuremethod.MADError;
import com.fachriza.imagequadtree.image.errormeasuremethod.MPDError;
import com.fachriza.imagequadtree.image.errormeasuremethod.SSIMError;
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

    private long fileSize;

    private File outputFile;

    private ImageData imageData;

    public ImageCompressor(File inputFile) throws IOException {
        fileSize = inputFile.length();
        imageData = new ImageData(ImageIO.read(inputFile), ImageUtil.getFormatName(inputFile));
    }

    public ImageCompressor setMethod(int method) {
        switch (method) {
            case 1:
                emm = new VarianceError(imageData);
                break;
            case 2:
                emm = new MADError(imageData);
                break;
            case 3:
                emm = new MPDError(imageData);
                break;
            case 4:
                emm = new EntropyError(imageData);
                break;
            case 5:
                emm = new SSIMError(imageData);
                break;
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
        if (compressionLevel > 0.0f) {
            System.out.println("auto adjust threshold");
            // Assume linear
            threshold = compressionLevel * emm.getMaxErrorValue();
        }
        return this;
    }

    public ImageCompressor setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    private void binaryRefine() throws IOException {

        draw(outputFile);

        float upperBound = getMaxErrorValue();
        float lowerBound = 0.0f;
        float delta = getCompressRatio() - compressionLevel;
        while (Math.abs(delta) > 0.01f) {
            if (delta < 0.0) {
                lowerBound = threshold;
                threshold = (threshold + upperBound) / 2.0f;
            } else {
                upperBound = threshold;
                threshold = (lowerBound + threshold) / 2.0f;
            }

            builder.setThreshold(threshold);

            builder.adjust(root, 0, 0, imageData.getWidth(), imageData.getHeight());

            draw(outputFile);

            delta = getCompressRatio() - compressionLevel;
            // System.out.println(delta);
        }
    }

    public ImageQuadTree compress() throws IOException {
        builder = new ImageQuadTreeBuilder(emm, imageData, threshold, minimumBlockSize,
                compressionLevel);

        root = builder.build(0, 0, imageData.getWidth(), imageData.getHeight());

        return root;
    }

    public float getCompressRatio() {
        return (1.0f - ((float) outputFile.length() / fileSize));
    }

    public float getMaxErrorValue() {
        if (emm != null)
            return emm.getMaxErrorValue();
        return 0.0f;
    }

    public void draw(File outputGif) throws IOException {

        ImageQuadTreeDrawer.draw(root, builder, outputFile);
    }

    public boolean isTargetPercentageEnabled() {
        return compressionLevel > 0.0f;
    }

    /* ============================================ */
    /* ==========MAIN PROGRAM ENTRY POINT========== */
    /* ============================================ */
    public static void main(String[] args) {
        try (SafeScanner safeScanner = new SafeScanner(new Scanner(System.in))) {

            TimeProfiler timeProfiler = new TimeProfiler("Image loading", "Tree construction",
                    "Tree adjustment + output");

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

            System.out.println("1. Variance");
            System.out.println("2. Mean Absolute Difference");
            System.out.println("3. Mean Pixel Difference");
            System.out.println("4. Entropy");
            System.out.println("5. SSIM");
            int method = safeScanner.getBoundedInput("method: ", Integer.class, 1, 5);
            imageCompressor.setMethod(method);

            float threshold = safeScanner.getBoundedInput("threshold", Float.class, 0.0f,
                    imageCompressor.getMaxErrorValue());

            int minimumBlockSize = safeScanner.getBoundedInput("minimum block size: ", Integer.class, 1,
                    Integer.MAX_VALUE);

            float compressionLevel = safeScanner.getBoundedInput("compression target level: ", Float.class, 0.0f, 1.0f);

            imageCompressor
                    .setMinimumBlockSize(minimumBlockSize)
                    .setThreshold(threshold)
                    .setCompressionLevel(compressionLevel);

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

            imageCompressor.setOutputFile(outputFile);

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
            ImageQuadTree tree;
            try {
                tree = imageCompressor.compress();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
            timeProfiler.stop();

            timeProfiler.startNext();
            try {
                if (imageCompressor.isTargetPercentageEnabled()) {
                    imageCompressor.binaryRefine();
                } else {
                    imageCompressor.draw(outputGif);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
            timeProfiler.stop();

            timeProfiler.print();
            System.out.println("Sebelum: " + (float) inputFile.length() / 1024.0f);
            System.out.println("Sesudah: " + (float) outputFile.length() / 1024.0f);
            System.out.println("Rasio: " + imageCompressor.getCompressRatio());
            System.out.println("Depth: " + tree.getDepth());
        }
    }
}
