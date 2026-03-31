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

    public static ProductService getInstance() {
        if (instance == null)
            instance = new ProductService();
        return instance;
    }

    public void loadProducts() {
        productRepo.listProducts().traverse(product -> {
            productTree.insert(product);
        });
    }

    public void addProduct(Product product) throws Exception {
        if (product == null)
            throw new Exception("Product cannot be null.");
        productTree.insert(product);
    }

    public LinkedList<Product> searchByKeyword(String keyword) throws Exception {

        if (keyword == null || keyword.isEmpty())
            throw new Exception("Keyword cannot be null or empty.");

        LinkedList<Product> results = new LinkedList<>();
        String lowerKeyword = keyword.toLowerCase();

        productTree.traverse(p -> {
            if (p.getName().toLowerCase().contains(lowerKeyword)) {
                results.insert(p);
            }
        });

        return results;
    }
}
