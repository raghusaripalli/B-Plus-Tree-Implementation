public class KeyValuePair implements Comparable<KeyValuePair> {
    int key;
    double value;

    public KeyValuePair(int key, double value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(KeyValuePair o) {
        return Integer.compare(key, o.key);
    }
}
