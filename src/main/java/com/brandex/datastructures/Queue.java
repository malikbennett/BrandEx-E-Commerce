package com.brandex.datastructures;

import java.util.Comparator;
import java.util.function.Consumer;

// The ADT Queue, implemented using our ADT LinkedList.
public class Queue<T> {
    private LinkedList<T> list = new LinkedList<>((a, b) -> a.equals(b) ? 0 : 1);
    private int size = 0;

    // Default constructor.
    public Queue() {
    }

    // Constructor that takes a comparator.
    public Queue(Comparator<T> comparator) {
        this.list = new LinkedList<>(comparator);
    }

    // Adds an element to the back of the queue.
    public void enqueue(T data) {
        if (data != null) {
            this.list.insert(data);
            this.size++;
        }
    }

    // Removes and returns the element at the front of the queue.
    public T dequeue() {
        if (this.list.isEmpty())
            return null;
        T data = this.list.getHead().getData();
        this.list.remove(data);
        this.size--;
        return data;
    }

    // Returns the element at the front of the queue without removing it.
    public T peek() {
        if (this.list.isEmpty())
            return null;
        return this.list.getHead().getData();
    }

    // Returns true if the queue is empty, false otherwise.
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    // Returns the number of elements in the queue.
    public int size() {
        return this.size;
    }

    // Clears the queue.
    public void clear() {
        this.list.clear();
        this.size = 0;
    }

    // Traverses the queue.
    public void traverse(Consumer<T> action) {
        this.list.traverse(action);
    }
}
