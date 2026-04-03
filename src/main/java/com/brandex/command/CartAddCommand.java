package com.brandex.command;

import com.brandex.models.CartItem;
import com.brandex.datastructures.LinkedList;
import com.brandex.models.Cart;
import com.brandex.models.Product;
import com.brandex.repository.CartRepository;
import com.brandex.service.CartService;
import com.brandex.service.ProductService;

public class CartAddCommand implements Command {
    private final LinkedList<CartItem> cartList;
    private final Cart cart;
    private final CartRepository cartRepo = CartRepository.getInstance();
    private final String productId;
    private final int quantity;
    private int previousQuantity = 0;
    private boolean isNewItem = false;

    public CartAddCommand(LinkedList<CartItem> cart, String productId, int quantity) {
        this.cartList = cart;
        this.productId = productId;
        this.quantity = quantity;
        this.cart = CartService.getInstance().getCurrentCart();
    }

    @Override
    public void execute() {
        Product product = ProductService.getInstance().searchById(this.productId);
        double price = (product != null) ? product.getPrice() : 0.0;

        // find the item that was added
        CartItem item = this.cartList.search(this.productId, CartItem::getProductId);
        // if it doesnt exist then create it
        if (item == null) {
            double totalItemPrice = price * this.quantity;
            cartRepo.createCartItem(this.cart.getId(), this.productId, this.quantity, totalItemPrice);
            item = cartRepo.getCartItemByProduct(this.cart.getId(), this.productId);
            this.cartList.insert(item);
            isNewItem = true;
        } else { // if the item exist then update the quantity
            previousQuantity = item.getQuantity();
            int newQuantity = this.quantity + previousQuantity;
            double newTotalItemPrice = price * newQuantity;

            cartRepo.updateCartItem(item.getId(), "quantity", newQuantity);
            cartRepo.updateCartItem(item.getId(), "total_price", newTotalItemPrice);

            item.setQuantity(newQuantity);
            item.setTotalPrice(newTotalItemPrice);
            isNewItem = false;
        }
        CartService.getInstance().syncCartTotalWithDatabase();
    }

    @Override
    public void undo() {
        // find the item that was added
        CartItem item = this.cartList.search(this.productId, CartItem::getProductId);
        if (item != null) {
            // if it was a new item then remove it
            if (isNewItem) {
                this.cartList.remove(item);
                cartRepo.deleteCartItem(item.getId());
            } else { // if it was an existing item then update the quantity
                Product product = ProductService.getInstance().searchById(this.productId);
                double price = (product != null) ? product.getPrice() : 0.0;
                double previousTotal = price * this.previousQuantity;

                cartRepo.updateCartItem(item.getId(), "quantity", this.previousQuantity);
                cartRepo.updateCartItem(item.getId(), "total_price", previousTotal);

                item.setQuantity(this.previousQuantity);
                item.setTotalPrice(previousTotal);
            }
        }
        CartService.getInstance().syncCartTotalWithDatabase();
    }
}
