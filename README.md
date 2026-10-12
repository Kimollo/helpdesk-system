# Helpdesk

Support desk for customers, agents, and administrators. The API never returns JPA entities. Controllers accept and return request and response DTOs, and services own the workflow rules.

## Live demo

Open the desk in the browser: [https://helpdesk-system-snowy.vercel.app](https://helpdesk-system-snowy.vercel.app)

Sign in with one of the demo accounts below. The API is [https://helpdesk-api-wo8z.onrender.com](https://helpdesk-api-wo8z.onrender.com). On the free Render plan the API sleeps when idle, so the first login can take about a minute while it wakes up.

## Stack

- Backend: Java 21, Spring Boot 3.5, Spring Data JPA, Spring Security 6, PostgreSQL, JJWT, SpringDoc
- Frontend: React 18, Vite, TypeScript, Tailwind CSS, Axios

Roles are `CUSTOMER`, `AGENT`, and `ADMIN`. Authentication is a stateless JWT bearer token. Customers can only read tickets they opened.

## Demo accounts

Seeded on first startup when `app.seed.enabled` is true (the local default).

| Role | Email | Password |
| --- | --- | --- |
| Admin | admin@helpdesk.local | Admin123! |
| Agent | agent@helpdesk.local | Agent123! |
| Customer | customer@helpdesk.local | Customer123! |

## Run locally

PostgreSQL must already have a database named `helpdesk` owned by the user `helpdesk`. The API listens on port **8081**. The Vite dev server listens on port **5173** and proxies `/api` to the API.

The default Spring profile is `dev`, which lets Hibernate update the schema. Any other profile validates the schema and does not change it.

```bash
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell, use `mvnw.cmd spring-boot:run` from `backend`.

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173.
