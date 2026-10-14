# EcoTrace assignment coverage and honest status

This file is a reviewer-oriented checklist. A checked item means the starter contains a representation or partial implementation, **not** that the full acceptance test has passed. Mark items complete only after testing in your local environment.

## Assignment technologies
- [x] Java 21 + Spring Boot REST API project
- [x] MySQL/JPA persistence configured through environment variables
- [x] Spring Security roles and BCrypt password encoder (demo users currently in-memory)
- [x] React + Vite frontend
- [x] Tailwind CSS dependency and PostCSS configuration added; custom CSS is also used
- [x] OpenAPI/Swagger dependency and Swagger UI path configured
- [ ] Verify a clean backend Maven build on the target machine
- [ ] Verify `npm install` and production build on the target machine

## Functional coverage in current starter
- [x] Static category endpoint lists Scope 1, Scope 2 and all 15 Scope 3 categories
- [x] Organization, factor, activity records and summary endpoint
- [x] Calculation record stores an emission-factor reference/snapshot fields
- [x] Factor source, URL, geography, year, dataset version and illustrative flag fields
- [x] Explicit warning that illustrative factors are not valid reporting factors
- [ ] Replace in-memory users with persistent user/role/org membership model
- [ ] Enforce organization isolation on every endpoint/query; current starter is not production-safe for cross-organization data
- [ ] Separate market-based and location-based Scope 2 accounting and validation
- [ ] Complete unit conversions and reject incompatible units
- [ ] Freight tonne-km validation, shared-leg de-duplication, allocation denominator checks
- [ ] Supply-chain transfer graph, facility/product/batch identifiers, all six demo journey stages
- [ ] Scope 3 category applicability/status and evidence/reason workflow
- [ ] Audit log, calculation revisions, factor snapshots immutable under later factor edits
- [ ] Add backend unit/integration tests for calculation, RBAC, isolation, double counting and invalid data
- [ ] Reproducible database migrations (Flyway/Liquibase) rather than Hibernate `update`
- [ ] Role-specific demo accounts verified end-to-end; remove hardcoded demo auth from frontend before production

## Delivery / submission
- [x] Postman collection starter included (import and extend with tested responses)
- [x] Data references, assumptions and ER draft included
- [ ] Execute every Postman request and attach actual test evidence
- [ ] Deploy backend, frontend and persistent database; verify public URLs
- [ ] Create separate GitHub repositories if the assignment requires separate repos
- [ ] Add screenshots from the actual running application
- [ ] Final reviewer guide with verified setup/deployment and tested credentials

**Do not submit this starter as if all unchecked requirements are complete.** It is a working direction and documentation pack. We must implement and test the unchecked items against the PDF before claiming completion.
