package assignment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/* 
 * Any comments and methods here are purely descriptions or suggestions.
 * This is your test file. Feel free to change this as much as you want.
 */

public class BVHTest {

    // This will run ONCE before all other tests. It can be useful to setup up
    // global variables and anything needed for all of the tests.
    @BeforeAll
    static void setupAll() {

    }

    // This will run before EACH test.
    @BeforeEach
    void setupEach() {

    }

    // You can test your BVH here
    @Test
    void testBVH() {

    }

    // You can test your Visualization here
    @Test
    void testVisualization() {
        
    }

    @Test 
    void testToString() {
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(
            new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2)),
            new Triangle(new java.awt.Point(1, 5), new java.awt.Point(2, 3), new java.awt.Point(3, 5)),
            new Triangle(new java.awt.Point(2, 2), new java.awt.Point(3, 1), new java.awt.Point(4, 2)),
            new Triangle(new java.awt.Point(4, 5), new java.awt.Point(5, 4), new java.awt.Point(5, 5))));

        String expected =
            "[(0, 0), (5, 5)]\n" +
            "\t[(0, 0), (3, 5)]\n" +
            "\t\t(0, 0) (1, 0) (1, 2)\n" +
            "\t\t(1, 5) (2, 3) (3, 5)\n" +
            "\t[(2, 1), (5, 5)]\n" +
            "\t\t(2, 2) (3, 1) (4, 2)\n" +
            "\t\t(4, 5) (5, 4) (5, 5)";

        assertEquals(expected, bvh.toString().stripTrailing());
    }

    @Test
    void testIntersectRay() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2));
        Triangle t2 = new Triangle(new java.awt.Point(1, 5), new java.awt.Point(2, 3), new java.awt.Point(3, 5));
        Triangle t3 = new Triangle(new java.awt.Point(2, 2), new java.awt.Point(3, 1), new java.awt.Point(4, 2));
        Triangle t4 = new Triangle(new java.awt.Point(4, 5), new java.awt.Point(5, 4), new java.awt.Point(5, 5));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2, t3, t4));

        assertEquals(t1, bvh.intersectRay(new java.awt.geom.Point2D.Double(-1, 1), new Vector2D(1, 0)), "ray right along y=1");
        assertEquals(t3, bvh.intersectRay(new java.awt.geom.Point2D.Double(6, 1), new Vector2D(-1, 0)), "ray left along y=1 should hit nearer t3, not t1");
        assertNull(bvh.intersectRay(new java.awt.geom.Point2D.Double(0, 10), new Vector2D(1, 0)), "ray below everything");
    }

    @Test
    void testInsertStaysBalanced() {
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        for (int i = 0; i < 8; i++) {
            int x = i * 10;
            bvh.insert(new Triangle(new java.awt.Point(x, 0), new java.awt.Point(x + 2, 0), new java.awt.Point(x + 1, 2)));
        }
        System.out.println(bvh);   // eyeball it

        // a balanced tree of 8 leaves has max depth 3 or 4, so no line should have more than 4 tabs
        for (String line : bvh.toString().split("\n")) {
            int tabs = line.length() - line.replaceAll("^\t+", "").length();
            assertTrue(tabs <= 4, "tree too deep, line: " + line);
        }
        // every inserted triangle should still be findable
        for (int i = 0; i < 8; i++) {
            assertEquals(1, bvh.findCollision(new java.awt.geom.Point2D.Double(i * 10 + 1, 1)).size(), "lost triangle " + i);
        }
    }

    @Test
    void testRemove() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2));
        Triangle t2 = new Triangle(new java.awt.Point(1, 5), new java.awt.Point(2, 3), new java.awt.Point(3, 5));
        Triangle t3 = new Triangle(new java.awt.Point(2, 2), new java.awt.Point(3, 1), new java.awt.Point(4, 2));
        Triangle t4 = new Triangle(new java.awt.Point(4, 5), new java.awt.Point(5, 4), new java.awt.Point(5, 5));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2, t3, t4));

        bvh.remove(t3);
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(3, 1.5)).isEmpty(), "t3 should be gone");
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(0.9, 0.5)).contains(t1), "t1 should remain");

        // sibling t4 replaced t3's parent, so the right box shrinks to t4's box
        assertTrue(bvh.toString().contains("[(0, 0), (5, 5)]"), bvh.toString());
        assertTrue(bvh.toString().contains("\t(4, 5) (5, 4) (5, 5)"), "t4 should move up a level:\n" + bvh);

        bvh.remove(t1); bvh.remove(t2); bvh.remove(t4);
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(4.9, 4.9)).isEmpty(), "tree should be empty");

        bvh.remove(t1);   // removing from an empty tree should not throw
    }


}
