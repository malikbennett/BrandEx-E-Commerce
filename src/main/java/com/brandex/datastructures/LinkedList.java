package com.brandex.datastructures;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

public class LinkedList<T> {
    private Node<T> head = null; // starting point of list
    private Node<T> tail = null; // points the last element in the list
    private Comparator<T> comparator;

    public LinkedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void insert(T data) {
        if (data == null)
            return;
        Node<T> newNode = new Node<>(data);
        if (this.head == null) {
            this.head = newNode;
            this.tail = newNode;
        } else {
            this.tail.setRight(newNode);
            newNode.setLeft(this.tail);
            this.tail = newNode;
        }
    }

    public void remove(T data) {
        if (this.head == null || data == null)
            return;
        Node<T> current = this.head;
        while (current != null) {
            int cmp = comparator.compare(data, current.getData());
            if (cmp == 0) {
                if (current == this.head && current == this.tail) {
                    this.head = null;
                    this.tail = null;
                } else if (current == this.head) {
                    this.head = current.getRight();
                    this.head.setLeft(null);
                } else if (current == this.tail) {
                    this.tail = current.getLeft();
                    this.tail.setRight(null);
                } else {
                    current.getLeft().setRight(current.getRight());
                    current.getRight().setLeft(current.getLeft());
                }
                break;
            }
            current = current.getRight();
        }
    }

    public T search(String key, Function<T, String> keyExtractor) {
        if (key == null)
            return null;
        Node<T> current = this.head;
        while (current != null) {
            if (key.equalsIgnoreCase(keyExtractor.apply(current.getData()))) {
                return current.getData();
            }
            current = current.getRight();
        }
        return null;
    }

    public void traverse(Consumer<T> action) {
        Node<T> current = this.head;
        while (current != null) {
            action.accept(current.getData());
            current = current.getRight();
        }
    }

    public T removeTail() {
        if (this.tail == null)
            return null;
        T Data = this.tail.getData();
        this.tail = this.tail.getLeft();
        if (this.tail == null) {
            this.head = null;
        } else {
            this.tail.setRight(null);
        }
        return Data;
    }

    public Node<T> getTail() {
        return this.tail;
    }

    public Node<T> getHead() {
        return this.head;
    }
}
