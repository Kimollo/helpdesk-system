# Enterprise Helpdesk System

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Vercel-black?style=for-the-badge&logo=vercel)](https://helpdesk-system-snowy.vercel.app)
[![Backend API](https://img.shields.io/badge/API-Render-46E3B7?style=for-the-badge&logo=render)](https://helpdesk-api-wo8z.onrender.com/health)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=for-the-badge&logo=springboot)](https://spring.io)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169E1?style=for-the-badge&logo=postgresql)](https://neon.tech)

A full-stack support desk system designed for customers, support agents, and system administrators. 

The architecture strictly decouples persistence from presentation: controllers accept and return request/response **DTOs**, services govern workflow and validation rules, and JPA entities are never exposed across the network boundary.

---

## 🌐 Live Demo & Credentials

* **Frontend Application:** [https://helpdesk-system-snowy.vercel.app](https://helpdesk-system-snowy.vercel.app)
* **Backend API Base:** `https://helpdesk-api-wo8z.onrender.com` *(Returns 401 Unauthorized directly in browser without a Bearer token)*

> **Note on Render Free Tier:** The backend runs on Render's free tier and spins down when idle. The first sign-in request may take 30–60 seconds while the container initializes. Subsequent requests respond immediately.

### Pre-Seeded Demo Accounts

| Role | Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@helpdesk.local` | `Admin123!` | Full queue access, user management, status overrides |
| **Agent** | `agent@helpdesk.local` | `Agent123!` | Ticket assignment, queue processing, status transitions |
| **Customer** | `customer@helpdesk.local` | `Customer123!` | Open tickets, view personal history |

---

## 🛠️ Tech Stack & Architecture

* **Backend:** Java 17/21, Spring Boot 3.5, Spring Data JPA, Spring Security 6, JJWT, SpringDoc OpenAPI, PostgreSQL
* **Frontend:** React 18, Vite, TypeScript, Tailwind CSS, Lucide React, Axios
* **Security:** Stateless JWT authentication with role authorization (`ROLE_CUSTOMER`, `ROLE_AGENT`, `ROLE_ADMIN`). Customers are scoped strictly to tickets they originated.
* **Database Management:** Hibernate updates the schema in the dev and production profiles. The base configuration validates the schema and does not change it.

---

## 💻 Local Setup

### Prerequisites
* JDK 17 or higher
* Node.js 18+
* PostgreSQL running locally on port `5432` with a database named `helpdesk` owned by user `helpdesk`

### 1. Backend Service
```bash
cd backend
# Linux / macOS
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw.cmd spring-boot:run
```
