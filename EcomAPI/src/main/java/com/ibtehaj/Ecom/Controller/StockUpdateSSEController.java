package com.ibtehaj.Ecom.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;


@SuppressWarnings("deprecation")
@RestController
public class StockUpdateSSEController {

    private final FluxProcessor<Double, Double> priceUpdateProcessor;
    private final FluxSink<Double> priceUpdateSink;

    public StockUpdateSSEController() {
        this.priceUpdateProcessor = DirectProcessor.create();
        this.priceUpdateSink = priceUpdateProcessor.sink();
    }

    @GetMapping(value = "/sse/price-update", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Double> priceUpdateSSE() {
        return priceUpdateProcessor
                .distinctUntilChanged() // Only send unique price updates
                .share(); // Share the Flux among multiple subscribers
    }

    // Method to trigger SSE update when the price is updated
    // Call this method whenever the price is changed in your application
    public void triggerPriceUpdate(Double newPrice) {
        priceUpdateSink.next(newPrice);
    }
}
