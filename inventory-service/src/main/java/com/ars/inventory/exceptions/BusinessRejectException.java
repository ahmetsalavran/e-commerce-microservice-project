package com.ars.inventory.exceptions;

/**
 * Stok yok, policy gereği reddettik vb. - retry istemediğimiz "beklenen" hata.
 */
public class BusinessRejectException extends RuntimeException {
    public BusinessRejectException(String message) { super(message); }
}
