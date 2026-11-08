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
}
