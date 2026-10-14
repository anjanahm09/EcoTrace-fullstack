# EcoTrace reviewer guide — complete after testing

## Demo URLs
- Frontend: `ADD_DEPLOYED_FRONTEND_URL`
- Backend health: `ADD_DEPLOYED_BACKEND_URL/api/health`
- Swagger UI: `ADD_DEPLOYED_BACKEND_URL/swagger-ui.html`

## Demo credentials
List only credentials that have been created and verified on the deployed system. Never commit production secrets. Current local prototype demo accounts are defined in `backend/src/main/java/com/ecotrace/config/SecurityConfig.java`; they are in-memory development accounts, not a production identity system.

## Architecture
React + Vite + Tailwind CSS frontend → Spring Boot REST API → MySQL/JPA. OpenAPI/Swagger documents the backend endpoints. Postman collection is in `postman/`.

## Local startup
1. Install JDK 21, Maven 3.9+, Node.js 20+, and MySQL 8+.
2. Create database `ecotrace_db` in MySQL.
3. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in your local environment; do not commit real passwords.
4. From `backend/`, run `mvn spring-boot:run`.
5. From `frontend/`, run `npm install` then `npm run dev`.
6. Open the Vite URL shown in the terminal. Verify `/api/health` and `/swagger-ui.html`.
7. Run the Postman requests and record actual outcomes before claiming tests passed.

## Calculation and data caveats
Document exact factors and versions used, factor boundaries, source/license, unit conversions, Scope 2 method, exclusions, estimates, data gaps and uncertainty. Never label the demo seed factors as verified values.

## Evidence to add
- Screenshots from actual running frontend and Swagger
- Postman test report/export
- Test results for calculation formulas and access isolation
- Deployment URLs and verified role-specific credentials
- Final schema/migration and updated ER diagram
