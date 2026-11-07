package assignment;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;

public class BoundedVolumeHierarchy implements BVH{

    private SplitMethod splitMethod;

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