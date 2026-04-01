package com.brandex.datastructures;

public class Stack<T> {
    private class Node {
        T data;
        Node next;
        Node(T data) { this.data = data; }
    }
    private Node top;

    public void push(T data) {
        Node newNode = new Node(data);
        newNode.next = top;
        top = newNode;
    }

    public T pop() {
        if (isEmpty()) return null;
        T data = top.data;
        top = top.next;
        return data;
    }

    public boolean isEmpty() { return top == null; }
    public void clear() { top = null; }
}