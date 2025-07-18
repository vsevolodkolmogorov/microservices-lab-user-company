# Test Task Multi-Service User & Company System 
This is a microservices-based Java application designed to manage **users** and **companies**, using **Spring Boot**, **Spring Cloud (Netflix Eureka, Config, Gateway)**, and **PostgreSQL**. Each service interacts via REST and shares data using Feign clients. The system is containerized using Docker and orchestrated with Docker Compose.

---

## 🧩 Architecture Overview

This project consists of the following services:

| Service          | Description |
|------------------|-------------|
| **config-service** | Centralized Spring Cloud Config Server |
| **eureka-server** | Service registry and discovery |
| **gateway-service** | API Gateway for routing and load balancing |
| **user-service** | Manages users and communicates with company-service |
| **company-service** | Manages companies and communicates with user-service |
| **PostgreSQL** | Database used by both company-service and user-service |

---

## 🔧 Technologies

- Java 21 (or latest stable)
- Spring Boot 3+
- Spring Cloud (Config, Eureka, Gateway, OpenFeign)
- PostgreSQL
- Docker & Docker Compose
- Lombok
- MapStruct
- Gradle

---

## 🚀 Features

### user-service
- CRUD operations on users
- Fetch users with full company details (via Feign)

### company-service
- CRUD operations on companies
- Fetch companies with full list of user details (via Feign)

---

## ▶️ How to Run

### Just information about .env ( local or docker )

My project uses spring profiles to run. 
I used it.env from which the necessary data is obtained using the env files plugin.

You can install this plugin in IDEA by going to:
##### File -> Settings -> Plugins -> .envfiles

My .env docker or local files contains:
```bash
DB_USERNAME=yourNameDataDataBase
DB_PASSWORD=yourPasswordDataBase

CONFIG_SERVER_URL=http://config-service:8888 or http://localhost:8888
SPRING_PROFILES_ACTIVE=docker or local
```
#### WARNING! 
##### user-service and company-service have same DB_USERNAME and DB_PASSWORD


### 1. 🐳 Docker-Based Launch

This is the recommended way to run the full system.

#### Requirements:
- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

#### Run for all project
```bash
mvn clean package -DskipTests
```

#### 1. The project root requires an .env file to run in the docker profile.

#### 2. Run the full stack:
```bash
docker-compose up --build
```
#### 3. Wait for the services to initialize in the following order

#### 4. Available Endpoints via Gateway:
- http://localhost:8080/api/users
- http://localhost:8080/api/company

You can test endpoints using Postman or curl.

### 2. 💻 Local Development (without Docker)

You can run services individually using your IDE (IntelliJ recommended):

#### Steps:
1. The project should have an env folder in which it is necessary to have an .env.local file to run in the local profile.
2. Check config-service all -local.properties (change url if you need) 
3. Make sure PostgreSQL is running locally on localhost:5432.
4. Start config-service first.
5. Start eureka-server.
6. Start gateway-service.
7. Start user-service and company-service.
