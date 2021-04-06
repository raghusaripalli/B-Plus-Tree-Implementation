/*
 * Internal Node child class of Node
 * It mainly contains Keys and pointers to Child Nodes (nodes can be internal nodes or leaf nodes)
 */
class InternalNode extends Node {
    final int maxDegree; // equals to m in the input of Initialize(m)
    final int minDegree; // equals ceil (m / 2)
    int degree; // no of child pointers
    InternalNode leftSibling; // reference to left and right sibling
    InternalNode rightSibling; // forming an doubly linked list
    Integer[] keys;  // indices determining the where data lies. Need not to be actual values
    Node[] children; // child Pointers from this node to internal or leaf nodes in the tree

    // Constructors
    // Initialize with m and keys
    public InternalNode(int m, Integer[] keys) {
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = 0;
        this.keys = keys;
        this.children = new Node[this.maxDegree + 1];
    }

    // Initialize with m, keys and child pointers
    public InternalNode(int m, Integer[] keys, Node[] pointers) {
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = CommonUtils.firstIndexOfNull(pointers);
        this.keys = keys;
        this.children = pointers;
    }

    // Helper Methods

    // Add given pointer to end of childPointers
    // and Increment the degree by 1
    public void appendChildPointer(Node pointer) {
        this.children[degree] = pointer;
        this.degree++;
    }

    // Right Shift all the childPointers by 1
    // Add given pointer to the first index and increment degree by 1
    public void prependChildPointer(Node pointer) {
        if (degree - 1 + 1 >= 0) System.arraycopy(children, 0, children, 1, degree - 1 + 1);
        this.children[0] = pointer;
        this.degree++;
    }

    // Find index of the given pointer in children array
    // Return index if found, else return -1.
    public int findIndexOfChildPointer(Node pointer) {
        for (int i = 0; i < children.length; i++) {
            if (children[i] == pointer) {
                return i;
            }
        }
        return -1;
    }

    // Right Shift all childPointers by 1 from index
    // Insert given pointer at the index provided
    // and Increment the degree by 1
    public void insertChildPointer(Node pointer, int index) {
        if (degree - index >= 0) System.arraycopy(children, index, children, index + 1, degree - index);
        this.children[index] = pointer;
        this.degree++;
    }


    // Move all keys to right by 1 and
    // Add the given key to the start of the keys.
    public void prependKey(int key) {
        int i;
        for (i = this.keys.length - 1; i > 0; i--) {
            this.keys[i] = this.keys[i - 1];
        }
        this.keys[0] = key;
    }

    // Remove key at the given index
    // shift left all the element to the right of the index by 1
    public void removeKeyAndShiftLeft(int index) {
        int i;
        for (i = index; i < this.keys.length - 1; i++) {
            this.keys[i] = this.keys[i + 1];
        }
        this.keys[i] = null;
    }

    // Remove child pointer at the given index
    // decrement degree by 1
    public void removePointer(int index) {
        this.children[index] = null;
        this.degree--;
    }

    // Remove child pointer at the given index
    // shift left all the element to the right of the index by 1
    // decrement degree by 1
    public void removeChildPointerAndShiftLeft(int index) {
        int i;
        for (i = index; i < this.degree - 1; i++) {
            this.children[i] = this.children[i + 1];
        }
        this.children[i] = null;
        this.degree--;
    }
}
