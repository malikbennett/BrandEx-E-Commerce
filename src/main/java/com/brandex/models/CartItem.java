
package com.brandex.models;

public class CartItem extends Model {
    private String cartId;
    private String productId;
    private int quantity;
    private double totalPrice;

    public CartItem() {
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

    public double getTotalPrice() {
        return this.totalPrice;
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

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
