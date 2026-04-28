package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OptimizeRequest {

    @NotNull(message = "truck must not be null")
    @Valid
    @JsonProperty("truck")
    private TruckRequest truck;

    @NotNull(message = "orders must not be null")
    @JsonProperty("orders")
    private List<@Valid OrderRequest> orders;

    public TruckRequest getTruck() { return truck; }
    public void setTruck(TruckRequest truck) { this.truck = truck; }

    public List<OrderRequest> getOrders() { return orders; }
    public void setOrders(List<OrderRequest> orders) { this.orders = orders; }
}
