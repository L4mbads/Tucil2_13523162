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
            System.out.println("Threshold akan diatur otomatis");
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
            TimeProfiler timeProfiler = new TimeProfiler();

            String fileAbsolutePath = null;
            File inputFile = null;
            while (inputFile == null || !inputFile.isFile()) {
                fileAbsolutePath = safeScanner.getInput("Alamat absolut gambar", String.class);
                inputFile = new File(fileAbsolutePath);
            }

            timeProfiler.startSection("Memuat Gambar");
            System.out.println("Memuat Gambar...");
            ImageCompressor imageCompressor = new ImageCompressor(inputFile);
            timeProfiler.stopSection();

            System.out.println("1. Varians");
            System.out.println("2. Mean Absolute Difference (MAD)");
            System.out.println("3. Mean Pixel Difference (MPD)");
            System.out.println("4. Entropi");
            System.out.println("5. Structural Similarity Index Measure (SSIM)");
            int method = safeScanner.getBoundedInput("method: ", Integer.class, 1, 5);
            imageCompressor.setMethod(method);

            float threshold = safeScanner.getBoundedInput("Threshold nilai error", Float.class, 0.0f,
                    imageCompressor.getMaxErrorValue());

            int minimumBlockSize = safeScanner.getBoundedInput("Ukuran blok minimum", Integer.class, 1,
                    Integer.MAX_VALUE);

            float compressionLevel = safeScanner.getBoundedInput("Target persentase kompresi", Float.class, 0.0f, 1.0f);

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
                outputFileAbsolutePath = safeScanner.getInput("Alamat absolut gambar hasil", String.class);
                outputFile = new File(outputFileAbsolutePath);
            }
            if (outputFile.isFile()) {
                System.out.println("Gambar sudah ada. Akan dilakukan overwrite");
            }

            imageCompressor.setOutputFile(outputFile);

            String outputGifAbsolutePath = null;
            File outputGif = null;
            while (outputGif == null || outputGif.getParentFile() == null || !outputGif.getParentFile().isDirectory()) {
                outputGifAbsolutePath = safeScanner.getInput("Alamat absolut GIF hasil (n untuk skip)", String.class);
                if (outputGifAbsolutePath.equalsIgnoreCase("n")) {
                    System.out.println("Tidak akan membuat GIF");
                    outputGif = null;
                    break;
                }
                outputGif = new File(outputGifAbsolutePath);
            }
            if (outputGif != null && outputGif.isFile()) {
                System.out.println("GIF sudah ada. Akan dilakukan overwrite");
            }

            timeProfiler.startSection("Konstruksi Quadtree");
            imageCompressor.compress();
            timeProfiler.stopSection();

            if (imageCompressor.isTargetPercentageEnabled()) {
                System.out.println("Mencoba memenuhi target kompresi...");
                timeProfiler.startSection("Binary Refine + Ekspor");
                imageCompressor.binaryRefine();
            } else {
                System.out.println("Mengeskpor gambar...");
                timeProfiler.startSection("Ekspor Gambar");
                imageCompressor.exportImage();
            }
            timeProfiler.stopSection();

            if (outputGif != null) {
                timeProfiler.startSection("Ekspor GIF");
                System.out.println("Mengekspor GIF...");
                imageCompressor.exportGIF(outputGif);
                timeProfiler.stopSection();
            }
            System.out.println("Kompresi berhasil");
            System.out.println("");
            timeProfiler.print();
            System.out.println("");
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
