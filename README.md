# Wallet Service

This project is a digital wallet management service that allows operations such as wallet creation, deposits, withdrawals, transfers, and balance inquiries. The service is built with Spring Boot and uses PostgreSQL as the primary database.

## Technologies Used
  - **Java 21**
  - **Spring Boot 3.1.1**
  - **Spring Data JPA**
  - **Spring Security**
  - **PostgreSQL**
  - **H2 Database (for testing)**
  - **ModelMapper**
  - **SpringDoc OpenAPI (for API documentation)**
  - **Lombok**
  - **JUnit 5**
  - **Mockito** 
  - **Docker**
  - **Maven**

## Getting Started

### Prerequisites

- **Java 21**: Ensure you have Java 21 installed on your machine.
- **Docker**: Required to run the PostgreSQL database in a container.
- **Maven**: Used for building and managing the project.

### Setting Up the Environment

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-repo/wallet-service.git
   cd wallet-service'

### Start PostgreSQL with Docker:
    docker-compose up -d

### Configure the Application:
Update the application.yml file with the correct database credentials if needed.

### Build and Run the Application:
    mvn clean install
    mvn spring-boot:run
 
### Access the Application:
The application will be available at http://localhost:8080.

### API Documentation
The API documentation is automatically generated using SpringDoc OpenAPI. You can access it at:

    Swagger UI: http://localhost:8080/swagger-ui.html

    OpenAPI JSON: http://localhost:8080/v3/api-docs

### Running Tests
To run the unit and integration tests, use the following command:

    mvn test

### DOCKER RUN
    docker-compose up -d
 
### Contributing
Contributions are welcome! Please follow these steps:

Fork the repository.

- **Create a new branch for your feature or bugfix.**

- **Commit your changes and push to the branch.**

- **Submit a pull request.**

License
This project is licensed under the MIT License. See the LICENSE file for details.