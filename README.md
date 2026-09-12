# AI Support Platform

An AI-powered customer support platform built with **Java, Spring Boot, MySQL, React, TypeScript, JWT, and Google Gemini**.

The platform provides separate workflows for **Customers, Agents, and Administrators**, covering the complete lifecycle of customer support tickets.

## 🚀 Live Demo

**Frontend:**
https://ai-support-platform-sigma.vercel.app/

**Backend:**
https://ai-support-platform-production-f435.up.railway.app/

## ✨ Features

### Customer

* Register and login with JWT authentication
* Create, view, and update tickets
* Reopen resolved tickets
* Send and receive ticket messages

### Agent

* Agent dashboard and ticket statistics
* Claim and release tickets
* Update ticket status
* Search and filter tickets
* Customer communication
* AI-generated suggested replies
* AI-powered ticket analysis

### Admin

* System-wide dashboard
* Manage tickets and assignments
* Assign and reassign agents
* Search and filter tickets
* Manage agents and customers
* Update ticket status
* Delete users and tickets

### AI

* Ticket summarization
* Ticket category classification
* Priority suggestions
* AI-generated support replies

## 🛠️ Tech Stack

**Backend**

* Java 17
* Spring Boot 3.5.15
* Spring Security
* Spring Data JPA
* JWT
* MySQL
* Maven

**Frontend**

* React
* TypeScript
* Vite
* Tailwind CSS
* React Router
* Axios

**AI**

* Google Gemini API

**Testing**

* JUnit
* Spring Boot Test
* MockMvc
* Spring Security Test

**Deployment**

* Vercel — Frontend
* Railway — Backend
* Aiven — MySQL

## 🏗️ Architecture

```text
React + TypeScript
        │
        │ REST API
        ▼
Spring Boot
        │
   ┌────┼─────┐
   ▼    ▼     ▼
 MySQL JWT  Gemini
```

The backend follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
MySQL
```

AI functionality is abstracted through an `AiService` interface:

```text
AiController
      ↓
   AiService
      ↓
GeminiAiService
      ↓
 Gemini API
```

A mock AI implementation is used during automated testing.

## 🔐 Security

* JWT-based stateless authentication
* BCrypt password hashing
* Role-based authorization
* Request validation
* Protected API endpoints
* Centralized exception handling
* CORS configuration
* Environment-based secrets

Roles:

```text
CUSTOMER
AGENT
ADMIN
```

## 🧪 Testing

The backend includes automated tests covering:

* Authentication
* Customer ticket workflows
* Agent workflows
* Admin workflows
* Security and authorization
* AI services
* Database operations

**65 tests — 65 passing**

Run tests:

```bash
./mvnw clean test
```

Windows:

```powershell
.\mvnw.cmd clean test
```

## ☁️ Deployment

```text
              ┌─────────────┐
              │   Vercel    │
              │   React UI  │
              └──────┬──────┘
                     │
                     ▼
              ┌─────────────┐
              │   Railway   │
              │ Spring Boot │
              └──────┬──────┘
                     │
              ┌──────┴──────┐
              ▼             ▼
         ┌─────────┐   ┌─────────┐
         │  Aiven  │   │ Gemini  │
         │  MySQL  │   │   API   │
         └─────────┘   └─────────┘
```

## 💻 Run Locally

### Backend

Configure the required environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
GEMINI_API_KEY
SPRING_PROFILES_ACTIVE
```

Then run:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend:

```text
http://localhost:8080
```

### Frontend

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Frontend:

```text
http://localhost:5173
```

## 📁 Project Structure

```text
ai-support-platform/
├── src/
│   ├── main/
│   │   └── java/
│   └── test/
├── frontend/
│   ├── src/
│   └── package.json
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md
```

## 🔮 Future Improvements

* Pagination
* Advanced audit/history tracking
* Rate limiting
* Redis caching
* Background job processing
* Advanced observability
* AI response evaluation

---

**Built with Java, Spring Boot, React, MySQL, and Google Gemini.**
