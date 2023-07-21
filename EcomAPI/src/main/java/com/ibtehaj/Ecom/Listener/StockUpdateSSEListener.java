package com.ibtehaj.Ecom.Listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;

import com.ibtehaj.Ecom.Controller.StockUpdateSSEController;

import reactor.core.publisher.Flux;

@Component
public class StockUpdateSSEListener {

    @Autowired
    private StockUpdateSSEController sseHandler;

    public StockUpdateSSEListener(StockUpdateSSEController sseHandler) {
    	//this.sseHandler = sseHandler;
        sseHandler.priceUpdateSSE().subscribe(this::handlePriceUpdate);
    }

    private void handlePriceUpdate(Double newPrice) {
        // This method will be called whenever there's a price update from the SSE handler
        // Add your logic here to process the price update on the server-side
        System.out.println("Received price update: " + newPrice);
    }
}

