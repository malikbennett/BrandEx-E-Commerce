package com.brandex.datastructures;

import java.util.Comparator;
import java.util.function.Function;

import com.brandex.models.Model;

public class BST<T extends Model> {
    private Node<T> root;
    private Comparator<T> comparator;

    private Node<T> InsertRecurssive(Node<T> node, T data) {
        try {
            // If the note that passed in is null then there is no more children nodes to check
            //  so we return on this position which stops the recurssion
            if (node.getData() == null) return new Node<T>(data);
            // This is what allows us to compare type of T, since T is a UDDT and could be any of our models, we don't know
            // its attribute so we have to specify a way to compare them when we create an instance of our BTS
            int cmp = comparator.compare(data, node.getData());
            if (cmp < 0) { // If the cmp is 0 then we found a match, if its less then the data is less than the current node's data
                node.setLeft(InsertRecurssive(node.getLeft(), data)); // continue recursion of the left side
            } else if (cmp > 0) { // if its greater then the data is greater than the current node's data, which means its on the right side of the tree node
                node.setRight(InsertRecurssive(node.getRight(), data)); // contiue recursion on the right side
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return node;
    }

    private T SearchRecurssive(Node<T> node, String key, Function<T, String> keyExtractor) {
        try {
            // samething here if the node is null, then thier is no children node to search, or better say the previous node is a leaf node
            if (node == null) return null;
            // This is using a String method that compares two strings no matter the case, as in case-sensitvity
            // keyExtractor is just a function, since we dont know the data type of node.data(like whether its a Product, or User)
            // we cant use specific methods like getName() for Products, or getUsername() for users, so instead we pass in the functions
            // as arguements, that we it doesnt matter what the type is, the caller knows what functions to pass, saying keyExtractor.apply()
            // is just the samething as saying node.getData().getName(), i.e if the node.data was of Product type and the getName method is what was passed.
            int cmp = key.compareToIgnoreCase(keyExtractor.apply(node.getData()));
            if (cmp == 0) { // same comparision logic explained eariler, 0 equal, less is less, more is more
                return node.getData();
            } else if (cmp < 0) {
                return SearchRecurssive(node.getLeft(), key, keyExtractor);
            } else {
                return SearchRecurssive(node.getLeft(), key, keyExtractor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Node<T> deleteRecursive(Node<T> node, T data) {
        try {
            if (node == null) return null; // same logic, the previous node was a leaf node so we can just remove its parent, i.e setting the parent pointer to null
            int cmp = comparator.compare(data, node.getData()); // same logic
            if (cmp < 0) { // same logic
                node.setLeft(deleteRecursive(node.getLeft(), data));
            } else if (cmp > 0) {
                node.setRight(deleteRecursive(node.getRight(), data));
            } else { // we found the match
                // if the node that needs to be deleted has a child, one, then we simply replace it with its child
                // // i.e setting its parents pointer to its child
                if (node.getLeft() == null) { // if the left side is not null then we check if we right side is null
                    return node.getRight();
                }
                if (node.getRight() == null) { // if the right child is also not null then this node has 2 children
                    return node.getLeft();
                }
                // we have to replace the node we want to remove with one of its children, i.e its successor
                // the successor will then be the new parent, the best candidate for a successor would be
                // the smallest value on the right side of the node we are replacing, getMin() finds the smallest node
                Node<T> successor = getMin(node.getRight());
                // simply replace the data from the node with the successors data
                // the node is still the same, all its children and parent is the same, we just replaces its value
                node.setData(successor.getData());
                // Now we want to remove the successor from the right side
                node.setRight(deleteRecursive(node.getRight(), successor.getData()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return node;
    }

    private Node<T> getMin(Node<T> node) {
        while (node.getLeft() != null) // Keeps going left until it cant anymore
            node = node.getLeft();
        return node;
    }

    public BST(Comparator<T> comparator) {
        this.root = null;
        this.comparator = comparator;
    }

    public void insert(T data) {
        this.root = InsertRecurssive(this.root, data);
    }

    public T search(String key, Function<T, String> keyExtractor) {
        return SearchRecurssive(this.root, key, keyExtractor);
    }

    public Node<T> delete(T data) {
        return deleteRecursive(this.root, data);
    }
}
