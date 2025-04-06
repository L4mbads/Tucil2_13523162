package com.fachriza.imagequadtree;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.fachriza.imagequadtree.image.ImageData;
import com.fachriza.imagequadtree.image.errormeasuremethod.*;
import com.fachriza.imagequadtree.quadtree.*;
import com.fachriza.imagequadtree.utils.*;

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
            threshold = compressionLevel * emm.getMaxErrorValue();
            minimumBlockSize = 1;
        }
        return this;
    }

    public ImageCompressor setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
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

    public ImageQuadTree compress() throws IOException {
        builder = new ImageQuadTreeBuilder(emm, imageData, threshold, minimumBlockSize);

        root = builder.build(0, 0, imageData.width, imageData.height);

        return root;
    }

    private boolean rebuild() throws IOException {

        exportImage();

        int width = imageData.width;
        int height = imageData.height;

        float upperBound = getMaxErrorValue();
        float lowerBound = 0.0f;
        float sizeDelta = getCompressRatio() - compressionLevel;
        float thresholdDelta = upperBound - lowerBound;

        final float RATIO_LENIENCE = 0.01f; // 1%

        byte iteration = 0;
        while (iteration < 100 && Math.abs(sizeDelta) > RATIO_LENIENCE && thresholdDelta > 0.1f) {
            iteration++;
            if (sizeDelta < 0.0) {
                lowerBound = threshold;
                threshold = (threshold + upperBound) / 2.0f;
            } else {
                upperBound = threshold;
                threshold = (lowerBound + threshold) / 2.0f;
            }

            builder.setThreshold(threshold);
            builder.resetNodeCount();

            root = builder.build(0, 0, width, height);

            exportImage();

            sizeDelta = getCompressRatio() - compressionLevel;
            thresholdDelta = upperBound - lowerBound;
        }
        return (Math.abs(sizeDelta) <= RATIO_LENIENCE);
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
    /* ========= MAIN PROGRAM ENTRY POINT ========= */
    /* ============================================ */
    public static void main(String[] args) {
        try (SafeScanner safeScanner = new SafeScanner(new Scanner(System.in))) {
            TimeProfiler timeProfiler = new TimeProfiler();

            // Read input image path
            String inputImageAbsolutePath = null;
            File inputImageFile = null;
            while (true) {
                inputImageAbsolutePath = safeScanner.getInput("Alamat absolut gambar", String.class);
                inputImageFile = new File(inputImageAbsolutePath);

                String inputImageFormat = ImageUtil.getFormatName(inputImageFile);
                if (!inputImageFile.isFile()) {
                    System.out.println("File input tidak valid!");
                } else if (inputImageFormat != "jpeg" && inputImageFormat != "png") {
                    System.out.println("Format file input tidak disupport! Masukkan .png/.jpg/.jpeg");
                } else {
                    break;
                }
            }

            // Load input image
            System.out.println("Memuat gambar...");
            timeProfiler.startSection("Memuat Gambar");

            ImageCompressor imageCompressor = new ImageCompressor(inputImageFile);

            timeProfiler.stopSection();

            // Read method
            System.out.println("1. Varians");
            System.out.println("2. Mean Absolute Difference (MAD)");
            System.out.println("3. Mean Pixel Difference (MPD)");
            System.out.println("4. Entropi");
            System.out.println("5. Structural Similarity Index Measure (SSIM)");
            int method = safeScanner.getBoundedInput("Metode: ", Integer.class, 1, 5);
            imageCompressor.setMethod(method);

            // Read threshold
            float threshold = safeScanner.getBoundedInput("Threshold nilai error", Float.class, 0.0f,
                    imageCompressor.getMaxErrorValue());

            // Read minimum block size
            int minimumBlockSize = safeScanner.getBoundedInput("Ukuran blok minimum", Integer.class, 1,
                    imageCompressor.imageData.width * imageCompressor.imageData.height);

            // Read target compression percentage
            float compressionLevel = safeScanner.getBoundedInput("Target persentase kompresi", Float.class, 0.0f, 1.0f);
            if (compressionLevel > 0.0f) {
                System.out.println("Threshold dan ukuran blok minimum akan diatur otomatis");
            } else {
                System.out.println("Mode target kompresi dinonaktifkan");
            }

            // Set compression parameters
            imageCompressor
                    .setMinimumBlockSize(minimumBlockSize)
                    .setThreshold(threshold)
                    .setCompressionLevel(compressionLevel);

            // Read output image path
            String outputImageAbsolutePath = null;
            File outputImageFile = null;
            while (true) {
                outputImageAbsolutePath = safeScanner.getInput("Alamat absolut gambar hasil", String.class);
                outputImageFile = new File(outputImageAbsolutePath);

                if (outputImageFile.getParentFile() == null) {
                    System.out.println("Direktori tidak valid!");
                } else if (!outputImageFile.getParentFile().isDirectory()) {
                    System.out.println("Direktori tidak valid!");
                } else if (ImageUtil.getFormatName(outputImageFile) != ImageUtil.getFormatName(inputImageFile)) {
                    System.out.println("Format output dan input harus sama!");
                } else {
                    if (outputImageFile.isFile()) {
                        System.out.println("Gambar sudah ada. Akan dilakukan overwrite");
                    }
                    break;
                }
            }

            imageCompressor.setOutputFile(outputImageFile);

            // Read output GIF path
            String outputGifAbsolutePath = null;
            File outputGifFile = null;
            while (true) {
                outputGifAbsolutePath = safeScanner.getInput("Alamat absolut GIF hasil (n untuk skip)", String.class);
                if (outputGifAbsolutePath.equalsIgnoreCase("n")) {
                    System.out.println("Tidak akan membuat GIF");
                    outputGifFile = null;
                    break;
                }

                outputGifFile = new File(outputGifAbsolutePath);

                if (outputGifFile.getParentFile() == null) {
                    System.out.println("Direktori tidak valid!");
                } else if (!outputGifFile.getParentFile().isDirectory()) {
                    System.out.println("Direktori tidak valid!");
                } else if (ImageUtil.getFormatName(outputGifFile) != "gif") {
                    System.out.println("Format output harus berupa .gif!");
                } else {
                    if (outputGifFile.isFile()) {
                        System.out.println("GIF sudah ada. Akan dilakukan overwrite");
                    }
                    break;
                }
            }

            // Construct quadtree
            System.out.println("Membangun quadtree...");
            timeProfiler.startSection("Build Quadtree");

            imageCompressor.compress();

            timeProfiler.stopSection();

            boolean targetReached = true;
            if (imageCompressor.isTargetPercentageEnabled()) {
                // Quadtree rebuild + export image
                System.out.println("Mencoba memenuhi target kompresi...");
                timeProfiler.startSection("Rebuild + Ekspor");

                targetReached = imageCompressor.rebuild();
            } else {
                // Export image
                System.out.println("Mengeskpor gambar...");
                timeProfiler.startSection("Ekspor Gambar");

                imageCompressor.exportImage();
            }

            timeProfiler.stopSection();
            if (!targetReached) {
                System.out.println("Tidak mampu mencapai target kompresi");
            }

            if (outputGifFile != null) {
                // Export GIF
                System.out.println("Mengekspor GIF...");
                timeProfiler.startSection("Ekspor GIF");

                imageCompressor.exportGIF(outputGifFile);

                timeProfiler.stopSection();
            }

            // Display compression statistics
            System.out.println("Kompresi berhasil");
            System.out.println("");
            timeProfiler.print();
            System.out.println("");
            System.out.format("Sebelum         : %.2f KB%n", (float) inputImageFile.length() / 1024.0f);
            System.out.format("Sesudah         : %.2f KB%n", (float) outputImageFile.length() / 1024.0f);
            System.out.format("Rasio Kompresi  : %.2f%%", imageCompressor.getCompressRatio() * 100.0f);
            if (imageCompressor.isTargetPercentageEnabled() && !targetReached) {
                System.out.println(
                        " (Target tidak bisa dicapai karena iterasi terlalu banyak atau tidak memungkinkan)");
            } else {
                System.out.println("");
            }
            System.out.format("Kedalaman Pohon : %d%n", imageCompressor.getCompressedTree().getDepth());
            System.out.format("Jumlah Simpul   : %d%n", imageCompressor.getNodeCount());
            System.out.println("");
            System.out.format("Alamat Gambar   : %s%n", inputImageAbsolutePath);
            if (outputGifFile != null)
                System.out.format("Alamat GIF      : %s%n", outputGifAbsolutePath);

        } catch (Exception e) {
            System.out.println("Terjadi error. Cek log/crash.log untuk melihat log");
            CrashLogger.logException(e);
        }
    }

}