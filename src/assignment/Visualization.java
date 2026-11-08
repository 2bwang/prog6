package assignment;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.LinkedList;

public class Visualization {
    BoundedVolumeHierarchy boundingVolumeHierarchy;
    int sX; //square x and y
    int sY;

    /**
     * Initialize state of square to be at (0, 0)
     */
    public Visualization () {
        //build with some default triangles
        Triangle t1 = new Triangle(new java.awt.Point(0, 100), new java.awt.Point(100, 50), new java.awt.Point(50, 50));
        Triangle t2 = new Triangle(new java.awt.Point(-150, 50), new java.awt.Point(-200, -120), new java.awt.Point(-60, -30));
        Triangle t3 = new Triangle(new java.awt.Point(225, -150), new java.awt.Point(180, -140), new java.awt.Point(120, -60));
        Triangle t4 = new Triangle(new java.awt.Point(-140, 80), new java.awt.Point(-40, 120), new java.awt.Point(-140, 160));
        boundingVolumeHierarchy = new BoundedVolumeHierarchy();
        boundingVolumeHierarchy.buildBVH(java.util.Arrays.asList(t1, t2, t3, t4));

        sX = 0;
        sY = 0;
    }

    /**
     * @param boundingVolumeHierarchy        the BVH to set the boundedVolumeHierarchy to
     */
    public void setBoundingVolumeHierarchy(BoundedVolumeHierarchy boundingVolumeHierarchy) {
        this.boundingVolumeHierarchy = boundingVolumeHierarchy;
    }

    /**
     * Moves the square within the environment if it's able to move in the given direction
     *
     * @param keyPressed        the character of the keyPressed
     * @return              if the environment should be redrawn or not
     */
    public boolean moveSquare (char keyPressed) {
        //needs to check if the new position of the square collides with any shapes in the BVH before moving it

        switch(keyPressed) {
            case 'w':
                if (!canMove(sX, sY-5)) return false;
                sY-=5;
                break;
            case 's':
                if (!canMove(sX, sY+5)) return false;
                sY+=5;
                break;
            case 'a':
                if (!canMove(sX-5, sY)) return false;
                sX-=5;
                break;
            case 'd':
                if (!canMove(sX+5, sY)) return false;
                sX+=5;
                break;
            default:
                return false;
        }


        return true;
    }

    private boolean canMove(int newX, int newY) {
        if (boundingVolumeHierarchy == null) return true;
        //check if the new position of the square collides with any shapes in the BVH
        //checks every point in the new square positoin
        //kinda slow but should work
        for (int x = newX-10; x <= newX+10; x++) {
            for (int y = newY-10; y <= newY+10; y++) {
                if (!boundingVolumeHierarchy.findCollision(new Point2D.Double(x, y)).isEmpty()) return false;
            }
        }
        return true;
    }

    /**
     * Calculates all the pixels visible from the square in the center
     *
     * @return              All points that should be drawn other than the square
     */
    public LinkedList<Point> drawGUI() {
        //store each point on the border (400,400)
        LinkedList<Point> aimPoints = new LinkedList<>();
        for (int x = 0; x <= 400; x++) {
            aimPoints.add(new Point(x, 0));
            aimPoints.add(new Point(x, 400));
        }
        for (int y = 0; y <= 400; y++) {
            aimPoints.add(new Point(0, y));
            aimPoints.add(new Point(400, y));
        }

        Point2D.Double center = new Point2D.Double(sX, sY); //center of the square
        Point centerPoint = new Point(sX, sY);

        LinkedList<Point> hitPoints = new LinkedList<>();
        //draw rays to each of these points
        for (Point aimPoint : aimPoints) {
            Vector2D direction = new Vector2D(aimPoint.x - 200, aimPoint.y - 200);
            Shape hitShape = boundingVolumeHierarchy.intersectRay(center, direction);
            if (hitShape == null) continue;


            //if it hist a shape and its on screen, save the point it hit the shape at
            Point2D.Double hitPoint = hitShape.findIntersection(centerPoint, direction);
            if (hitPoint != null) {
                //convert that point to gui coords (+200 - square coord)
                int guiX = (int)hitPoint.x + 200 - sX;
                int guiY = (int)hitPoint.y + 200 - sY;
                 if (guiX >= 0 && guiX <= 400 && guiY >= 0 && guiY <= 400)hitPoints.add(new Point(guiX, guiY));
            }
        }

        return hitPoints;
    }
}
