# Insurance Project Management System

Welcome to the **Insurance Project Management System**! This repository hosts the backend services for a comprehensive insurance management platform. Built with a focus on scalability, maintainability, and security, it provides a robust set of RESTful APIs to handle policies, claims, user management, and seamless third-party integrations.

---

## 🏗 Project Architecture

This application is built using a **Monolithic Layered Architecture** powered by **Java 17** and **Spring Boot**. 

If you are a technical recruiter or engineering manager reviewing this project, here is a high-level breakdown of how the system is designed, structured, and the technical decisions made:

### 1. Presentation Layer (`/controller`)
The entry point for all client requests. We expose **RESTful APIs** that adhere to standard HTTP conventions. 
- Controllers never handle business logic directly.
- They consume and return **Data Transfer Objects (DTOs)** (`/dto`), ensuring our internal database models (`/model`) are never directly exposed to the outside world. This provides a clear contract and prevents over-posting attacks.

### 2. Business Logic Layer (`/service`)
The heart of the application. 
- All core business rules and validations reside here.
- Services are built using interfaces and implementations, promoting loose coupling and making the code highly testable (e.g., easy to mock dependencies during unit testing).

### 3. Data Access Layer (`/repository` & `/model`)
We use **Spring Data JPA (Hibernate)** for object-relational mapping to interact with a **MySQL** relational database.
- The `/model` package contains JPA Entities that perfectly map to our database schemas.
- The `/repository` interfaces abstract away complex SQL queries, providing clean, out-of-the-box CRUD operations and custom derived queries.

### 4. Security & Authentication (`/security`)
Security is treated as a first-class citizen. We use **Spring Security** configured for **Stateless Sessions**.
- **JWT (JSON Web Tokens)** are used for authentication and authorization.
- Every incoming request is intercepted by custom security filters that validate the JWT signature, ensuring only authenticated and authorized users can access sensitive endpoints (like processing claims or viewing policy details).

### 5. Cross-Cutting Concerns & Integrations
To provide a complete product experience, the application integrates with several industry-standard tools:
- **Cloud Storage (Cloudinary)**: We integrated the `cloudinary-http44` SDK to securely upload, manage, and deliver user files and images (e.g., claim evidence documents) to the cloud, rather than bloating our local servers.
- **SMS Notifications (Twilio)**: Critical alerts and updates are pushed to users' mobile devices via the Twilio SDK.
- **Email Services (Spring Mail)**: Automated transactional emails are handled asynchronously to keep users informed about their policy status.
- **Exception Handling (`/exception`)**: A global `@ControllerAdvice` intercepts all runtime exceptions, mapping them to standardized, user-friendly JSON error responses.

### 6. Developer Experience & Tooling
- **API Documentation**: Integrated **SpringDoc OpenAPI (Swagger UI)**. This allows frontend developers and QA engineers to interactively explore and test the APIs without needing to dig into the code.
- **Boilerplate Reduction**: **Lombok** is heavily utilized to auto-generate getters, setters, and constructors, keeping our domain classes clean and readable.
- **Object Mapping**: **ModelMapper** handles the tedious task of converting Entities to DTOs and vice-versa.

---

## 🚀 Tech Stack Summary
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Database**: MySQL
- **Security**: Spring Security + JJWT
- **Cloud/Storage**: Cloudinary
- **Messaging/Comms**: Twilio (SMS), JavaMailSender (Email)
- **API Docs**: Swagger / OpenAPI
- **Build Tool**: Maven

## 📦 Project Structure Overview
```text
src/main/java/com/monocept/app/
├── config/       # Bean configurations (Swagger, Cloudinary, Security Beans)
├── controller/   # REST API Endpoints
├── dto/          # Data Transfer Objects for Request/Response payloads
├── enums/        # Constant enumerations (e.g., Role, PolicyStatus)
├── exception/    # Custom exceptions and Global Exception Handler
├── model/        # JPA Entities / Database tables
├── repository/   # Spring Data JPA interfaces
├── security/     # JWT filters, entry points, and custom user details
├── service/      # Business logic implementations
└── util/         # Helper classes (Email Sender, File uploaders)
```

## ⚙️ How to Run Locally

1. **Clone the repository.**
2. **Configure Environment Variables**: Set up your database credentials, JWT secret keys, Twilio credentials, and Cloudinary keys in `src/main/resources/application.properties` or `.env`.
3. **Build the project**: 
   ```bash
   ./mvnw clean install
   ```
4. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```
5. **Access API Documentation**: Once running, navigate to `http://localhost:8080/swagger-ui.html` to explore the endpoints.
