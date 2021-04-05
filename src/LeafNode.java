import java.util.Arrays;

public class LeafNode extends Node {
    public int maxNumPairs;
    public int minNumPairs;
    public int numPairs;
    public LeafNode leftSibling;
    public LeafNode rightSibling;
    public KeyValuePair[] dictionary;

    public LeafNode(int m, KeyValuePair dp) {

        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2.0) - 1);
        this.dictionary = new KeyValuePair[m];
        this.numPairs = 0;
        this.insert(dp);
    }


    public LeafNode(int m, KeyValuePair[] pairs, InternalNode parent) {
        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2.0) - 1);
        this.dictionary = pairs;
        this.numPairs = Helper.firstIndexOfNull(pairs);
        this.parent = parent;
    }

    public void delete(int index) {
        this.dictionary[index] = null;
        numPairs--;
    }

    public void deleteAndShiftLeft(int index) {
        int i;
        for (i = index + 1; i < this.dictionary.length; i++) {
            this.dictionary[i - 1] = this.dictionary[i];
        }
        this.dictionary[i - 1] = null;
        --numPairs;
    }

    public boolean insert(KeyValuePair pair) {
        if (this.isFull()) {
            return false;
        } else {
            // Insert dictionary pair, increment numPairs, sort dictionary
            this.dictionary[numPairs] = pair;
            numPairs++;
            Arrays.sort(this.dictionary, 0, numPairs);
            return true;
        }
    }

    public void prepend(KeyValuePair pair) {
        int i;
        for (i = this.dictionary.length - 1; i > 0; i--) {
            this.dictionary[i] = this.dictionary[i - 1];
        }
        this.dictionary[i] = pair;
        numPairs++;
    }

    public boolean isDeficient() {
        return numPairs < minNumPairs;
    }


    public boolean isFull() {
        return numPairs == maxNumPairs;
    }


    public boolean canLend() {
        return numPairs > minNumPairs;
    }

    public boolean isMergeable() {
        return numPairs == minNumPairs;
    }
}
