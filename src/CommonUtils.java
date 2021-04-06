import java.util.Arrays;
import java.util.Comparator;

/*
 * Helper Methods that are useful in Insert, Delete and Search APIs
 */
public class CommonUtils {
    static int m;

    // Comparator used in searching the array of keyValuePair
    private final static Comparator<KeyValuePair> search = (o1, o2) -> {
        Integer a = o1.key;
        Integer b = o2.key;
        return a.compareTo(b);
    };

    // Comparator used in sorting the array of keyValuePair
    private final static Comparator<KeyValuePair> sort = (o1, o2) -> {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        return o1.compareTo(o2);
    };

    // Return index of first null in pointers[] else -1
    public static int firstIndexOfNull(Node[] pointers) {
        for (int i = 0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    // Return index of first null in pairs[] else -1
    public static int firstIndexOfNull(KeyValuePair[] pairs) {
        for (int i = 0; i < pairs.length; i++) {
            if (pairs[i] == null) {
                return i;
            }
        }
        return -1;
    }

    // Perform binary search on key value pairs for key
    public static int binarySearch(KeyValuePair[] pair, int numPairs, int key) {
        return Arrays.binarySearch(pair, 0, numPairs, new KeyValuePair(key, 0), search);
    }

    // return index of leafNode in pointers
    public static int getIndexOfLeafNode(Node[] pointers, LeafNode leafNode) {
        int i;
        for (i = 0; i < pointers.length; i++) {
            if (pointers[i] == leafNode) {
                break;
            }
        }
        return i;
    }

    // Sort key value pairs in asc order
    public static void orderPairsInAscending(KeyValuePair[] pairs) {
        Arrays.sort(pairs, sort);
    }
}
