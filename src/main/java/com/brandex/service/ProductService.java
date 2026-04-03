package com.brandex.service;

import com.brandex.datastructures.BST;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Product;
import com.brandex.repository.ProductRepository;

public class ProductService {

    private static ProductService instance;
    private final BST<Product> productTree = new BST<>(
            (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
    private final ProductRepository productRepo = ProductRepository.getInstance();
    private String searchQuery = "";

    public static ProductService getInstance() {
        if (instance == null)
            instance = new ProductService();
        return instance;
    }

    public void loadProducts() {
        this.productRepo.listProducts().traverse(product -> {
            this.productTree.insert(product);
        });
    }

    public void addProduct(Product product) throws Exception {
        if (product == null)
            throw new Exception("Product cannot be null.");
        this.productTree.insert(product);
    }

    public LinkedList<Product> searchByKeyword(String keyword) throws Exception {

        if (keyword == null)
            throw new Exception("Keyword cannot be null.");
        if (keyword.isEmpty())
            keyword = this.searchQuery;

        LinkedList<Product> results = new LinkedList<>(
                (a, b) -> a.getId().compareTo(b.getId()));
        String lowerKeyword = keyword.toLowerCase();

        this.productTree.traverse(p -> {
            if (p.getName().toLowerCase().contains(lowerKeyword)) {
                results.insert(p);
            }
        });

        return results;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

}
