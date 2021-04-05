import java.util.Arrays;
import java.util.Comparator;

public class Helper {
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
        Comparator<KeyValuePair> c = (o1, o2) -> {
            Integer a = o1.key;
            Integer b = o2.key;
            return a.compareTo(b);
        };
        return Arrays.binarySearch(pair, 0, numPairs, new KeyValuePair(t, 0), c);
    }
}
