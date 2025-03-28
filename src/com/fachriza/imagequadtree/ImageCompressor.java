package com.fachriza.imagequadtree;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import com.fachriza.imagequadtree.utils.SafeScanner;

public class ImageCompressor {
    private int method;
    private float threshold;
    private int minimumBlockSize;
    private float compressionLevel;

    private int[] buffer;
    private int width;
    private int height;

    public ImageCompressor(File inputFile) throws IOException {
        BufferedImage img = ImageIO.read(inputFile);

        this.width = img.getWidth();
        this.height = img.getHeight();

        // int[] pixels = new int[width * height];
        // img.getRGB(0, 0, width, height, pixels, 0, width); // Convert to array
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        DataBufferInt dataBuffer = (DataBufferInt) image.getRaster().getDataBuffer();
        this.buffer = dataBuffer.getData();
    }

    public ImageCompressor setMethod(int method) {
        this.method = method;
        return this;
    }

    public ImageCompressor setThreshold(float threshold) {
        this.threshold = method;
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

    public void compress(File outputFile, File outputGif) {
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
        float threshold = safeScanner.getBoundedInput("threshold", Float.class, 0.0f, 10.0f);
        int minimumBlockSize = safeScanner.getBoundedInput("minimum block size: ", Integer.class, 1, Integer.MAX_VALUE);
        float compressionLevel = safeScanner.getBoundedInput("compression target level: ", Float.class, 0.0f, 1.0f);

        imageCompressor.setMethod(method).setCompressionLevel(compressionLevel).setMinimumBlockSize(minimumBlockSize)
                .setThreshold(threshold);

        String outputFileAbsolutePath = null;
        File outputFile = null;
        while (outputFile == null || !outputFile.getParentFile().isDirectory()) {
            outputFileAbsolutePath = safeScanner.getInput("Enter output file path", String.class);
            outputFile = new File(outputFileAbsolutePath);
        }

        String outputGifAbsolutePath = null;
        File outputGif = null;
        while (outputGif == null || !outputGif.getParentFile().isDirectory()) {
            outputGifAbsolutePath = safeScanner.getInput("Enter output GIF path", String.class);
            outputGif = new File(outputGifAbsolutePath);
        }

        imageCompressor.compress(outputFile, outputGif);
    }
}
