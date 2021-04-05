/*
 * Internal Node child class of Node
 * It mainly contains Keys and pointers to Child Nodes (nodes can be internal nodes or leaf nodes)
 */
class InternalNode extends Node {

    int maxDegree; // equals to m in the input of Initialize(m)
    int minDegree; // ceil (m / 2)
    int degree; // no of child pointers
    InternalNode leftSibling; // references used while lending or borrowing
    InternalNode rightSibling;
    Integer[] keys;  // indices determining the where data lies. Need not to be actual values
    Node[] childPointers; // Points to child internal or leaf nodes in the tree

    // Constructors
    // Initialize with m and keys
    public InternalNode(int m, Integer[] keys) {
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = 0;
        this.keys = keys;
        this.childPointers = new Node[this.maxDegree + 1];
    }

    // Initialize with m, keys and child pointers
    public InternalNode(int m, Integer[] keys, Node[] pointers) {
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = Helper.firstIndexOfNull(pointers);
        this.keys = keys;
        this.childPointers = pointers;
    }

    // Add given pointer to end of childPointers
    // and Increment the degree by 1
    public void appendChildPointer(Node pointer) {
        this.childPointers[degree] = pointer;
        this.degree++;
    }

    // Right Shift all the childPointers by 1
    // Add given pointer to the first index and increment degree by 1
    public void prependChildPointer(Node pointer) {
        for (int i = degree - 1; i >= 0; i--) {
            childPointers[i + 1] = childPointers[i];
        }
        this.childPointers[0] = pointer;
        this.degree++;
    }


    public int findIndexOfChildPointer(Node pointer) {
        for (int i = 0; i < childPointers.length; i++) {
            if (childPointers[i] == pointer) {
                return i;
            }
        }
        return -1;
    }

    // Right Shift all childPointers by 1 from index
    // Insert given pointer at the index provided
    // and Increment the degree by 1
    public void insertChildPointer(Node pointer, int index) {
        if (degree - index >= 0) System.arraycopy(childPointers, index, childPointers, index + 1, degree - index);
        this.childPointers[index] = pointer;
        this.degree++;
    }


    public boolean isDeficient() {
        return this.degree < this.minDegree;
    }


    public boolean canLend() {
        return this.degree > this.minDegree;
    }


    public boolean isMergeable() {
        return this.degree == this.minDegree;
    }

    public void prependKey(int key) {
        int i;
        for (i = this.keys.length - 1; i > 0; i--) {
            this.keys[i] = this.keys[i - 1];
        }
        this.keys[0] = key;
    }


    public void removeKeyAndShiftLeft(int index) {
        int i;
        for (i = index; i < this.keys.length - 1; i++) {
            this.keys[i] = this.keys[i + 1];
        }
        this.keys[i] = null;
    }


    public void removePointer(int index) {
        this.childPointers[index] = null;
        this.degree--;
    }

    public void removeChildPointerAndShiftLeft(int index) {
        int i;
        for (i = index; i < this.degree - 1; i++) {
            this.childPointers[i] = this.childPointers[i + 1];
        }
        this.childPointers[i] = null;
        this.degree--;
    }
}
