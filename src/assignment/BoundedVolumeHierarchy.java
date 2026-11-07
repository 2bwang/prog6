package assignment;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

public class BoundedVolumeHierarchy implements BVH{

    private SplitMethod splitMethod;
    private BoundedVolumeHierarchy left;
    private BoundedVolumeHierarchy right;

    private Point min;
    private Point max;
    private Shape triangle;

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
        //set splitmethod, set left/right to null
        //for each shape, store min and max of its bounding box
        //find split (max of left tree, min of right tree, can be different)
        //assign each shape to left or right list based on the split value

        if (shapeList == null || shapeList.isEmpty()) {
            throw new IllegalArgumentException("Shape list cannot be null or empty");
        }

        this.left = null;
        this.right = null;
        this.splitMethod = SPLIT_DEFAULT;

        //actually nvm just implement the avltree they posted in ed
        //each node should also store a shape (null unless its a leaf) and a min/max point
        //left and right children aren't necessarily left and right locationally, they are just smaller tighter boxes around some shapes
        //min of parent is min of its children's min points, max of parent is max of its children's max points
    }

    @Override
    public void insert(Shape shape) {
        // toDo
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
}