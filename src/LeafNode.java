import java.util.Arrays;

/*
 * Leaf Node is child class of Node class
 * This is the node which contains actual key-value pair
 * There are no children to this node
 * And the parent for this node will only be an Internal Node
 */
class LeafNode extends Node {
    final int maxNumPairs; // equals to m - 1 in the input of Initialize(m)
    final int minNumPairs; // equals ceil (m / 2)
    int numPairs; // no of key-value pairs
    LeafNode leftSibling; // reference to left and right sibling
    LeafNode rightSibling; // forming an doubly linked list
    KeyValuePair[] dictionary; // array containing actual key-value pairs

    // Constructor
    // Initialize with m and key-value pair
    public LeafNode(int m, KeyValuePair pair) {
        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2.0) - 1);
        this.dictionary = new KeyValuePair[m];
        this.numPairs = 0;
        this.insert(pair);
    }

    // Initialize with m, pairs and parent
    public LeafNode(int m, KeyValuePair[] pairs, InternalNode parent) {
        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2.0) - 1);
        this.dictionary = pairs;
        this.numPairs = CommonUtils.firstIndexOfNull(pairs);
        this.parent = parent;
    }

    // delete key-value pair at given index
    // decrement numPairs by 1
    public void delete(int index) {
        this.dictionary[index] = null;
        numPairs--;
    }

    // Remove key-value at the given index
    // shift left all the elements to the right of the index by 1
    public void deleteAndShiftLeft(int index) {
        int i;
        for (i = index + 1; i < this.dictionary.length; i++) {
            this.dictionary[i - 1] = this.dictionary[i];
        }
        this.dictionary[i - 1] = null;
        --numPairs;
    }

    // returns false if pair if full
    // else insert pair, increment degree by 1 and return true
    public boolean insert(KeyValuePair pair) {
        if (numPairs == maxNumPairs) {
            return false;
        } else {
            // Insert dictionary pair, increment numPairs, sort dictionary
            this.dictionary[numPairs] = pair;
            numPairs++;
            Arrays.sort(this.dictionary, 0, numPairs);
            return true;
        }
    }

    // Move all pairs to right by 1 and
    // Add the given pair to the start of the dictionary.
    public void prependPair(KeyValuePair pair) {
        int i;
        for (i = this.dictionary.length - 1; i > 0; i--) {
            this.dictionary[i] = this.dictionary[i - 1];
        }
        this.dictionary[i] = pair;
        numPairs++;
    }
}
