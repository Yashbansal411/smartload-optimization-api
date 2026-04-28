# SmartLoad Optimization API

Finds the highest-revenue combination of shipment orders a truck can legally carry, respecting weight, volume, route, and hazmat constraints.

## How to run

```bash
git clone https://github.com/Yashbansal411/smartload-optimization-api.git
cd smartload-optimization-api
docker compose up --build
# Service available at http://localhost:8080
```

## Health check

```bash
curl http://localhost:8080/actuator/health
```

## Example request

```bash
curl -X POST http://localhost:8080/api/v1/load-optimizer/optimize \
  -H "Content-Type: application/json" \
  -d @sample-request.json
```

Expected response:

```json
{
  "truck_id": "truck-123",
  "selected_order_ids": ["ord-001", "ord-002"],
  "total_payout_cents": 430000,
  "total_weight_lbs": 30000,
  "total_volume_cuft": 2100,
  "utilization_weight_percent": 68.18,
  "utilization_volume_percent": 70.0
}
```

## API

### `POST /api/v1/load-optimizer/optimize`

| Status | Meaning |
|--------|---------|
| 200 | Optimal selection found (or empty if nothing fits) |
| 400 | Invalid input — missing fields, negative capacity, pickup after delivery |
| 413 | More than 22 orders submitted |

## Design notes

**Algorithm — Bitmask DP**

The problem is a 2-dimensional 0/1 knapsack (weight + volume capacity). Greedy doesn't work here. For n ≤ 22, iterating all 2ⁿ subsets is the canonical correct approach. Each subset is computed in O(1) using a lowest-set-bit trick that builds incrementally from the previous subset, giving O(2ⁿ) total time and space. At n = 22 this is ~4 M states and runs in ~50 ms.

**Constraints enforced**

- **Route compatibility** — all selected orders must share the same origin → destination lane (case-insensitive).
- **Hazmat isolation** — hazmat and non-hazmat orders are never combined on the same truck. Each pool is optimised independently and the global best is returned.
- **Time windows** — each order must have `pickup_date ≤ delivery_date`; cross-order time conflicts are not checked per spec.
- **Money** — all financial values are `long` integer cents. No `float` or `double` is used for money anywhere in the codebase.

**Performance**

Tested at n = 22 orders in < 100 ms on a standard laptop. Docker container is allocated 256 MB heap (`JAVA_OPTS=-Xmx256m`) which comfortably holds the DP arrays.
