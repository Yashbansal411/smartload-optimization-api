package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OptimizeResponse {

    @JsonProperty("truck_id")
    private final String truckId;

    @JsonProperty("selected_order_ids")
    private final List<String> selectedOrderIds;

    @JsonProperty("total_payout_cents")
    private final long totalPayoutCents;

    @JsonProperty("total_weight_lbs")
    private final long totalWeightLbs;

    @JsonProperty("total_volume_cuft")
    private final long totalVolumeCuft;

    @JsonProperty("utilization_weight_percent")
    private final double utilizationWeightPercent;

    @JsonProperty("utilization_volume_percent")
    private final double utilizationVolumePercent;

    public OptimizeResponse(
            String truckId,
            List<String> selectedOrderIds,
            long totalPayoutCents,
            long totalWeightLbs,
            long totalVolumeCuft,
            double utilizationWeightPercent,
            double utilizationVolumePercent) {
        this.truckId = truckId;
        this.selectedOrderIds = selectedOrderIds;
        this.totalPayoutCents = totalPayoutCents;
        this.totalWeightLbs = totalWeightLbs;
        this.totalVolumeCuft = totalVolumeCuft;
        this.utilizationWeightPercent = utilizationWeightPercent;
        this.utilizationVolumePercent = utilizationVolumePercent;
    }

    public String getTruckId() { return truckId; }
    public List<String> getSelectedOrderIds() { return selectedOrderIds; }
    public long getTotalPayoutCents() { return totalPayoutCents; }
    public long getTotalWeightLbs() { return totalWeightLbs; }
    public long getTotalVolumeCuft() { return totalVolumeCuft; }
    public double getUtilizationWeightPercent() { return utilizationWeightPercent; }
    public double getUtilizationVolumePercent() { return utilizationVolumePercent; }
}
