package com.fachriza.imagequadtree.utils;

import java.util.InputMismatchException;
import java.util.Scanner;

public class SafeScanner implements AutoCloseable {

    private Scanner scanner;

    public SafeScanner(Scanner scanner) {
        this.scanner = scanner;
        scanner.useDelimiter("\\s+");
    }

    @Override
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }

    public <T> T getInput(String prompt, Class<T> type) {
        while (true) {
            try {
                System.out.print(prompt + ": ");

                if (type == Integer.class) {
                    return type.cast(scanner.nextInt());
                } else if (type == Double.class) {
                    return type.cast(scanner.nextDouble());
                } else if (type == Boolean.class) {
                    return type.cast(scanner.nextBoolean());
                } else if (type == String.class) {
                    return type.cast(scanner.next());
                } else {
                    throw new IllegalArgumentException("Tipe data tidak dikenal");
                }

            } catch (InputMismatchException e) {
                System.out.println("Input invalid. Masukkan " + type.getSimpleName());
                scanner.nextLine();
            }
        }
    }

    public <T extends Number> T getBoundedInput(String prompt, Class<T> type, T min, T max) {
        while (true) {
            try {
                System.out.print(prompt + " (" + min + " - " + max + "): ");

                Number value;
                if (type == Integer.class) {
                    value = scanner.nextInt();
                } else if (type == Double.class) {
                    value = scanner.nextDouble();
                } else if (type == Long.class) {
                    value = scanner.nextLong();
                } else if (type == Float.class) {
                    value = scanner.nextFloat();
                } else {
                    throw new IllegalArgumentException("Tipe numerik tidak disupport");
                }

                if (value.doubleValue() < min.doubleValue() || value.doubleValue() > max.doubleValue()) {
                    System.out.println("Input harus berada di antara " + min + " dan " + max + ".");
                } else {
                    return type.cast(value);
                }

            } catch (InputMismatchException e) {
                System.out.println("Input invalid. Masukkan " + type.getSimpleName());
                scanner.nextLine();
            }
        }
    }
}
