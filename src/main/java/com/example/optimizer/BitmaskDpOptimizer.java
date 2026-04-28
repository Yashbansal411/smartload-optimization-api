package com.example.optimizer;

import com.example.dto.OrderRequest;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BitmaskDpOptimizer {

    /**
     * Finds the highest-payout subset of orders that fits within the truck's
     * weight and volume limits, using bitmask DP over all 2^n subsets.
     *
     * Complexity: O(2^n) time and space. Safe for n <= 22 (~4M states, ~96MB).
     *
     * @return bitmask of the best valid subset (0 = no feasible combination)
     */
    public int optimize(List<OrderRequest> orders, long maxWeightLbs, long maxVolumeCuft) {
        int n = orders.size();
        if (n == 0) return 0;

        int totalMasks = 1 << n;
        long[] weight = new long[totalMasks];
        long[] volume = new long[totalMasks];
        long[] payout = new long[totalMasks];

        long bestPayout = 0L;
        int bestMask = 0;

        for (int mask = 1; mask < totalMasks; mask++) {
            // Lowest-set-bit trick: build incrementally from the previous subset
            // so each mask is computed in O(1) rather than O(n).
            int lsb = Integer.numberOfTrailingZeros(mask);
            int prev = mask ^ (1 << lsb);
            OrderRequest order = orders.get(lsb);

            weight[mask] = weight[prev] + order.getWeightLbs();
            volume[mask] = volume[prev] + order.getVolumeCuft();
            payout[mask] = payout[prev] + order.getPayoutCents();

            if (weight[mask] <= maxWeightLbs && volume[mask] <= maxVolumeCuft
                    && payout[mask] > bestPayout) {
                bestPayout = payout[mask];
                bestMask = mask;
            }
        }

        return bestMask;
    }
}
