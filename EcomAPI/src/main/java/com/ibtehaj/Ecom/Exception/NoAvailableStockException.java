package com.ibtehaj.Ecom.Exception;

public class NoAvailableStockException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoAvailableStockException(String message) {
        super(message);
    }

}
