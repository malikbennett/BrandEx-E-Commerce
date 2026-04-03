package com.brandex.models;

public class OrderItem extends Model {
    private String cartId;
    private String productId;
    private int quantity;

    public OrderItem() {
    }

    public String getCartId() {
        return this.cartId;
    }

    public String getProductId() {
        return this.productId;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
