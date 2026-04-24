# 🏥 Medical Health Analyzer

A full-stack health analysis system built with **Spring Boot 3**, **MySQL**, and **vanilla HTML/CSS/JavaScript**.

## Features

- ✅ User Registration & Login with **BCrypt** password hashing + **JWT** authentication
- ✅ Dashboard with color-coded health parameter cards (Low/Normal/High)
- ✅ Manual lab input for 11 blood parameters + height & weight
- ✅ Smart analysis: status, risk level, and percentage difference from normal range
- ✅ **BMI Calculator** with category classification
- ✅ **Health Score** out of 100
- ✅ 🇮🇳 Indian diet, exercise, and lifestyle recommendations
- ✅ **Chart.js** interactive graphs (bar + line overlay)
- ✅ **PDF report export** (jsPDF)
- ✅ **Dark mode** toggle
- ✅ Analysis history with date tracking
- ✅ Medical disclaimer
- ✅ Fully responsive design (mobile-friendly)

## Tech Stack

| Layer    | Technology                      |
|----------|---------------------------------|
| Frontend | HTML5, CSS3, JavaScript (ES6+)  |
| Charts   | Chart.js 4                      |
| PDF      | jsPDF                           |
| Backend  | Spring Boot 3.2.5, Java 17      |
| Security | Spring Security + JWT (jjwt)    |
| Database | MySQL 8+ with JPA/Hibernate     |
| Build    | Maven                           |

## REST API Endpoints

| Method | Endpoint        | Auth     | Description            |
|--------|-----------------|----------|------------------------|
| POST   | `/api/register` | Public   | Register new user      |
| POST   | `/api/login`    | Public   | Login, returns JWT     |
| POST   | `/api/analyze`  | Bearer   | Submit lab values       |
| GET    | `/api/history`  | Bearer   | Get analysis history   |

## Project Structure

```
medical-health-analyzer/
├── pom.xml
├── README.md
├── src/main/
│   ├── java/com/healthanalyzer/
│   │   ├── MedicalHealthAnalyzerApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── AnalysisController.java
│   │   ├── dto/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   ├── LabInputRequest.java
│   │   │   └── AnalysisResponse.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   └── AnalysisRecord.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── AnalysisRecordRepository.java
│   │   ├── security/
│   │   │   ├── JwtUtils.java
│   │   │   └── JwtAuthFilter.java
│   │   └── service/
│   │       ├── AuthService.java
│   │       └── AnalysisService.java
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── login.html
│           ├── register.html
│           ├── dashboard.html
│           ├── css/style.css
│           └── js/dashboard.js
```

## Setup Instructions

### Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **MySQL 8+**

### 1. Clone / Extract the Project

Extract the downloaded zip to your workspace.

### 2. Create MySQL Database

```sql
CREATE DATABASE health_analyzer_db;
```

### 3. Configure Database Credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

> Tables are auto-created by JPA (`ddl-auto=update`).

### 4. Run the Application

#### Using Maven (Terminal):
```bash
cd medical-health-analyzer
./mvnw spring-boot:run
```

#### Using IntelliJ IDEA:
1. Open project → File > Open > select the folder
2. Wait for Maven dependencies to download
3. Run `MedicalHealthAnalyzerApplication.java`

#### Using VS Code:
1. Install "Extension Pack for Java" and "Spring Boot Extension Pack"
2. Open the folder
3. Click ▶️ Run above the `main` method

### 5. Open in Browser

Navigate to **http://localhost:8080**

## Lab Parameters & Normal Ranges

| Parameter    | Normal Range        | Unit        |
|-------------|--------------------:|-------------|
| Hemoglobin  | 12.0 – 17.5        | g/dL        |
| RBC         | 4.5 – 5.5          | million/µL  |
| WBC         | 4,000 – 11,000     | cells/µL    |
| Platelets   | 150,000 – 400,000  | cells/µL    |
| Blood Sugar | 70 – 100           | mg/dL       |
| Vitamin D   | 30 – 100           | ng/mL       |
| Vitamin B12 | 200 – 900          | pg/mL       |
| Iron        | 60 – 170           | µg/dL       |
| Calcium     | 8.5 – 10.5         | mg/dL       |
| Cholesterol | < 200              | mg/dL       |
| TSH         | 0.4 – 4.0          | mIU/L       |

## ⚠️ Disclaimer

This application is for **educational and informational purposes only**. It does not provide medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional.

## License

MIT
