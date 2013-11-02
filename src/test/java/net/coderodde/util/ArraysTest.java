package net.coderodde.util;

import java.util.Random;
import org.junit.Test;
import static net.coderodde.util.Arrays.*;
import static org.junit.Assert.*;

public class ArraysTest {

    @Test
    public void testCorrectness() {
        final int N = 1000000;
        final long SEED = System.currentTimeMillis();
        final Random r = new Random(SEED);

        System.out.println("Seed: " + SEED);

        Integer[] array1 = getRandomArray(N, 10000, r);
        Integer[] array2 = array1.clone();

        assertTrue(arraysEqual(array1, array2));

        long ta = System.currentTimeMillis();
        java.util.Arrays.sort(array1);
        long tb = System.currentTimeMillis();

        System.out.println("java.util.Arrays.sort() in " + (tb - ta) + " ms, "
                           + "sorted: " + isSorted(array1) + ".");
        assertTrue(isSorted(array1));
        ta = System.currentTimeMillis();
        sort(array2, 8);
        tb = System.currentTimeMillis();

        System.out.println("net.coderodde.util.Arrays.sort() in "
                           + (tb - ta) + " ms, sorted: " + isSorted(array2)
                           + ".");

        System.out.println("Arrays equal: " + arraysEqual(array1, array2));

        assertTrue(isSorted(array2));
        assertTrue(arraysEqual(array1, array2));
    }
}
