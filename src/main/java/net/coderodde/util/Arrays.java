package net.coderodde.util;

import java.util.Random;

public class Arrays {

    private static final boolean DEBUG = false;

    public static final <E extends Comparable<? super E>>
            void sort(E[] T, int threadCount) {
        sort(T, 0, T.length - 1, threadCount);
    }

    public static final <E extends Comparable<? super E>>
            void  sort(E[] T, int p, int r, int threadCount) {
        if (threadCount < 2) {
            threadCount = 2;
        }

        E[] buffer = T.clone();

        if (p < r) {
            int q = p + (r - p) >> 1;
            MergeSorter<E> msLeft =
                    new MergeSorter<E>(p,
                                       q,
                                       T,
                                       buffer,
                                       threadCount >> 1);
            MergeSorter<E> msRight =
                    new MergeSorter<E>(q + 1,
                                       r,
                                       T,
                                       buffer,
                                       threadCount - 1 - threadCount >> 1);
            msLeft.start();
            msRight.run();

            try {
                msLeft.join();
            } catch (InterruptedException ie) {
                ie.printStackTrace(System.err);
            }

            merge(buffer, T, p, q, r);
        }
    }

    /**
     * Merges serially the subarrays <code>source[p..q]</code> and
     * <code>source[(q+1)..rr] and stores the result in
     * <code>target[p..rr]</code>.
     * @param <E> the element type.
     * @param source the source array.
     * @param target the target array.
     * @param p the index of the first element from the left subarray.
     * @param q the index of the last element from the left subarray.
     * @param rr the index of the last element from the right subarray.
     */
    static <E extends Comparable<? super E>> void merge(E[] source,
                                                        E[] target,
                                                        int p,
                                                        int q,
                                                        int rr) {
        int l = p;
        int i = p;
        int r = q + 1;

        final int LLIMIT = r;
        final int RLIMIT = rr + 1;

        while (l < LLIMIT && r < RLIMIT) {
            target[i++] = (source[r].compareTo(source[l]) < 0) ?
                    source[r++] :
                    source[l++];
        }

        while (l < LLIMIT) { target[i++] = source[l++]; }
        while (r < RLIMIT) { target[i++] = source[r++]; }
    }

    /**
     * This class encapsulates the sorting thread logic.
     * @param <E> the element type.
     */
    static final class MergeSorter<E extends Comparable<? super E>>
        extends Thread {

        int from;
        int to;
        E[] source;
        E[] target;
        int spawnPermits;

        /**
         * Constructs new sorting thread.
         * @param from the first index of the array to sort.
         * @param to the last index of the array to sort.
         * @param source the source thread.
         * @param target the target thread.
         * @param spawnPermits the amount of threads to spawn in the deeper
         * recursion level.
         */
        MergeSorter(final int from,
                    final int to,
                    final E[] source,
                    final E[] target,
                    final int spawnPermits) {
            this.from = from;
            this.to = to;
            this.source = source;
            this.target = target;
            this.spawnPermits = spawnPermits;
        }

        @Override
        public void run() {
            if (from < to) {
                int mid = from + (to - from) >> 1;
                if (spawnPermits > 1) {
                    MergeSorter<E> leftThread =
                            new MergeSorter<E>(from,
                                               mid,
                                               target,
                                               source,
                                               spawnPermits >> 1);

                    MergeSorter<E> rightThread =
                            new MergeSorter<E>(mid + 1,
                                               to,
                                               target,
                                               source,
                                               spawnPermits - spawnPermits >> 1
                                               );

                    leftThread.start();

                    rightThread.run();

                    try {
                        leftThread.join();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace(System.err);
                    }

                    merge(source, target, from, mid, to);
                } else {
                    // Sort both in the current thread.
                    sort(target, source, from, mid);
                    sort(target, source, mid + 1, to);

                    int l = from;
                    int i = from;
                    int r = mid + 1;

                    final int LLIMIT = r;
                    final int RLIMIT = to + 1;

                    while (l < LLIMIT && r < RLIMIT) {
                        target[i++] = (source[r].compareTo(source[l]) < 0) ?
                                source[r++] :
                                source[l++];
                    }

                    while (l < LLIMIT) {
                        target[i++] = source[l++];
                    }

                    while (r < RLIMIT) {
                        target[i++] = source[r++];
                    }
                }
            }
        }
    }

    static final <E extends Comparable<? super E>> void sort(
            E[] source,
            E[] target,
            int from,
            int to) {
        if (from < to) {
//            System.out.println("[" + from + ", " + to + "]");
            int mid = from + (to - from) / 2;
            sort(target, source, from, mid);
            sort(target, source, mid + 1, to);
            merge(source, target, from, mid, to);
        }
    }

    static final <E extends Comparable<? super E>> int binarySearch(E x,
                                                                    E[] T,
                                                                    int p,
                                                                    int r) {
        int low = p;
        int high = Math.max(p, r + 1);
        while (low < high) {
            int mid = low + (high - low) >> 1;
            if (x.compareTo(T[mid]) <= 0) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return high;
    }

    public final static <E extends Comparable<? super E>>
            boolean isSorted(E[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i].compareTo(array[i + 1]) > 0) {
                return false;
            }
        }

        return true;
    }

    public final static Integer[] getRandomArray(final int size,
                                                 final int limit,
                                                 final Random random) {
        Integer[] array = new Integer[size];

        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(limit);
        }

        return array;
    }

    public static <E> boolean arraysEqual(E[]... arrays) {
        for (int i = 0; i < arrays.length - 1; i++) {
            if (arrays[i].length != arrays[i + 1].length) {
                return false;
            }
        }

        for (int i = 0; i < arrays[0].length; i++) {
            for (int j = 0; j < arrays.length - 1; j++) {
                if (arrays[j][i] != arrays[j + 1][i]) {
                    return false;
                }
            }
        }

        return true;
    }

    public static void println(Object[] array) {
        for (Object o : array) {
            System.out.print(o + " ");
        }
        System.out.println();
    }

    public static void main( String[] args ) {
        final int N = 1000000;
        final Random r = new Random(321L);

        Integer[] array1 = getRandomArray(N, 10000, r);
        Integer[] array2 = array1.clone();

        if (DEBUG) {
            System.out.print("Before: ");
            println(array2);
        }

        System.out.println("Initially equals: " + arraysEqual(array1, array2));

        long ta = System.currentTimeMillis();
        java.util.Arrays.sort(array1);
        long tb = System.currentTimeMillis();

        System.out.println("java.util.Arrays.sort() in " + (tb - ta) + " ms, "
                           + "sorted: " + isSorted(array1) + ".");

        ta = System.currentTimeMillis();
        sort(array2, 8);
        tb = System.currentTimeMillis();

        System.out.println("net.coderodde.util.Arrays.sort() in "
                           + (tb - ta) + " ms, sorted: " + isSorted(array2)
                           + ".");

        System.out.println("Arrays equal: " + arraysEqual(array1, array2));

        if (DEBUG) {
            System.out.print("After:  ");
            println(array2);
        }
    }
}
