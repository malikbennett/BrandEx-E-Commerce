package com.brandex.datastructures;

public class Stack<T> {
    private LinkedList<T> list = new LinkedList<>((a, b) -> 0);

    Stack() {
    }

    public void push(T data) {
        this.list.insert(data);
    }

    public T pop() {
        T data = this.list.removeTail();
        return data;
    }

    public T peek() {
        if (isEmpty())
            return null;
        return list.getTail().getData();
    }

    public boolean isEmpty() {
        return list.getTail() == null;
    }

    public void clear() {
        this.list = new LinkedList<>((a, b) -> 0);
    }
}
