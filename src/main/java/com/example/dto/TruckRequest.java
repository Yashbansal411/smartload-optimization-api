package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class TruckRequest {

    @NotBlank(message = "truck.id must not be blank")
    @JsonProperty("id")
    private String id;

    @Positive(message = "truck.max_weight_lbs must be positive")
    @JsonProperty("max_weight_lbs")
    private long maxWeightLbs;

    @Positive(message = "truck.max_volume_cuft must be positive")
    @JsonProperty("max_volume_cuft")
    private long maxVolumeCuft;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getMaxWeightLbs() { return maxWeightLbs; }
    public void setMaxWeightLbs(long maxWeightLbs) { this.maxWeightLbs = maxWeightLbs; }

    public long getMaxVolumeCuft() { return maxVolumeCuft; }
    public void setMaxVolumeCuft(long maxVolumeCuft) { this.maxVolumeCuft = maxVolumeCuft; }
}
