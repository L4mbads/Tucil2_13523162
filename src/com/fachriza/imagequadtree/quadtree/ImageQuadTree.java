package com.fachriza.imagequadtree.quadtree;

public class ImageQuadTree {

    public final byte[] averageColor;
    private ImageQuadTree[] children;

    public ImageQuadTree(byte red, byte green, byte blue) {
        this.averageColor = new byte[] { red, green, blue };
        this.children = null;
    }

    public ImageQuadTree[] getChildrenArray() {
        return children;
    }

    public void setChildrenArray(ImageQuadTree[] children) {
        this.children = children;
    }

    public ImageQuadTree getChildren(int idx) {
        if (children == null)
            return null;
        return children[idx];
    }

    public void setChildren(int idx, ImageQuadTree child) {
        if (children == null) {
            children = new ImageQuadTree[4];
        }
        children[idx] = child;
    }

    public boolean isLeafNode() {
        return children == null;
    }

    public int getDepth() {
        if (children != null) {
            int maxDepth = 0;
            for (ImageQuadTree child : children) {
                int thisDepth = child.getDepth();
                maxDepth = thisDepth > maxDepth ? thisDepth : maxDepth;
            }
            return 1 + maxDepth;
        }
        return 1;
    }

}
