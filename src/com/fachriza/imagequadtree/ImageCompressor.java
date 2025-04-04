package com.fachriza.imagequadtree;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.image.errormeasuremethod.EntropyError;
import com.fachriza.imagequadtree.image.errormeasuremethod.ErrorMeasurementMethod;
import com.fachriza.imagequadtree.image.errormeasuremethod.MADError;
import com.fachriza.imagequadtree.image.errormeasuremethod.MPDError;
import com.fachriza.imagequadtree.image.errormeasuremethod.SSIMError;
import com.fachriza.imagequadtree.image.errormeasuremethod.VarianceError;
import com.fachriza.imagequadtree.quadtree.ImageQuadTree;
import com.fachriza.imagequadtree.quadtree.ImageQuadTreeBuilder;
import com.fachriza.imagequadtree.quadtree.ImageQuadTreeExporter;
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
            threshold = compressionLevel * emm.getMaxErrorValue();
        }
        return this;
    }

    public ImageCompressor setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    private void binaryRefine() throws IOException {

        exportImage();

        float upperBound = getMaxErrorValue();
        float lowerBound = 0.0f;
        float delta = getCompressRatio() - compressionLevel;
        while (Math.abs(delta) > 0.01f && Math.abs(lowerBound - upperBound) > 0.1f) {
            if (delta < 0.0) {
                lowerBound = threshold;
                threshold = (threshold + upperBound) / 2.0f;
            } else {
                upperBound = threshold;
                threshold = (lowerBound + threshold) / 2.0f;
            }

            builder.setThreshold(threshold);
            builder.resetNodeCount();

            // builder.adjust(root, 0, 0, imageData.getWidth(), imageData.getHeight());
            // why is this faster what
            root = builder.build(0, 0, imageData.getWidth(), imageData.getHeight());

            exportImage();

            delta = getCompressRatio() - compressionLevel;
        }
    }

    public ImageQuadTree compress() throws IOException {
        builder = new ImageQuadTreeBuilder(emm, imageData, threshold, minimumBlockSize);

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

    public int getNodeCount() {
        return builder.getNodeCount();
    }

    public ImageQuadTree getCompressedTree() {
        return root;
    }

    public void exportImage() throws IOException {
        ImageQuadTreeExporter.exportImage(root, builder, outputFile);
    }

    public void exportGIF(File outputGIF) throws Exception {
        ImageQuadTreeExporter.exportGIF(root, builder, outputGIF);
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
                    "Tree adjustment + output", "GIF output");

            String fileAbsolutePath = null;
            File inputFile = null;
            while (inputFile == null || !inputFile.isFile()) {
                fileAbsolutePath = safeScanner.getInput("Enter input file path", String.class);
                inputFile = new File(fileAbsolutePath);
            }

            timeProfiler.startNext();
            ImageCompressor imageCompressor = new ImageCompressor(inputFile);
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
            while (outputFile == null
                    || outputFile.getParentFile() == null
                    || !outputFile.getParentFile().isDirectory()
                    || ImageUtil.getFormatName(outputFile) != ImageUtil.getFormatName(inputFile)) {
                outputFileAbsolutePath = safeScanner.getInput("Enter output file path", String.class);
                outputFile = new File(outputFileAbsolutePath);
            }

            if (outputFile.isFile()) {
                System.out.println("File already exists. Will overwrite later");
            }

            imageCompressor.setOutputFile(outputFile);

            String outputGifAbsolutePath = null;
            File outputGif = null;
            while (outputGif == null || outputGif.getParentFile() == null || !outputGif.getParentFile().isDirectory()) {
                outputGifAbsolutePath = safeScanner.getInput("Enter output GIF path", String.class);
                if (outputGifAbsolutePath.equalsIgnoreCase("n")) {
                    System.out.println("No output GIF path included.");
                    outputGif = null;
                    break;
                }
                outputGif = new File(outputGifAbsolutePath);
            }
            if (outputGif != null && outputGif.isFile()) {
                System.out.println("File already exists. Will overwrite later");
            }

            timeProfiler.startNext();
            imageCompressor.compress();
            timeProfiler.stop();

            timeProfiler.startNext();
            if (imageCompressor.isTargetPercentageEnabled()) {
                imageCompressor.binaryRefine();
            } else {
                imageCompressor.exportImage();
            }
            timeProfiler.stop();

            timeProfiler.startNext();
            if (outputGif != null) {
                System.out.println("Outputting gif");
                imageCompressor.exportGIF(outputGif);
            }
            timeProfiler.stop();

            timeProfiler.print();
            System.out.format("Sebelum         : %.2fKB%n", (float) inputFile.length() / 1024.0f);
            System.out.format("Sesudah         : %.2fKB%n", (float) outputFile.length() / 1024.0f);
            System.out.format("Rasio Kompresi  : %.5f%%%n", imageCompressor.getCompressRatio() * 100.0f);
            System.out.format("Kedalaman Pohon : %d%n", imageCompressor.getCompressedTree().getDepth());
            System.out.format("Jumlah Simpul   : %d%n", imageCompressor.getNodeCount());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
