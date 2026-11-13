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

    //builds the figure in the spec, tests if it matches
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

    //checks if rays return closest intersection correctly
    @Test
    void testIntersectRay() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2));
        Triangle t2 = new Triangle(new java.awt.Point(1, 5), new java.awt.Point(2, 3), new java.awt.Point(3, 5));
        Triangle t3 = new Triangle(new java.awt.Point(2, 2), new java.awt.Point(3, 1), new java.awt.Point(4, 2));
        Triangle t4 = new Triangle(new java.awt.Point(4, 5), new java.awt.Point(5, 4), new java.awt.Point(5, 5));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2, t3, t4));

        assertEquals(t1, bvh.intersectRay(new java.awt.geom.Point2D.Double(-1, 1), new Vector2D(1, 0))); // ray right along y=1
        assertEquals(t3, bvh.intersectRay(new java.awt.geom.Point2D.Double(6, 1), new Vector2D(-1, 0))); // ray left should hit nearer t3, not t1
        assertNull(bvh.intersectRay(new java.awt.geom.Point2D.Double(0, 10), new Vector2D(1, 0))); // ray below everything
    }

    //checks if inserting multiple triangles keeps the tree balanced
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
            assertTrue(tabs <= 4);
        }
        // every inserted triangle should still be findable
        for (int i = 0; i < 8; i++) {
            assertEquals(1, bvh.findCollision(new java.awt.geom.Point2D.Double(i * 10 + 1, 1)).size());
        }
    }

    //checks that removed triangle disappears and updates tree structure
    @Test
    void testRemove() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2));
        Triangle t2 = new Triangle(new java.awt.Point(1, 5), new java.awt.Point(2, 3), new java.awt.Point(3, 5));
        Triangle t3 = new Triangle(new java.awt.Point(2, 2), new java.awt.Point(3, 1), new java.awt.Point(4, 2));
        Triangle t4 = new Triangle(new java.awt.Point(4, 5), new java.awt.Point(5, 4), new java.awt.Point(5, 5));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2, t3, t4));

        bvh.remove(t3);
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(3, 1.5)).isEmpty()); // t3 should be gone
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(0.9, 0.5)).contains(t1)); // t1 should remain

        // sibling t4 replaced t3's parent, so the right box shrinks to t4's box
        assertTrue(bvh.toString().contains("[(0, 0), (5, 5)]"));
        assertTrue(bvh.toString().contains("\t(4, 5) (5, 4) (5, 5)")); // t4 moves up a level

        bvh.remove(t1); bvh.remove(t2); bvh.remove(t4);
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(4.9, 4.9)).isEmpty()); // tree should be empty

        bvh.remove(t1);   // removing from an empty tree should not throw
    }

    //overlap returns both, vertex counts, empty space returns nothing
    @Test
    void testFindCollision() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(4, 0), new java.awt.Point(0, 4));
        Triangle t2 = new Triangle(new java.awt.Point(1, 1), new java.awt.Point(5, 1), new java.awt.Point(1, 5));
        Triangle t3 = new Triangle(new java.awt.Point(10, 10), new java.awt.Point(12, 10), new java.awt.Point(10, 12));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2, t3));

        // t1 and t2 overlap at (1.5, 1.5) so both should come back
        assertEquals(Set.of(t1, t2), bvh.findCollision(new java.awt.geom.Point2D.Double(1.5, 1.5)));
        assertEquals(Set.of(t3), bvh.findCollision(new java.awt.geom.Point2D.Double(10, 10))); // vertex counts as inside
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(8, 8)).isEmpty()); // point between triangles
    }

    //checks that median and midrange split differently
    @Test
    void testSplitMethods() {
        // centroid x = 1, 2, 3, 30. median splits 2|2, midrange (15.5) splits 3|1
        List<Shape> shapes = java.util.Arrays.asList(
            new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(2, 3)),
            new Triangle(new java.awt.Point(1, 0), new java.awt.Point(2, 0), new java.awt.Point(3, 6)),
            new Triangle(new java.awt.Point(2, 0), new java.awt.Point(3, 0), new java.awt.Point(4, 9)),
            new Triangle(new java.awt.Point(29, 0), new java.awt.Point(30, 0), new java.awt.Point(31, 3)));
        BoundedVolumeHierarchy median = new BoundedVolumeHierarchy();
        median.buildBVH(shapes);
        BoundedVolumeHierarchy midrange = new BoundedVolumeHierarchy();
        midrange.setSplitMethod(BVH.SplitMethod.SPLIT_MIDRANGE);
        midrange.buildBVH(shapes);

        assertTrue(median.toString().contains("\n\t[(0, 0), (3, 6)]")); // median left child holds the first 2
        assertTrue(midrange.toString().contains("\n\t[(0, 0), (4, 9)]")); // midrange left child holds the first 3
    }

    //insert into empty tree makes one leaf
    @Test
    void testInsertEmpty() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.insert(t1);

        assertEquals("(0, 0) (1, 0) (1, 2)", bvh.toString().stripTrailing());
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(0.9, 0.5)).contains(t1));
    }

    //removing a missing triangle shouldnt change the tree
    @Test
    void testRemoveMissing() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2));
        Triangle t2 = new Triangle(new java.awt.Point(1, 5), new java.awt.Point(2, 3), new java.awt.Point(3, 5));
        Triangle t3 = new Triangle(new java.awt.Point(2, 2), new java.awt.Point(3, 1), new java.awt.Point(4, 2));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2, t3));
        String before = bvh.toString();

        // fits inside the root box but isn't in the tree
        bvh.remove(new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(0, 1)));
        assertEquals(before, bvh.toString());
    }

    //only the edge facing the square is drawn, negative coords still show
    @Test
    void testDrawGUI() {
        // spec figure 3 triangle plus its mirror above-left of the square
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(
            new Triangle(new java.awt.Point(0, 100), new java.awt.Point(50, 100), new java.awt.Point(50, 50)),
            new Triangle(new java.awt.Point(0, -100), new java.awt.Point(-50, -100), new java.awt.Point(-50, -50))));
        Visualization vis = new Visualization();
        vis.setBoundingVolumeHierarchy(bvh);
        List<java.awt.Point> pixels = vis.drawGUI();

        // virtual (25, 75) -> gui (225, 275) is on the diagonal, (50, 75) -> gui (250, 275) is on the back edge
        assertTrue(drawnNear(pixels, 225, 275)); // facing edge drawn
        assertFalse(drawnNear(pixels, 250, 275)); // back edge hidden
        // virtual (-25, -75) -> gui (175, 125)
        assertTrue(drawnNear(pixels, 175, 125));
    }

    //whole square blocks movement, moving shifts the scene
    @Test
    void testMoveSquare() {
        // left edge at x = 15, square reaches x = 10, so moving right 5 would touch it
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(new Triangle(new java.awt.Point(15, -50), new java.awt.Point(15, 50), new java.awt.Point(40, 0))));
        Visualization vis = new Visualization();
        vis.setBoundingVolumeHierarchy(bvh);

        assertFalse(vis.moveSquare('d')); // blocked by the triangle
        assertFalse(vis.moveSquare('x')); // invalid key
        assertTrue(vis.moveSquare('a')); // moving away works
        // square is now at x = -5, so virtual (15, 0) shows up at gui (220, 200)
        assertTrue(drawnNear(vis.drawGUI(), 220, 200));
    }

    // true if any drawn pixel is within 1 pixel of (x, y)
    private boolean drawnNear(List<java.awt.Point> pixels, int x, int y) {
        for (java.awt.Point p : pixels) if (Math.abs(p.x - x) <= 1 && Math.abs(p.y - y) <= 1) return true;
        return false;
    }

    //equal triangle with different vertex order should still remove
    @Test
    void testRemoveEqualTriangle() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2));
        Triangle t2 = new Triangle(new java.awt.Point(1, 5), new java.awt.Point(2, 3), new java.awt.Point(3, 5));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2));

        // a different object with the same corners (in a different order) should still count as t1
        bvh.remove(new Triangle(new java.awt.Point(1, 2), new java.awt.Point(0, 0), new java.awt.Point(1, 0)));
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(0.9, 0.5)).isEmpty());
    }

    //vertical rays should hit the nearer triangle
    @Test
    void testIntersectRayVertical() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(4, 0), new java.awt.Point(2, 2));
        Triangle t2 = new Triangle(new java.awt.Point(0, 12), new java.awt.Point(4, 12), new java.awt.Point(2, 10));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.insert(t1);
        bvh.insert(t2);

        // both triangles sit on x = 2 in different subtrees, so the hit depends on which way the ray points
        assertEquals(t1, bvh.intersectRay(new java.awt.geom.Point2D.Double(2, -5), new Vector2D(0, 1))); // ray down hits t1 first
        assertEquals(t2, bvh.intersectRay(new java.awt.geom.Point2D.Double(2, 20), new Vector2D(0, -1))); // ray up hits t2 first
        assertNull(bvh.intersectRay(new java.awt.geom.Point2D.Double(10, -5), new Vector2D(0, 1))); // right of everything
    }

    //diagonal rays should hit the nearer triangle
    @Test
    void testIntersectRayDiagonal() {
        Triangle t1 = new Triangle(new java.awt.Point(4, 6), new java.awt.Point(6, 4), new java.awt.Point(8, 8));
        Triangle t2 = new Triangle(new java.awt.Point(14, 16), new java.awt.Point(16, 14), new java.awt.Point(12, 12));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2));

        // both triangles cross y = x, t1 at (5, 5) and t2 at (15, 15)
        assertEquals(t1, bvh.intersectRay(new java.awt.geom.Point2D.Double(0, 0), new Vector2D(1, 1)));
        assertEquals(t2, bvh.intersectRay(new java.awt.geom.Point2D.Double(20, 20), new Vector2D(-1, -1)));
        assertNull(bvh.intersectRay(new java.awt.geom.Point2D.Double(0, 0), new Vector2D(1, -1))); // away from both
    }

    //empty tree queries shouldnt crash
    @Test
    void testEmptyTree() {
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();

        assertNull(bvh.intersectRay(new java.awt.geom.Point2D.Double(0, 0), new Vector2D(1, 0)));
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(0, 0)).isEmpty());
        assertEquals("Empty tree", bvh.toString().stripTrailing());
    }

    //second build replaces the first
    @Test
    void testBuildTwice() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2));
        Triangle t2 = new Triangle(new java.awt.Point(1, 5), new java.awt.Point(2, 3), new java.awt.Point(3, 5));
        Triangle t3 = new Triangle(new java.awt.Point(20, 20), new java.awt.Point(22, 20), new java.awt.Point(21, 22));
        Triangle t4 = new Triangle(new java.awt.Point(30, 20), new java.awt.Point(32, 20), new java.awt.Point(31, 22));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2));
        bvh.buildBVH(java.util.Arrays.asList(t3, t4));

        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(0.9, 0.5)).isEmpty()); // old triangle gone
        assertEquals(Set.of(t3), bvh.findCollision(new java.awt.geom.Point2D.Double(21, 21))); // new triangle found
        assertTrue(bvh.toString().startsWith("[(20, 20), (32, 22)]")); // root box only covers the new ones
    }

    //remove should rotate to stay balanced, reinsert should work
    @Test
    void testRemoveRebalances() {
        List<Shape> row = new ArrayList<>();
        for (int i = 0; i < 5; i++) row.add(new Triangle(new java.awt.Point(i * 10, 0), new java.awt.Point(i * 10 + 2, 0), new java.awt.Point(i * 10 + 1, 2)));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(row);

        // median puts 2 left and 3 right, so removing a left one needs a rotation to keep 4 leaves at height 2
        bvh.remove(row.get(0));
        for (String line : bvh.toString().split("\n")) {
            int tabs = line.length() - line.replaceAll("^\t+", "").length();
            assertTrue(tabs <= 2);
        }
        // putting it back should make it findable again
        bvh.insert(row.get(0));
        for (int i = 0; i < 5; i++) {
            assertEquals(1, bvh.findCollision(new java.awt.geom.Point2D.Double(i * 10 + 1, 1)).size());
        }
    }

    //ties on the midrange line should split evenly
    @Test
    void testMidrangeTie() {
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.setSplitMethod(BVH.SplitMethod.SPLIT_MIDRANGE);
        bvh.buildBVH(java.util.Arrays.asList(
            new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(2, 3)),
            new Triangle(new java.awt.Point(4, 0), new java.awt.Point(5, 0), new java.awt.Point(6, 3)),
            new Triangle(new java.awt.Point(4, 10), new java.awt.Point(5, 10), new java.awt.Point(6, 7)),
            new Triangle(new java.awt.Point(8, 0), new java.awt.Point(9, 0), new java.awt.Point(10, 3))));

        // centroid x = 1, 5, 5, 9 and midrange = (0 + 10) / 2 = 5, so the two on the line should go one to each side
        for (String line : bvh.toString().split("\n")) {
            int tabs = line.length() - line.replaceAll("^\t+", "").length();
            if (!line.contains("[")) assertEquals(2, tabs); // every leaf at depth 2
        }
    }

    //triangle hidden behind another shouldnt be drawn
    @Test
    void testDrawGUIHidden() {
        // near triangle left of the square completely blocks the far one behind it
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(
            new Triangle(new java.awt.Point(-30, -50), new java.awt.Point(-30, 50), new java.awt.Point(-40, 0)),
            new Triangle(new java.awt.Point(-100, -10), new java.awt.Point(-100, 10), new java.awt.Point(-110, 0))));
        Visualization vis = new Visualization();
        vis.setBoundingVolumeHierarchy(bvh);
        List<java.awt.Point> pixels = vis.drawGUI();

        // virtual (-30, 0) -> gui (170, 200), and nothing should show up past the near triangle at gui x = 160
        assertTrue(drawnNear(pixels, 170, 200));
        for (java.awt.Point p : pixels) assertTrue(p.x >= 159);
    }

    //removing the far triangle should shrink the boxes above it
    @Test
    void testRemoveShrinksBox() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(2, 0), new java.awt.Point(1, 2));
        Triangle t2 = new Triangle(new java.awt.Point(4, 0), new java.awt.Point(6, 0), new java.awt.Point(5, 2));
        Triangle t3 = new Triangle(new java.awt.Point(100, 100), new java.awt.Point(102, 100), new java.awt.Point(101, 102));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2, t3));

        bvh.remove(t3);
        assertTrue(bvh.toString().startsWith("[(0, 0), (6, 2)]")); // root box no longer reaches (102, 102)
    }

    //removing one at a time shouldnt lose any of the others
    @Test
    void testRemoveOneByOne() {
        List<Shape> row = new ArrayList<>();
        for (int i = 0; i < 6; i++) row.add(new Triangle(new java.awt.Point(i * 10, 0), new java.awt.Point(i * 10 + 2, 0), new java.awt.Point(i * 10 + 1, 2)));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(row);

        // remove in a mixed order, after each one check removed ones are gone and the rest are still there
        int[] order = {3, 0, 5, 1, 4, 2};
        Set<Integer> removed = new HashSet<>();
        for (int r : order) {
            bvh.remove(row.get(r));
            removed.add(r);
            for (int i = 0; i < 6; i++) {
                assertEquals(removed.contains(i) ? 0 : 1, bvh.findCollision(new java.awt.geom.Point2D.Double(i * 10 + 1, 1)).size());
            }
        }
    }

    //tree should still work after removing everything
    @Test
    void testRemoveAllThenInsert() {
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(1, 0), new java.awt.Point(1, 2));
        Triangle t2 = new Triangle(new java.awt.Point(1, 5), new java.awt.Point(2, 3), new java.awt.Point(3, 5));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(t1, t2));

        bvh.remove(t1);
        bvh.remove(t2);
        bvh.insert(t2);
        assertEquals("(1, 5) (2, 3) (3, 5)", bvh.toString().stripTrailing()); // back to a single leaf
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(2, 4)).contains(t2));
    }

    //checks that first/last row pixels stay inside the 400x400 frame
    @Test
    void testDrawGUIScreenEdges() {
        //triangles on gui y = 399 and gui y = 0
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(new Triangle(new java.awt.Point(-1000, 199), new java.awt.Point(1000, 199), new java.awt.Point(0, 1000)), new Triangle(new java.awt.Point(-1000, -200), new java.awt.Point(1000, -200), new java.awt.Point(0, -1000))));
        Visualization vis = new Visualization();
        vis.setBoundingVolumeHierarchy(bvh);
        List<java.awt.Point> pixels = vis.drawGUI();

        for (java.awt.Point p : pixels) assertTrue(p.x >= 0 && p.x < 400 && p.y >= 0 && p.y < 400);
        assertTrue(drawnNear(pixels, 200, 399)); // bottom row
        assertTrue(drawnNear(pixels, 200, 0)); // top row
        assertTrue(drawnNear(pixels, 0, 399) && drawnNear(pixels, 399, 0)); // reaches the corners
    }

    //points exactly on an edge count, points just outside don't
    @Test
    void testFindCollisionOnEdge() {
        Triangle t = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(4, 0), new java.awt.Point(0, 4));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.insert(t);

        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(2, 0)).contains(t)); // bottom edge
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(2, 2)).contains(t)); // slanted edge
        assertTrue(bvh.findCollision(new java.awt.geom.Point2D.Double(2.01, 2.01)).isEmpty()); // just outside
    }

    //centroids all on one side of midrange
    @Test
    void testMidrangeOneSideEmpty() {
        // box midrange is x = 15, but all three centroids are past it
        Triangle t1 = new Triangle(new java.awt.Point(0, 0), new java.awt.Point(30, 0), new java.awt.Point(30, 1));
        Triangle t2 = new Triangle(new java.awt.Point(25, 0), new java.awt.Point(30, 0), new java.awt.Point(30, 1));
        Triangle t3 = new Triangle(new java.awt.Point(26, 0), new java.awt.Point(30, 0), new java.awt.Point(30, 2));
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.setSplitMethod(BVH.SplitMethod.SPLIT_MIDRANGE);
        bvh.buildBVH(java.util.Arrays.asList(t1, t2, t3));

        assertEquals(Set.of(t1, t2, t3), bvh.findCollision(new java.awt.geom.Point2D.Double(29, 0.5)));
    }

    @Test
    void testDrawGUINoGaps() {
        // top edge sits at gui y = 240, from x = 50 to 350
        BoundedVolumeHierarchy bvh = new BoundedVolumeHierarchy();
        bvh.buildBVH(java.util.Arrays.asList(new Triangle(new java.awt.Point(-150, 40), new java.awt.Point(150, 40), new java.awt.Point(0, 80))));
        Visualization vis = new Visualization();
        vis.setBoundingVolumeHierarchy(bvh);

        Set<Integer> xs = new HashSet<>();
        for (java.awt.Point p : vis.drawGUI()) if (p.y == 240) xs.add(p.x);
        for (int x = 51; x <= 349; x++) assertTrue(xs.contains(x));
    }
}
