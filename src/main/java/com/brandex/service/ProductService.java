package com.brandex.service;

import com.brandex.datastructures.BST;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Product;
import com.brandex.repository.ProductRepository;

public class ProductService {

    private static ProductService instance;
    private final BST<Product> productTree = new BST<>(
            (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

    private final LinkedList<Product> productCatalog = new LinkedList<>(
            (a, b) -> a.getId().compareTo(b.getId()));

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
            this.productCatalog.insert(product);
        });
    }

    /**
     * Persists a new product to the DB, then inserts it into both the BST and
     * the LinkedList so in-memory state stays consistent immediately.
     *
     * @throws Exception if the product is null or the DB insert fails.
     */
    public void createProduct(Product product) throws Exception {
        if (product == null)
            throw new Exception("Product cannot be null.");

        // 1. Persist to DB – get the generated UUID back
        String generatedId = this.productRepo.createProduct(product);
        if (generatedId == null)
            throw new Exception("Failed to save product to database.");

        // 2. Stamp the id on the object
        product.setId(generatedId);

        // 3. Add to both in-memory structures
        this.productTree.insert(product);
        this.productCatalog.insert(product);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Updates an existing product in the DB and refreshes both in-memory
     * structures.
     *
     * The BST is keyed by name, so if the name changed we must remove the OLD
     * node (by old name) before re-inserting with the new name. We therefore
     * require the caller to pass the original (pre-edit) name.
     *
     * @param product the product with updated field values (id must be set)
     * @param oldName the product's name BEFORE the edit (needed for BST removal)
     * @throws Exception if the DB update fails
     */
    public void updateProduct(Product product, String oldName) throws Exception {
        if (product == null)
            throw new Exception("Product cannot be null.");

        // 1. Persist changes to DB
        boolean ok = this.productRepo.updateProduct(product);
        if (!ok)
            throw new Exception("Failed to update product in database.");

        // 2. Remove old BST node (keyed on old name) and re-insert with new data.
        // We create a tiny proxy with just the old name so the BST comparator
        // finds and removes the correct node
        Product proxy = new Product();
        proxy.setName(oldName);
        this.productTree.remove(proxy);
        this.productTree.insert(product);

        // 3. Remove old LinkedList node (keyed on id) and re-insert.
        this.productCatalog.remove(product); // comparator uses id, id is unchanged
        this.productCatalog.insert(product);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    /**
     * Deletes a product from the DB and removes it from both in-memory
     * structures.
     *
     * @param product the product to delete (must have id and name set)
     * @throws Exception if the DB delete fails
     */
    public void deleteProduct(Product product) throws Exception {
        if (product == null)
            throw new Exception("Product cannot be null.");

        // 1. Remove from DB (also removes orphaned cart_items)
        boolean ok = this.productRepo.deleteProduct(product.getId());
        if (!ok)
            throw new Exception("Failed to delete product from database.");

        // 2. Remove from BST (keyed on name)
        this.productTree.remove(product);

        // 3. Remove from LinkedList (keyed on id)
        this.productCatalog.remove(product);
    }

    // ── READ / SEARCH ─────────────────────────────────────────────────────────

    /**
     * Returns a LinkedList of products whose name contains the keyword.
     * An empty keyword returns ALL products (uses the full catalog LinkedList).
     */
    public LinkedList<Product> searchByKeyword(String keyword) throws Exception {
        if (keyword == null)
            throw new IllegalArgumentException("Keyword cannot be null.");
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

    /**
     * Returns the full product LinkedList (all products, insertion order).
     * Used by the admin ManageProducts table to list everything.
     */
    public LinkedList<Product> getAllProducts() {
        return this.productCatalog;
    }

    /** Finds a product by exact id, traversing the BST. */
    public Product searchById(String id) {
        final Product[] found = new Product[1];
        this.productTree.traverse(p -> {
            if (p.getId().equals(id)) {
                found[0] = p;
            }
        });
        return found[0];
    }

    // Legacy method
    public void addProduct(Product product) throws Exception {
        if (product == null)
            throw new Exception("Product cannot be null.");
        this.productTree.insert(product);
        this.productCatalog.insert(product);
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
}
