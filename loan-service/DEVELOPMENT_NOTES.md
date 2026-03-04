# DEVELOPMENT_NOTES.md

## Overall Approach

Built a layered Spring Boot REST service following clean architecture principles:

```
Controller → Service → Domain Services → Repository
```

Each layer has a single responsibility:
- **Controller**: HTTP handling, request/response mapping
- **Service (LoanApplicationService)**: Orchestrates the evaluation pipeline
- **Domain Services**: Each handles one business concern (risk, interest rate, EMI, eligibility)
- **Repository**: Persistence via Spring Data JPA (H2 in-memory)

---

## Key Design Decisions

### 1. Separate Domain Services
Rather than putting all business logic in one "God service", I split it into focused services:
- `RiskClassificationService` — risk band from credit score
- `InterestRateService` — rate calculation with all premiums
- `EmiCalculationService` — EMI formula using BigDecimal
- `EligibilityService` — all three eligibility rules

This makes each class independently testable and easy to extend.

### 2. BigDecimal Throughout
All financial values (EMI, interest rate, income, loan amount) use `BigDecimal` with `scale=2` and `RoundingMode.HALF_UP` as required by the specifications. Higher precision (scale=10) is used during intermediate calculations to avoid rounding errors accumulating.

### 3. Two EMI Thresholds
The spec defines two different thresholds:
- **60%** — used in eligibility checks (Section 3)
- **50%** — used in offer generation (Section 6)

Both are implemented separately. A 60% check failure gives `EMI_EXCEEDS_60_PERCENT`. A 50% failure gives `EMI_EXCEEDS_50_PERCENT`. This matches the spec's intent of having stricter offer criteria than basic eligibility.

### 4. Rejection Reasons — Collect All
All eligibility checks run even if an earlier one fails. This gives the applicant all rejection reasons at once, which is better UX than failing fast and showing one reason at a time.

### 5. EligibilityResult Domain Object
Instead of returning raw booleans or throwing exceptions for business rule failures, a dedicated `EligibilityResult` value object communicates the outcome clearly and carries the rejection reasons.

### 6. Persistence for Audit
All applications (approved and rejected) are persisted to H2 with full details. Rejection reasons are stored via `@ElementCollection` for clean normalized storage.

---

## Trade-offs Considered

| Decision | Trade-off |
|---|---|
| H2 in-memory DB | Easy to run without setup, but data doesn't survive restarts. Production would use PostgreSQL/MySQL. |
| `@ElementCollection` for rejection reasons | Simple, no extra entity class needed. For complex queries, a dedicated `RejectionReason` entity would be better. |
| EMI uses final interest rate | The spec provides the EMI formula but doesn't explicitly state which rate. I used the final computed rate (with all premiums) for offer EMI, which is the most meaningful to the applicant. |
| Lombok | Reduces boilerplate significantly. Trade-off: requires IDE plugin and adds compile-time dependency. |

---

## Assumptions Made

1. **Age + Tenure**: Tenure is converted to years using `Math.ceil()` (conservative). E.g., 13 months = 2 years for this check.
2. **Credit score < 600**: Short-circuited before risk classification (since `RiskBand` only covers 600+). Still returns a proper rejected response.
3. **Loan size premium threshold**: "Loan > 10,00,000" means strictly greater than 10,00,000 (not ≥).
4. **Base rate in EMI formula**: The spec says "Base Interest Rate = 12%" for the EMI formula. I use the **final computed rate** (with all premiums) for offer EMI, as this is the actual rate the customer would pay.
5. **HTTP status**: Returns `200 OK` for both approved and rejected (both are valid business outcomes, not errors).

---

## Improvements With More Time

1. **Integration tests**: Add `@SpringBootTest` tests covering the full HTTP stack with MockMvc.
2. **Persistent database**: Replace H2 with MySQL.
3. **GET /applications/{id}**: Add an endpoint to retrieve a stored application by ID.
4. **Actuator + Health checks**: Add Spring Boot Actuator for monitoring.
5. **OpenAPI/Swagger docs**: Add `springdoc-openapi` for live API documentation.
6. **Rate limiting**: Protect the API endpoint from abuse.
7. **Structured logging**: Use MDC to correlate log lines per request using a trace ID.
8. **More granular EMI check**: The 60% vs 50% threshold logic could be made configurable via `application.properties` for easy tuning without code changes.
