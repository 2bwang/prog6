package assignment;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BoundedVolumeHierarchy implements BVH{

    private SplitMethod splitMethod;
    private AVLNode root;
    private int size;

    //every node has 2 avlnode children or is a leaf node (is a shape) and has 2 null children
    private static class AVLNode {
        private int height;
        private AVLNode left;
        private AVLNode right;

        private Point min;
        private Point max;
        private Shape shape;

        //node constructor
        public AVLNode(AVLNode left, AVLNode right) {
            this.left = left;
            this.right = right;
            this.shape = null;
            this.height = 0;
            update();
        }

        //leaf constructor
        public AVLNode(Point min, Point max, Shape shape) {
            this.min = min;
            this.max = max;
            this.shape = shape;
            this.left = null;
            this.right = null;
            this.height = 0;
        }

        public boolean isLeaf() {return left == null && right == null;}

        // updates height and min/max based on children when a new node is inserted
        public void update() {

            //shouldn't run for leaf nodes
            if (isLeaf()) return;

            //remaining nodes should all have 2 children
            this.min = new Point(Math.min(left.getMin().x, right.getMin().x), Math.min(left.getMin().y, right.getMin().y));
            this.max = new Point(Math.max(left.getMax().x, right.getMax().x), Math.max(left.getMax().y, right.getMax().y));

            height = Math.max(left.getHeight(), right.getHeight()) + 1;
        }

        public Point getMin() {return min;}

        public Point getMax() {return max;}

        public int getHeight() {return height;}

        public AVLNode getLeft() {return left;}

        public AVLNode getRight() {return right;}

        public Shape getShape() {return shape;}
    }

    // helpers
    private AVLNode insertHelper(AVLNode node, Shape shape) {
        //insert at current position if at child of leaf node
        if (node == null) {
            size ++;
            return new AVLNode(shape.getMinSurroundingPoint(), shape.getMaxSurroundingPoint(), shape);
        }

        //need to compare
        return null;
    }


    @Override
    public void setSplitMethod(SplitMethod splitMethod) {
        // toDo
        if (splitMethod == null) { throw new IllegalArgumentException("Split method cannot be null"); }
        if (!(splitMethod instanceof SplitMethod)) { throw new IllegalArgumentException("Invalid split method"); }
        this.splitMethod = splitMethod;
    }

    @Override
    public void buildBVH(List<Shape> shapeList) {
        // toDo

        if (shapeList == null || shapeList.isEmpty()) {
            throw new IllegalArgumentException("Shape list cannot be null or empty");
        }

        //calls recursive buildHelper that splits the shape list into left and right depending on splitmethod

        root = buildHelper(shapeList, 0);
        size = shapeList.size();
    }

    private AVLNode buildHelper(List<Shape> shapeList, int depth) {
        //base case: if list is empty, return null
        if (shapeList.isEmpty()) {
            throw new IllegalStateException("split produced an empty side");
        }
        //if list has only one shape, return a leaf node
        if (shapeList.size() == 1) {
            Shape shape = shapeList.get(0);
            return new AVLNode(shape.getMinSurroundingPoint(), shape.getMaxSurroundingPoint(), shape);
        }

        //otherwise, split the list into left and right based on the split method
        List<Shape> leftList = new ArrayList<>();
        List<Shape> rightList = new ArrayList<>();
        
        //calls another helper method for the actual splitting
        split(shapeList, leftList, rightList, depth % 2 == 0);

        AVLNode left = buildHelper(leftList, depth + 1);
        AVLNode right = buildHelper(rightList, depth + 1);

        return new AVLNode(left, right);
    }

    private void split(List<Shape> shapeList, List<Shape> leftList, List<Shape> rightList, boolean byX) {
        //modifies the leftList and rightList based on the split method
        //median splits by median coordinates of all shape midpoints
        //midrange splits by avg of min and max coordinates of all objects in the space
        //byX determines whether to split along the x-axis or y-axis

        ArrayList<Shape> sortedList = new ArrayList<>(shapeList);
        if (byX) {
            sortedList.sort((a, b) -> Double.compare(a.getCenter().x, b.getCenter().x));
        } else {
            sortedList.sort((a, b) -> Double.compare(a.getCenter().y, b.getCenter().y));
        }

        if (splitMethod != SplitMethod.SPLIT_MIDRANGE) {
            // implement median split logic here
            int mid = sortedList.size() / 2;
            leftList.addAll(sortedList.subList(0, mid));
            rightList.addAll(sortedList.subList(mid, sortedList.size()));

        } else {
            // implement midrange split logic here
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (Shape shape : shapeList) {
                min = Math.min(min, byX ? (int)(shape.getMinSurroundingPoint().x) : (int)(shape.getMinSurroundingPoint().y));
                max = Math.max(max, byX ? (int)(shape.getMaxSurroundingPoint().x) : (int)(shape.getMaxSurroundingPoint().y));
            }
            double midrange = (min + max)/2.0; //no need to divide by 2 for comparison purposes

            //split by midrange, right side starts at splitindex
            //if there are multiple shapes on the midrange, split depending on if the midrange is less than or greater than half of the list size
            int splitIndex = 0;
            while (splitIndex < sortedList.size() && (byX ? (sortedList.get(splitIndex).getCenter().x) 
                : (sortedList.get(splitIndex).getCenter().y)) < midrange) {
                splitIndex++;
            } 

            while (splitIndex < sortedList.size() && (byX ? (sortedList.get(splitIndex).getCenter().x) 
                : (sortedList.get(splitIndex).getCenter().y)) == midrange) {
                if (splitIndex >= (sortedList.size() / 2)) break;
                splitIndex++;
            }

            //edge case: one side empty, inc/dec splitindex by one (from the end)
            if (splitIndex == 0) splitIndex = 1;
            else if (splitIndex == sortedList.size()) splitIndex = sortedList.size() - 1;

            leftList.addAll(sortedList.subList(0, splitIndex));
            rightList.addAll(sortedList.subList(splitIndex, sortedList.size()));    
        }

        //edge cases: centers exactly on split line, one side ending up empty

    }


    @Override
    public void insert(Shape shape) {
        // toDo
        //call recursive insert helper method (like in the example)
        root = insertHelper(root, shape);
    }

    @Override
    public void remove(Shape shape) {
        // toDo
    }

    @Override
    public Set<Shape> findCollision(Point2D.Double point) {
        // toDo
        return null;
    }

    @Override
    public Shape intersectRay(Point2D.Double origin, Vector2D direction) {
        // toDo
        return null;
    }

    @Override 
    public String toString() {
        if (root == null) return "Empty tree";

        StringBuilder sb = new StringBuilder();
        toStringHelper(root, sb, 0);
        return sb.toString();
    }

    private void toStringHelper(AVLNode node, StringBuilder sb, int depth) {
        if (node == null) return;

        sb.append("\t".repeat(depth));

        if (node.shape != null) {
            if (node.shape instanceof Triangle) {
                Triangle t = (Triangle) node.shape;
                sb.append("(").append(t.a.x).append(", ").append(t.a.y).append(")");
                sb.append(" (").append(t.b.x).append(", ").append(t.b.y).append(")");
                sb.append(" (").append(t.c.x).append(", ").append(t.c.y).append(")");
                sb.append("\n");
            } else {
                sb.append(node.shape.toString()).append("\n");
            }
            return;
        }

        sb.append("[(").append(node.getMin().x).append(", ").append(node.getMin().y).append(")");
        sb.append(", (").append(node.getMax().x).append(", ").append(node.getMax().y).append(")]\n");

        toStringHelper(node.left, sb, depth + 1);
        toStringHelper(node.right, sb, depth + 1);
    }
}