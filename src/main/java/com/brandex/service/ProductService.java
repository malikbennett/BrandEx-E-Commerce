package com.brandex.service;

import java.util.function.Consumer;

import com.brandex.datastructures.BST;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Product;
import com.brandex.repository.ProductRepository;

public class ProductService {

    private static ProductService instance;
    private final ProductRepository productRepo = ProductRepository.getInstance();
    private final BST<Product> productTree = new BST<>(
            (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

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

    public void forEachProduct(Consumer<Product> action) {
        this.productTree.traverse(action);
    }

    public void createProduct(Product product) throws Exception {
        if (product == null)
            throw new Exception("Product cannot be null.");

        String generatedId = this.productRepo.createProduct(product);
        if (generatedId == null)
            throw new Exception("Failed to save product to database.");

        product.setId(generatedId);
        this.productTree.insert(product);
    }

    public void updateProduct(Product product, String oldName, Runnable updateAction) throws Exception {
        if (product == null)
            throw new Exception("Product cannot be null.");

        // remove from tree using the old key before it's modified
        Product proxy = new Product();
        proxy.setName(oldName);
        this.productTree.remove(proxy);

        // apply the updates (name, etc.)
        updateAction.run();

        // update database
        boolean ok = this.productRepo.updateProduct(product);
        if (!ok) {
            // Re-insert if DB fails (using the name it has now)
            this.productTree.insert(product);
            throw new Exception("Failed to update product in database.");
        }

        // re-insert with the new key position
        this.productTree.insert(product);
    }

    public void deleteProduct(Product product) throws Exception {
        if (product == null)
            throw new Exception("Product cannot be null.");

        boolean ok = this.productRepo.deleteProduct(product.getId());
        if (!ok)
            throw new Exception("Failed to delete product from database.");

        this.productTree.remove(product);
    }

    public LinkedList<Product> searchByKeyword(String keyword) {
        if (keyword == null)
            throw new IllegalArgumentException("Keyword cannot be null.");
        if (keyword.isEmpty())
            keyword = this.searchQuery;

        LinkedList<Product> results = new LinkedList<>((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        String lowerKeyword = keyword.toLowerCase();

        this.productTree.traverse(p -> {
            if (p.getName().toLowerCase().contains(lowerKeyword)) {
                results.insert(p);
            }
        });

        return results;
    }

    public Product searchById(String id) {
        final Product[] found = new Product[1];
        this.productTree.traverse(p -> {
            if (p.getId().equals(id)) {
                found[0] = p;
            }
        });
        return found[0];
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }

    public void setSearchQuery(String q) {
        this.searchQuery = q;
    }

    public void clearProducts() {
        this.productTree.clear();
    }

    public BST<Product> getProductsTree() {
        return productTree;
    }
}
