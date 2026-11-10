package assignment;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoundedVolumeHierarchy implements BVH{

    private SplitMethod splitMethod;
    private AVLNode root;
    private int size;
    private boolean removed;

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

        public void setLeft(AVLNode left) {this.left = left; update();}

        public void setRight(AVLNode right) {this.right = right; update();}
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
        if (shape == null) throw new IllegalArgumentException("Shape cannot be null");

        if (root == null) {
            root = new AVLNode(shape.getMinSurroundingPoint(), shape.getMaxSurroundingPoint(), shape);
        }
        //call recursive insert helper method (like in the example)
        else root = insertHelper(root, shape);
        size ++;
    }

        // helpers
    private AVLNode insertHelper(AVLNode node, Shape shape) {
        //if reached a leaf node, create new internal node above it with that leaf and a new leaf for the new shape
        if (node.isLeaf()) {
            return new AVLNode(node, new AVLNode(shape.getMinSurroundingPoint(), shape.getMaxSurroundingPoint(), shape));
        }

        //if one child is a leaf and one isnt, call recursion on that to balance the tree
        if (node.getLeft().isLeaf() && !node.getRight().isLeaf()) {
            node.setLeft(insertHelper(node.getLeft(), shape));
            return balance(node);
        } else if (!node.getLeft().isLeaf() && node.getRight().isLeaf()) {
            node.setRight(insertHelper(node.getRight(), shape));
            return balance(node);
        }
        
        //otherwise, pick the child whose bounding box changes the least when adding the new shape
        //multiply the width and height of the bounding boxes to get the area
        int leftBefore = area(node.getLeft().getMin(), node.getLeft().getMax());
        int rightBefore = area(node.getRight().getMin(), node.getRight().getMax());

        //calculate points after adding the shape for min and max using the current node's bounding box and the new shape's bounding box
        Point leftMinAfter = new Point(Math.min(node.getLeft().getMin().x, (int)(shape.getMinSurroundingPoint().getX())), Math.min(node.getLeft().getMin().y, (int)(shape.getMinSurroundingPoint().getY())));
        Point leftMaxAfter = new Point(Math.max(node.getLeft().getMax().x, (int)(shape.getMaxSurroundingPoint().getX())), Math.max(node.getLeft().getMax().y, (int)(shape.getMaxSurroundingPoint().getY())));
        Point rightMinAfter = new Point(Math.min(node.getRight().getMin().x, (int)(shape.getMinSurroundingPoint().getX())), Math.min(node.getRight().getMin().y, (int)(shape.getMinSurroundingPoint().getY())));
        Point rightMaxAfter = new Point(Math.max(node.getRight().getMax().x, (int)(shape.getMaxSurroundingPoint().getX())), Math.max(node.getRight().getMax().y, (int)(shape.getMaxSurroundingPoint().getY())));

        double leftAfter = area(leftMinAfter, leftMaxAfter);
        double rightAfter = area(rightMinAfter, rightMaxAfter);
        double leftChange = leftAfter - leftBefore;
        double rightChange = rightAfter - rightBefore;

        if (leftChange < rightChange) {
            node.setLeft(insertHelper(node.getLeft(), shape));
            return balance(node);
        } else if (leftChange > rightChange) {
            node.setRight(insertHelper(node.getRight(), shape));
            return balance(node);
        }

        //if change is same, should check height
        if (node.getLeft().getHeight() < node.getRight().getHeight()) {
            node.setLeft(insertHelper(node.getLeft(), shape));
            return balance(node);
        } else if (node.getLeft().getHeight() > node.getRight().getHeight()) {
            node.setRight(insertHelper(node.getRight(), shape));
            return balance(node);
        }

        //pick randomly
        if (Math.random() < 0.5) {
            node.setLeft(insertHelper(node.getLeft(), shape));
        } else {
            node.setRight(insertHelper(node.getRight(), shape));
        }
        return balance(node);
    }

    private int area(Point min, Point max) {
        return (max.x - min.x) * (max.y - min.y);
    }

    //balances the AVL tree after an insertion or deletion
    //if any child's height is more than 1 greater than the other, perform a rotation to balance the tree
    private AVLNode balance(AVLNode node) {
        //
        if (node == null || node.isLeaf()) return node;
        int diff = node.getLeft().getHeight() - node.getRight().getHeight();

        if (diff > 1) {
            if (node.getLeft().getLeft().getHeight() >= node.getLeft().getRight().getHeight()) {
                node = rotateRight(node); // left-left
            } else { //rotate twice
                node.setLeft(rotateLeft(node.getLeft()));   // left-right
                node = rotateRight(node);
            }
        } else if (diff < -1) {
            // right-heavy (mirror)
            if (node.getRight().getRight().getHeight() >= node.getRight().getLeft().getHeight()) {
                node = rotateLeft(node);             // right-right
            } else {
                node.setRight(rotateRight(node.getRight()));  // right-left
                node = rotateLeft(node);
            }
        }

        node.update();
        return node;
    }

    //moves a child up and its parent to the right
    //ex: 
    //                            child2
    //                        /           \
    //                  child1        child2.getright
    //             /           \
    //  child1.getLeft         child1.getRight
    //
    // becomes:
    //                              child1
    //                        /                \
    //                 child1.getLeft           child2 
    //                                      /            \ 
    //                                child1.getRight     child2.getRight
    //
    //rotating once doesn't change imbalance (just flips it), so have to rotate twice

    private AVLNode rotateRight(AVLNode child2) {
        AVLNode child1 = child2.getLeft();
        child2.setLeft(child1.getRight()); //these call update() internally, hieght gets updateed
        child1.setRight(child2);
        return child1;
    }

    //same but opposite for rotateLeft
    private AVLNode rotateLeft(AVLNode child2) {
        AVLNode child1 = child2.getRight();
        child2.setRight(child1.getLeft());
        child1.setLeft(child2);
        return child1;
    }

    @Override
    public void remove(Shape shape) {
        if (shape == null) throw new IllegalArgumentException("Shape cannot be null");
        if (root == null) return;

        // flag to check if the remove occured (will trip in the helper)
        removed = false;
        root = removeHelper(root, shape);
        if (removed) size--;
    }

    //for each node, returns the node as is if not the shape, otherwise removes the subtree above the leaf and replaces it with the other child
    private AVLNode removeHelper(AVLNode node, Shape shape) {
        //base case: if not the shape, returns the leaf, otherwise retursn null (so it will return the other left/right as the node)
        if (node.isLeaf()) {
            if (!removed && node.shape.equals(shape)) {
                removed = true;
                return null;
            }
            return node;
        }

        Point shapeMin = shape.getMinSurroundingPoint();
        Point shapeMax = shape.getMaxSurroundingPoint();

        //if the shape is not in the box, retursn the node unchanged
        if (shapeMax.x < node.getMin().x || shapeMin.x > node.getMax().x || shapeMax.y < node.getMin().y || shapeMin.y > node.getMax().y) return node;

        AVLNode left = removeHelper(node.getLeft(), shape);
        //cannot be leaf (first if checks for that), so if left child is null, it must have been the shape removed
        //replace the node itself with the right child
        if (left == null) return node.getRight();

        //if the shape has already been removed, skip removing the right child
        AVLNode right = removed ? node.getRight() : removeHelper(node.getRight(), shape);
        if (right == null) return node.getLeft();

        node.setLeft(left);
        node.setRight(right);
        return balance(node);
    }


    @Override
    public Set<Shape> findCollision(Point2D.Double point) {
        //recursive traversal, checks bounding boxes of each node to narrow down
        if (point == null) throw new IllegalArgumentException("Point cannot be null");
        
        HashSet<Shape> collisions = new HashSet<>();
        collisionsHelper(root, point, collisions);
        
        return collisions;
    }

    private void collisionsHelper(AVLNode node, Point2D.Double point, HashSet<Shape> collisions) {
        if (node == null) return;

        //check bounding box of node
        if (node.getMin().x <= point.x && point.x <= node.getMax().x && node.getMin().y <= point.y && point.y <= node.getMax().y) {
            //base case: if node is a leaf, check if the shape collides with the point
            if (node.isLeaf()) {
                if (node.shape.containsPoint(point)) collisions.add(node.shape);
                return;
            }

            collisionsHelper(node.getLeft(), point, collisions);
            collisionsHelper(node.getRight(), point, collisions);
        }
    }

    @Override
    public Shape intersectRay(Point2D.Double origin, Vector2D direction) {
        if (origin == null || direction == null) throw new IllegalArgumentException("Origin and direction cannot be null");

        Point originPoint = new Point((int)(origin.x), (int)(origin.y));
        return intersectRayHelper(root, origin, direction);
    }

    private Shape intersectRayHelper(AVLNode node, Point2D.Double origin, Vector2D direction) {
        if (node == null) return null;

        //check if ray falls in the bounding box of the current node, only continues if it does
        Rectangle cur = new Rectangle(node.getMin(), node.getMax());
        Point originPoint = new Point((int)(origin.x), (int)(origin.y));

        if (cur.doesRayIntersect(originPoint, direction)) {
            if (node.isLeaf()) {        
                if (node.shape.findIntersection(originPoint, direction) != null) {
                    return node.shape;
                } else return null;
            }

            Shape left = intersectRayHelper(node.getLeft(), origin, direction);
            Shape right = intersectRayHelper(node.getRight(), origin, direction);

            if (left != null && right != null) {
                return BVH.distanceBetweenPoints(origin, left.findIntersection(originPoint, direction)) < BVH.distanceBetweenPoints(origin, right.findIntersection(originPoint, direction)) ? left : right;
            } else if (left != null) {
                return left;
            } else if (right != null) {
                return right;
            }
            return null;
        }

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
                sb.append(node.shape.toString().stripTrailing()).append("\n"); //some shapes might have trailing spaces
            }
            return;
        }

        sb.append("[(").append(node.getMin().x).append(", ").append(node.getMin().y).append(")");
        sb.append(", (").append(node.getMax().x).append(", ").append(node.getMax().y).append(")]\n");

        toStringHelper(node.left, sb, depth + 1);
        toStringHelper(node.right, sb, depth + 1);
    }
}