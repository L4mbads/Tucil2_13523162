#!/bin/bash

SRC_DIR=src/com/fachriza/imagequadtree
BIN_DIR=bin
JAR_NAME=ImageCompressor.jar
MAIN_CLASS=com.fachriza.imagequadtree.ImageCompressor

mkdir -p $BIN_DIR

/usr/bin/find ./$SRC_DIR -name "*.java" > sources.txt
javac -d bin -sourcepath /$SRC_DIR @sources.txt
rm sources.txt

jar cfe $BIN_DIR/$JAR_NAME $MAIN_CLASS -C $BIN_DIR .

echo "Compilation and packaging complete. Run with:"
echo "java -jar $BIN_DIR/$JAR_NAME"