package com.brandex.datastructures;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

public class BST<T> {
    private Node<T> root = null; // starting point of the tree
    private Comparator<T> comparator;

    private Node<T> insertRecursive(Node<T> node, T data) {
        if (data == null)
            return node;
        if (node == null)
            return new Node<T>(data);

        int cmp = comparator.compare(data, node.getData());
        if (cmp < 0) {
            node.setLeft(insertRecursive(node.getLeft(), data));
        } else {
            node.setRight(insertRecursive(node.getRight(), data));
        }
        return node;
    }

    private T searchRecursive(Node<T> node, String key, Function<T, String> keyExtractor) {
        // samething here if the node is null, then thier is no children node to search,
        // or better say the previous node is a leaf node
        if (node == null || key == null)
            return null;
        // This is using a String method that compares two strings no matter the case,
        // as in case-sensitvity
        // keyExtractor is just a function, since we dont know the data type of
        // node.data(like whether its a Product, or User)
        // we cant use specific methods like getName() for Products, or getUsername()
        // for users, so instead we pass in the functions
        // as arguements, that we it doesnt matter what the type is, the caller knows
        // what functions to pass, saying keyExtractor.apply()
        // is just the samething as saying node.getData().getName(), i.e if the
        // node.data was of Product type and the getName method is what was passed.
        int cmp = key.compareToIgnoreCase(keyExtractor.apply(node.getData()));
        if (cmp == 0) {
            return node.getData();
        } else if (cmp < 0) {
            return searchRecursive(node.getLeft(), key, keyExtractor);
        } else {
            return searchRecursive(node.getRight(), key, keyExtractor);
        }
    }

    private Node<T> removeRecursive(Node<T> node, T data) {
        if (node == null || data == null)
            return node; // same logic, the previous node was a leaf node so we can just remove its
                         // parent, i.e setting the parent pointer to null
        int cmp = comparator.compare(data, node.getData()); // same logic
        if (cmp < 0) { // same logic
            node.setLeft(removeRecursive(node.getLeft(), data));
        } else if (cmp > 0) {
            node.setRight(removeRecursive(node.getRight(), data));
        } else { // we found the match
            // if the node that needs to be deleted has a child, one, then we simply replace
            // it with its child
            // // i.e setting its parents pointer to its child
            if (node.getLeft() == null) { // if the left side is not null then we check if we right side is null
                return node.getRight();
            }
            if (node.getRight() == null) { // if the right child is also not null then this node has 2 children
                return node.getLeft();
            }
            // we have to replace the node we want to remove with one of its children, i.e
            // its successor
            // the successor will then be the new parent, the best candidate for a successor
            // would be
            // the smallest value on the right side of the node we are replacing, getMin()
            // finds the smallest node
            Node<T> successor = getMin(node.getRight());
            // simply replace the data from the node with the successors data
            // the node is still the same, all its children and parent is the same, we just
            // replaces its value
            node.setData(successor.getData());
            // Now we want to remove the successor from the right side
            node.setRight(removeRecursive(node.getRight(), successor.getData()));
        }
        return node;
    }

    private Node<T> getMin(Node<T> node) {
        while (node.getLeft() != null) // Keeps going left until it cant anymore
            node = node.getLeft();
        return node;
    }

    private void inOrderRecursive(Node<T> node, Consumer<T> action) {
        if (node == null) // Same logic
            return;
        inOrderRecursive(node.getLeft(), action); // left first
        action.accept(node.getData()); // then root
        inOrderRecursive(node.getRight(), action); // then right
    }

    private void preOrderRecursive(Node<T> node, Consumer<T> action) {
        if (node == null) // Same logic
            return;
        action.accept(node.getData()); // root first
        preOrderRecursive(node.getLeft(), action); // then left
        preOrderRecursive(node.getRight(), action); // then right
    }

    private void postOrderRecursive(Node<T> node, Consumer<T> action) {
        if (node == null) // Same logic
            return;
        postOrderRecursive(node.getLeft(), action); // left first
        postOrderRecursive(node.getRight(), action); // then right
        action.accept(node.getData()); // root last
    }

    private void getRangeRecursive(Node<T> node, T min, T max, Consumer<T> action) {
        if (node == null) // Same logic
            return;
        // This is just comparing the min and max to the current node
        int cmpMin = comparator.compare(min, node.getData());
        int cmpMax = comparator.compare(max, node.getData());
        // If current node is greater than min, there might be values in the left
        // subtree
        if (cmpMin < 0) {
            getRangeRecursive(node.getLeft(), min, max, action);
        }
        // If current node is within range, process/add it
        if (cmpMin <= 0 && cmpMax >= 0) {
            action.accept(node.getData());
        }
        // If current node is less than max, there might be values in the right subtree
        if (cmpMax > 0) {
            getRangeRecursive(node.getRight(), min, max, action);
        }
    }

    public BST(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void insert(T data) {
        this.root = insertRecursive(this.root, data);
    }

    public T search(String key, Function<T, String> keyExtractor) {
        return searchRecursive(this.root, key, keyExtractor);
    }

    public void remove(T data) {
        this.root = removeRecursive(this.root, data);
    }

    // in order traversal
    public void inOrder(Consumer<T> action) {
        inOrderRecursive(root, action);
    }

    // pre order traversal, dont really need but i did it for practice
    public void preOrder(Consumer<T> action) {
        preOrderRecursive(root, action);
    }

    // post order traversal, dont really need but i did it for practice as well
    public void postOrder(Consumer<T> action) {
        postOrderRecursive(root, action);
    }

    // get range traversal
    public void getRange(T min, T max, Consumer<T> action) {
        getRangeRecursive(root, min, max, action);
    }

    public void traverse(Consumer<T> action) {
        inOrder(action);
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void clear() {
        root = null;
    }
}
