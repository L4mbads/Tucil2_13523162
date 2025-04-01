#!/bin/bash

SRC_DIR=src/com/fachriza/imagequadtree
BIN_DIR=bin
JAR_NAME=ImageCompressor.jar
MAIN_CLASS=com.fachriza.imagequadtree.ImageCompressor
MANIFEST=manifest/MANIFEST.MF

mkdir -p $BIN_DIR

/usr/bin/find ./$SRC_DIR -name "*.java" > sources.txt
javac -d $BIN_DIR -cp "lib/AnimatedGIFWriter.jar" -sourcepath /$SRC_DIR @sources.txt
rm sources.txt

jar cfm $BIN_DIR/$JAR_NAME $MANIFEST -C $BIN_DIR .

echo "Compilation and packaging complete. Run with:"
echo "java -jar $BIN_DIR/$JAR_NAME"