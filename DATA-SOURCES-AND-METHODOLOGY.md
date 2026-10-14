# Data sources and calculation methodology

## Assignment references
1. Climatiq Data Explorer: https://www.climatiq.io/data/explorer
2. Climatiq API documentation: https://www.climatiq.io/docs
3. GHG Protocol Scope 3 Calculation Guidance: https://ghgprotocol.org/scope-3-calculation-guidance-2

## Version check performed 14 October 2026
Climatiq's official data-version documentation lists fixed release `37` as the current release at the time checked. Use a fixed version for reproducible calculations and record the access date. Re-check before submission because releases can change. References:
- https://www.climatiq.io/docs/api-reference/data-version
- https://www.climatiq.io/docs/guides/working-with-climatiq-data/data-management-and-quality/data-versioning

The Data Explorer is useful for browsing factor metadata and source, region, year, unit type and lifecycle activity. API search/estimate requests require an API key. Do not assume that viewing a factor grants rights to bulk download, redistribute, or embed all values. Check the license and plan terms for every factor.

## Important demo-data rule
The seed rows in this starter are explicitly marked illustrative and use placeholder values. They are for testing the UI/calculation flow only and **must not be described as verified Climatiq/GHG Protocol factors or used for real reporting**. Replace each placeholder with a factor selected from the Data Explorer or an authorized API response. Capture the exact factor ID, publisher/source URL, factor value and unit, dataset release, reference year, geography, gas/GWP basis, lifecycle boundary, method, license/access terms and access date.

## Core formulas
- General activity: `emissions (kg CO2e) = activity quantity in factor unit × factor (kg CO2e per factor unit)`.
- Freight: `tonne-km = shipped mass in metric tonnes × distance in km`; multiply by a factor whose unit and transport mode match.
- Unit conversion must occur before multiplication. Reject incompatible units and negative quantities. Use decimal arithmetic and round only for display.
- If the selected factor is already expressed in CO2e, do not apply GWP again. For gas-specific factors, record the GWP assessment basis and convert each gas consistently.
- Missing factor or missing activity data is **unknown**, not zero. Record an explicit unavailable status and reason.
- Scope 2 location-based and market-based totals are separate views; do not add them together. Only calculate market-based emissions when a valid contractual/supplier factor and evidence support it.
- Do not add a supplier's Scope 1/2 inventory to a buyer's Scope 3 estimate for the same emission event. Product totals should sum unique events once, with documented allocation for shared events.

## Coverage model for Scope 3
1. Purchased goods and services
2. Capital goods
3. Fuel- and energy-related activities (not included in Scope 1 or Scope 2)
4. Upstream transportation and distribution
5. Waste generated in operations
6. Business travel
7. Employee commuting
8. Upstream leased assets
9. Downstream transportation and distribution
10. Processing of sold products
11. Use of sold products
12. End-of-life treatment of sold products
13. Downstream leased assets
14. Franchises
15. Investments

A category being listed in the application does not mean it has been calculated. Each reporting organization and period needs a documented applicable/not-applicable/data-unavailable status and supporting reason.
