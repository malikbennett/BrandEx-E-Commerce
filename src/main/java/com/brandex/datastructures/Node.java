package com.brandex.datastructures;

// A node for our custom data structures.
public class Node<T> {
    private T data;
    private Node<T> left = null;
    private Node<T> right = null;

    // Default constructor.
    public Node() {
    }

    // Constructor that takes data.
    public Node(T data) {
        this.data = data;
    }

    // Returns the data.
    public T getData() {
        return this.data;
    }

    // Returns the right child.
    public Node<T> getRight() {
        return this.right;
    }

    // Returns the left child.
    public Node<T> getLeft() {
        return this.left;
    }

    // Sets the data.
    public void setData(T data) {
        this.data = data;
    }

    // Sets the right child.
    public void setRight(Node<T> right) {
        this.right = right;
    }

    // Sets the left child.
    public void setLeft(Node<T> left) {
        this.left = left;
    }
}
