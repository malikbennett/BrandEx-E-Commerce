package com.brandex.datastructures;

import com.brandex.models.Model;

public class Node<T extends Model> {
    private T data;
    private Node<T> left = null;
    private Node<T> right = null;

    public Node (){}
    public Node(T data) { this.data = data;}

    public T getData() { return this.data; }
    public Node<T> getRight() { return this.right; }
    public Node<T> getLeft() { return this.left; }

    public void setData(T data) { this.data = data; }
    public void setRight(Node<T> right) { this.right = right; }
    public void setLeft(Node<T> left) { this.left = left; }
}
