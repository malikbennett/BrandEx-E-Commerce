package com.brandex.datastructures;

import com.brandex.models.Model;

public class LinkedList<T extends Model> {
    private Node<T> head = null; // starting point of list
    private Node<T> tail = null; // points the last element in the list

    LinkedList() {}

    public void add(T data) {
        try {
            if (this.head == null) { // If the head is empty then the list is empty
                this.head = new Node<>();
                Node<T> nodeModel = new Node<>(data);
                this.head.setNext(nodeModel); // add a new node to the head's next ptr
                this.tail = nodeModel; // last element is now the tail
                this.head.setPrevious(this.tail); //previous ptr of head is always the tail
            } else { // since the head isnt empty this is atleast 1 element inside the list
                this.tail.setNext(new Node<>(data)); // add a new node to the tail's next ptr
                this.tail.getNext().setPrevious(this.tail); // set the new node's previous ptr
                this.tail = this.tail.getNext(); // update the tail to the new node
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(T data) {
        try {
            Node<T> current = this.head.getNext(); // start at the first element
            while (current != null) { // loop until the end of the list
                if (current.getData().getId() == data.getId()) { // if we found the node to remove
                    if (current == this.tail) { // if its the tail we need to update the tail ptr
                        this.tail = current.getPrevious(); // update tail to previous node
                        this.tail.setNext(null); // set new tail's next ptr to null
                    } else { // if its not the tail we just need to bypass it
                        current.getPrevious().setNext(current.getNext()); // bypass current node by linking previous to next
                        current.getNext().setPrevious(current.getPrevious()); // link next node back to previous
                    }
                    break;
                }
                current = current.getNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public T find(int id) {
        try {
            Node<T> current = this.head.getNext(); // start at the first element
            while (current != null) { // loop until the end of the list
                if (current.getData().getId() == id) { // if we found the node
                    return current.getData(); // return the data
                }
                current = current.getNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
