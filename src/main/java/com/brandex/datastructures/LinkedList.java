package com.brandex.datastructures;

import java.util.function.Consumer;
import java.util.function.Function;

import com.brandex.models.Model;

public class LinkedList<T extends Model> {
    private Node<T> head = null; // starting point of list
    private Node<T> tail = null; // points the last element in the list

    public LinkedList() {}

    public void insert(T data) {
        if (data == null) return;
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

    public void delete(T data) {
        if (this.head == null || data == null) return;
        Node<T> current = this.head;
        while (current != null) {
            if (current.getData().getId() != null && current.getData().getId().equals(data.getId())) {
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
        if (key == null) return null;
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
}
