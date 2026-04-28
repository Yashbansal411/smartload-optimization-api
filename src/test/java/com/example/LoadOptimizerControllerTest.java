package com.example;

import com.example.dto.OptimizeRequest;
import com.example.dto.OrderRequest;
import com.example.dto.TruckRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoadOptimizerControllerTest {

    private static final String URL = "/api/v1/load-optimizer/optimize";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private TruckRequest truck(String id, long maxWeight, long maxVolume) {
        TruckRequest t = new TruckRequest();
        t.setId(id);
        t.setMaxWeightLbs(maxWeight);
        t.setMaxVolumeCuft(maxVolume);
        return t;
    }

    private OrderRequest order(String id, long payout, long weight, long volume,
                               String origin, String dest,
                               String pickup, String delivery, boolean hazmat) {
        OrderRequest o = new OrderRequest();
        o.setId(id);
        o.setPayoutCents(payout);
        o.setWeightLbs(weight);
        o.setVolumeCuft(volume);
        o.setOrigin(origin);
        o.setDestination(dest);
        o.setPickupDate(LocalDate.parse(pickup));
        o.setDeliveryDate(LocalDate.parse(delivery));
        o.setHazmat(hazmat);
        return o;
    }

    private String json(OptimizeRequest req) throws Exception {
        return objectMapper.writeValueAsString(req);
    }

    // -----------------------------------------------------------------------
    // Test 1: happy path — PDF sample, expects ord-001 + ord-002
    // -----------------------------------------------------------------------
    @Test
    void happyPath_selectsBestNonHazmatCombo() throws Exception {
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(truck("truck-123", 44000, 3000));
        req.setOrders(List.of(
                order("ord-001", 250000, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                        "2025-12-05", "2025-12-09", false),
                order("ord-002", 180000, 12000, 900, "Los Angeles, CA", "Dallas, TX",
                        "2025-12-04", "2025-12-10", false),
                order("ord-003", 320000, 30000, 1800, "Los Angeles, CA", "Dallas, TX",
                        "2025-12-06", "2025-12-08", true)
        ));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truck_id").value("truck-123"))
                .andExpect(jsonPath("$.total_payout_cents").value(430000))
                .andExpect(jsonPath("$.selected_order_ids", containsInAnyOrder("ord-001", "ord-002")))
                .andExpect(jsonPath("$.total_weight_lbs").value(30000))
                .andExpect(jsonPath("$.total_volume_cuft").value(2100));
    }

    // -----------------------------------------------------------------------
    // Test 2: hazmat isolation — hazmat wins when it has higher payout alone
    // -----------------------------------------------------------------------
    @Test
    void hazmatIsolation_hazmatWinsWhenHigherPayout() throws Exception {
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(truck("t1", 44000, 3000));
        req.setOrders(List.of(
                order("non-001", 100000, 5000, 500, "Chicago, IL", "Miami, FL",
                        "2025-12-01", "2025-12-05", false),
                order("haz-001", 500000, 10000, 800, "Chicago, IL", "Miami, FL",
                        "2025-12-01", "2025-12-05", true)
        ));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selected_order_ids", contains("haz-001")))
                .andExpect(jsonPath("$.total_payout_cents").value(500000));
    }

    // -----------------------------------------------------------------------
    // Test 3: empty orders list — 200 with empty selection
    // -----------------------------------------------------------------------
    @Test
    void emptyOrders_returnsEmptySelection() throws Exception {
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(truck("t1", 44000, 3000));
        req.setOrders(List.of());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selected_order_ids", hasSize(0)))
                .andExpect(jsonPath("$.total_payout_cents").value(0));
    }

    // -----------------------------------------------------------------------
    // Test 4: all orders exceed capacity individually — empty selection
    // -----------------------------------------------------------------------
    @Test
    void allOrdersExceedCapacity_returnsEmptySelection() throws Exception {
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(truck("t1", 5000, 500));
        req.setOrders(List.of(
                order("big-001", 999999, 50000, 9999, "LA, CA", "NY, NY",
                        "2025-01-01", "2025-01-10", false)
        ));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selected_order_ids", hasSize(0)))
                .andExpect(jsonPath("$.total_payout_cents").value(0));
    }

    // -----------------------------------------------------------------------
    // Test 5: 23 orders — 413 Payload Too Large
    // -----------------------------------------------------------------------
    @Test
    void tooManyOrders_returns413() throws Exception {
        List<OrderRequest> orders = new ArrayList<>();
        for (int i = 0; i < 23; i++) {
            orders.add(order("ord-" + i, 1000, 100, 10, "A", "B",
                    "2025-01-01", "2025-01-02", false));
        }
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(truck("t1", 99999, 99999));
        req.setOrders(orders);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isPayloadTooLarge());
    }

    // -----------------------------------------------------------------------
    // Test 6: null truck — 400
    // -----------------------------------------------------------------------
    @Test
    void nullTruck_returns400() throws Exception {
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(null);
        req.setOrders(List.of());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Test 7: null orders — 400
    // -----------------------------------------------------------------------
    @Test
    void nullOrders_returns400() throws Exception {
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(truck("t1", 44000, 3000));
        req.setOrders(null);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Test 8: pickup_date after delivery_date — 400
    // -----------------------------------------------------------------------
    @Test
    void invalidTimeWindow_returns400() throws Exception {
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(truck("t1", 44000, 3000));
        req.setOrders(List.of(
                order("ord-001", 100000, 5000, 500, "LA, CA", "NY, NY",
                        "2025-12-10", "2025-12-05", false)  // pickup after delivery
        ));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("ord-001")));
    }

    // -----------------------------------------------------------------------
    // Test 9: orders on different lanes — picks best single-lane subset
    // -----------------------------------------------------------------------
    @Test
    void differentLanes_picksBestLane() throws Exception {
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(truck("t1", 44000, 3000));
        req.setOrders(List.of(
                order("la-dal", 300000, 10000, 800, "Los Angeles, CA", "Dallas, TX",
                        "2025-12-01", "2025-12-05", false),
                order("chi-mia", 100000, 5000, 400, "Chicago, IL", "Miami, FL",
                        "2025-12-01", "2025-12-05", false)
        ));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selected_order_ids", contains("la-dal")))
                .andExpect(jsonPath("$.total_payout_cents").value(300000));
    }

    // -----------------------------------------------------------------------
    // Test 10: negative truck weight — 400
    // -----------------------------------------------------------------------
    @Test
    void negativeTruckCapacity_returns400() throws Exception {
        OptimizeRequest req = new OptimizeRequest();
        req.setTruck(truck("t1", -1000, 3000));
        req.setOrders(List.of());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(req)))
                .andExpect(status().isBadRequest());
    }
}
