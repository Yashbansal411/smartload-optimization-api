package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public class OrderRequest {

    @NotBlank(message = "order.id must not be blank")
    @JsonProperty("id")
    private String id;

    @PositiveOrZero(message = "order.payout_cents must be >= 0")
    @JsonProperty("payout_cents")
    private long payoutCents;

    @PositiveOrZero(message = "order.weight_lbs must be >= 0")
    @JsonProperty("weight_lbs")
    private long weightLbs;

    @PositiveOrZero(message = "order.volume_cuft must be >= 0")
    @JsonProperty("volume_cuft")
    private long volumeCuft;

    @NotBlank(message = "order.origin must not be blank")
    @JsonProperty("origin")
    private String origin;

    @NotBlank(message = "order.destination must not be blank")
    @JsonProperty("destination")
    private String destination;

    @NotNull(message = "order.pickup_date must not be null")
    @JsonProperty("pickup_date")
    private LocalDate pickupDate;

    @NotNull(message = "order.delivery_date must not be null")
    @JsonProperty("delivery_date")
    private LocalDate deliveryDate;

    @JsonProperty("is_hazmat")
    private boolean isHazmat;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getPayoutCents() { return payoutCents; }
    public void setPayoutCents(long payoutCents) { this.payoutCents = payoutCents; }

    public long getWeightLbs() { return weightLbs; }
    public void setWeightLbs(long weightLbs) { this.weightLbs = weightLbs; }

    public long getVolumeCuft() { return volumeCuft; }
    public void setVolumeCuft(long volumeCuft) { this.volumeCuft = volumeCuft; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public boolean isHazmat() { return isHazmat; }
    public void setHazmat(boolean hazmat) { isHazmat = hazmat; }
}
