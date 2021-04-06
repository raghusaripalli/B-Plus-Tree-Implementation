import java.util.Arrays;
import java.util.Comparator;

public class CommonUtils {
    static int m;

    private final static Comparator<KeyValuePair> search = (o1, o2) -> {
        Integer a = o1.key;
        Integer b = o2.key;
        return a.compareTo(b);
    };

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

    public static int firstIndexOfNull(Node[] pointers) {
        for (int i = 0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public static int firstIndexOfNull(KeyValuePair[] pairs) {
        for (int i = 0; i < pairs.length; i++) {
            if (pairs[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public static int binarySearch(KeyValuePair[] pair, int numPairs, int t) {

        return Arrays.binarySearch(pair, 0, numPairs, new KeyValuePair(t, 0), search);
    }

    public static int getIndexOfLeafNode(Node[] pointers, LeafNode node) {
        int i;
        for (i = 0; i < pointers.length; i++) {
            if (pointers[i] == node) {
                break;
            }
        }
        return i;
    }

    public static void orderPairsInAscending(KeyValuePair[] pairs) {
        Arrays.sort(pairs, sort);
    }

    public static int findMidIndex() {
        return (int) Math.ceil((m + 1) / 2.0) - 1;
    }
}
