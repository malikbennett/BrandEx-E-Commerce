package com.brandex.datastructures;

import java.util.Comparator;

// The ADT Stack, implemented using our ADT LinkedList.
public class Stack<T> {
    private LinkedList<T> list = new LinkedList<>((a, b) -> 0);

    // Default constructor.
    public Stack() {
    }

    // Constructor that takes a comparator.
    public Stack(Comparator<T> comparator) {
        this.list = new LinkedList<>(comparator);
    }

    // Pushes an element onto the top of the stack.
    public void push(T data) {
        this.list.insert(data);
    }

    // Removes and returns the element at the top of the stack.
    public T pop() {
        T data = this.list.removeTail();
        return data;
    }

    // Returns the element at the top of the stack without removing it.
    public T peek() {
        if (isEmpty())
            return null;
        return list.getTail().getData();
    }

    // Returns true if the stack is empty, false otherwise.
    public boolean isEmpty() {
        return list.getTail() == null;
    }

    // Clears the stack.
    public void clear() {
        this.list = new LinkedList<>((a, b) -> 0);
    }
}
