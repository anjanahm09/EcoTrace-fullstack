# Proposed relational ER model (design target)

```mermaid
erDiagram
  ORGANIZATION ||--o{ USER_ORG_ROLE : grants
  USER ||--o{ USER_ORG_ROLE : assigned
  ROLE ||--o{ USER_ORG_ROLE : authorizes
  ORGANIZATION ||--o{ FACILITY : owns_or_controls
  ORGANIZATION ||--o{ PRODUCT : reports
  PRODUCT ||--o{ PRODUCT_BATCH : has
  PRODUCT_BATCH ||--o{ TRANSFER : moves_through
  ORGANIZATION ||--o{ TRANSFER : sends_or_receives
  PRODUCT_BATCH ||--o{ EMISSION_EVENT : contains
  ORGANIZATION ||--o{ EMISSION_EVENT : reports
  SCOPE_CATEGORY ||--o{ EMISSION_EVENT : classifies
  EMISSION_FACTOR ||--o{ FACTOR_VERSION : versions
  FACTOR_VERSION ||--o{ EMISSION_EVENT : calculates
  EMISSION_EVENT ||--o{ ALLOCATION : allocates
  PRODUCT_BATCH ||--o{ ALLOCATION : receives
  EMISSION_EVENT ||--o{ CALCULATION_REVISION : snapshots
  USER ||--o{ AUDIT_LOG : performs
```

This diagram is the target architecture, not a claim that all tables/relationships have been implemented. The current prototype has a smaller set of JPA entities. Before final submission, align the diagram with the actual schema and migrations.
