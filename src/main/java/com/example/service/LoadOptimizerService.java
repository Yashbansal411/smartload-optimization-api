package com.example.service;

import com.example.dto.OptimizeRequest;
import com.example.dto.OptimizeResponse;
import com.example.dto.OrderRequest;
import com.example.dto.TruckRequest;
import com.example.optimizer.BitmaskDpOptimizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoadOptimizerService {

    private final BitmaskDpOptimizer optimizer;

    public LoadOptimizerService(BitmaskDpOptimizer optimizer) {
        this.optimizer = optimizer;
    }

    public OptimizeResponse optimize(OptimizeRequest request) {
        TruckRequest truck = request.getTruck();
        List<OrderRequest> orders = request.getOrders();

        validateTimeWindows(orders);

        long globalBestPayout = 0L;
        int globalBestMask = 0;
        List<OrderRequest> globalBestGroup = Collections.emptyList();

        for (List<OrderRequest> laneOrders : groupByLane(orders).values()) {
            List<OrderRequest> nonHazmat = new ArrayList<>();
            List<OrderRequest> hazmat = new ArrayList<>();
            for (OrderRequest o : laneOrders) {
                (o.isHazmat() ? hazmat : nonHazmat).add(o);
            }

            for (List<OrderRequest> group : List.of(nonHazmat, hazmat)) {
                if (group.isEmpty()) continue;

                int bestMask = optimizer.optimize(group, truck.getMaxWeightLbs(), truck.getMaxVolumeCuft());
                long groupPayout = payoutForMask(group, bestMask);

                if (groupPayout > globalBestPayout) {
                    globalBestPayout = groupPayout;
                    globalBestMask = bestMask;
                    globalBestGroup = group;
                }
            }
        }

        return buildResponse(truck, globalBestGroup, globalBestMask);
    }

    private void validateTimeWindows(List<OrderRequest> orders) {
        for (OrderRequest o : orders) {
            if (o.getPickupDate().isAfter(o.getDeliveryDate())) {
                throw new IllegalArgumentException(
                        "Order " + o.getId() + ": pickup_date must not be after delivery_date");
            }
        }
    }

    private Map<String, List<OrderRequest>> groupByLane(List<OrderRequest> orders) {
        Map<String, List<OrderRequest>> laneMap = new LinkedHashMap<>();
        for (OrderRequest o : orders) {
            String key = laneKey(o.getOrigin(), o.getDestination());
            laneMap.computeIfAbsent(key, k -> new ArrayList<>()).add(o);
        }
        return laneMap;
    }

    private String laneKey(String origin, String destination) {
        return origin.trim().toLowerCase() + "|" + destination.trim().toLowerCase();
    }

    private long payoutForMask(List<OrderRequest> group, int mask) {
        long total = 0L;
        for (int i = 0; i < group.size(); i++) {
            if ((mask & (1 << i)) != 0) total += group.get(i).getPayoutCents();
        }
        return total;
    }

    private OptimizeResponse buildResponse(TruckRequest truck, List<OrderRequest> group, int mask) {
        List<String> selectedIds = new ArrayList<>();
        long totalWeight = 0L, totalVolume = 0L, totalPayout = 0L;

        for (int i = 0; i < group.size(); i++) {
            if ((mask & (1 << i)) != 0) {
                OrderRequest o = group.get(i);
                selectedIds.add(o.getId());
                totalWeight += o.getWeightLbs();
                totalVolume += o.getVolumeCuft();
                totalPayout += o.getPayoutCents();
            }
        }

        double utilWeight = round2(totalWeight * 100.0 / truck.getMaxWeightLbs());
        double utilVolume = round2(totalVolume * 100.0 / truck.getMaxVolumeCuft());

        return new OptimizeResponse(
                truck.getId(), selectedIds,
                totalPayout, totalWeight, totalVolume,
                utilWeight, utilVolume);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
