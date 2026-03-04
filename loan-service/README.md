# Loan Evaluation Service — RBIH Take Home Assignment

A Spring Boot REST service that evaluates loan applications and determines whether a loan offer can be approved.

## Tech Stack
- Java 17
- Spring Boot 3.2.4
- Spring Data JPA + H2 (in-memory)
- Bean Validation (Jakarta)
- Lombok
- JUnit 5 + AssertJ

## How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Start the application
```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

### Run tests
```bash
mvn test
```

---

## API Usage

### POST /applications

**Approved example:**
```bash
curl -X POST http://localhost:8080/applications \
  -H "Content-Type: application/json" \
  -d '{
    "applicant": {
      "name": "John Doe",
      "age": 30,
      "monthlyIncome": 75000,
      "employmentType": "SALARIED",
      "creditScore": 720
    },
    "loan": {
      "amount": 500000,
      "tenureMonths": 36,
      "purpose": "PERSONAL"
    }
  }'
```

**Rejected example (low credit score):**
```bash
curl -X POST http://localhost:8080/applications \
  -H "Content-Type: application/json" \
  -d '{
    "applicant": {
      "name": "Jane Doe",
      "age": 30,
      "monthlyIncome": 75000,
      "employmentType": "SALARIED",
      "creditScore": 550
    },
    "loan": {
      "amount": 500000,
      "tenureMonths": 36,
      "purpose": "HOME"
    }
  }'
```

---

## Project Structure

```
src/main/java/com/rbih/loanservice/
├── LoanServiceApplication.java
├── config/
│   ├── LmsIdGenerator.java
│   └── LmsIdProperties.java
├── controller/
│   └── LoanApplicationController.java
├── service/
│   ├── LoanApplicationService.java       ← main orchestrator
│   ├── RiskClassificationService.java
│   ├── InterestRateService.java
│   ├── EmiCalculationService.java
│   └── EligibilityService.java
├── domain/
│   ├── EligibilityResult.java
│   ├── entity/
│   │   └── LoanApplication.java
│   └── enums/
│       ├── ApplicationStatus.java
│       ├── EmploymentType.java
│       ├── LoanPurpose.java
│       ├── RejectionReason.java
│       └── RiskBand.java
├── dto/
│   ├── request/
│   │   ├── ApplicantDTO.java
│   │   ├── LoanDTO.java
│   │   └── LoanApplicationRequest.java
│   └── response/
│       ├── LoanApplicationResponse.java
│       └── OfferDTO.java
├── repository/
│   └── LoanApplicationRepository.java
└── exception/
    └── GlobalExceptionHandler.java
```

## H2 Console
Available at `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:loandb`
