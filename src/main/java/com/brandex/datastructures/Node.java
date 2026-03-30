package com.brandex.datastructures;

import com.brandex.models.Model;

public class Node<T extends Model> {
    private T data = null;
    private Node<T> next = null;
    private Node<T> previous = null;

    public Node (){}
    public Node(T data) { this.data = data;}

    public T getData() { return this.data; }
    public Node<T> getNext() { return this.next; }
    public Node<T> getPrevious() { return this.previous; }

    public void setData(T data) { this.data = data; }
    public void setNext(Node<T> next) { this.next = next; }
    public void setPrevious(Node<T> previous) { this.previous = previous; }
}
