# Deploy the helpdesk

This guide puts the API on [Render](https://render.com), the database on [Neon](https://neon.tech), and the React app on [Vercel](https://vercel.com). All three have a free tier. The GitHub repository can stay private. Recruiters use the Vercel link; they do not clone the project or install PostgreSQL.

Local development is unchanged. Without the `prod` profile the API still listens on port **8081**.

## What you will set

| Where | Name | Value |
| --- | --- | --- |
| Render | `JDBC_DATABASE_URL` | Neon JDBC URL from step 1 |
| Render | `SPRING_DATASOURCE_USERNAME` | Neon role name |
| Render | `SPRING_DATASOURCE_PASSWORD` | Neon role password |
| Render | `JWT_SECRET` | Random string, at least 32 characters |
| Vercel | `VITE_API_BASE_URL` | Render URL, no trailing slash |

`JDBC_DATABASE_URL` is preferred. If it is unset, the `prod` profile uses `SPRING_DATASOURCE_URL` instead. The container listens on `PORT`, which Render sets, and falls back to **8080**.

The `prod` profile turns Hibernate `ddl-auto` to `update`, so the first boot creates the tables on an empty Neon database. Demo users are seeded on that first boot.

## 1. Create the database on Neon

1. Sign in at [https://neon.tech](https://neon.tech) and create a project. Pick the region closest to where you will host the API.
2. Open the project dashboard and select the connection details for the default branch.
3. Turn off the pooled connection. This app uses Hibernate, which needs Neon's direct host.
4. Copy the host, database name, role, and password.
5. Build a JDBC URL. Neon shows a `postgresql://` string. Spring needs the `jdbc:` form, with the user and password kept in their own variables:

```text
jdbc:postgresql://HOST/DATABASE?sslmode=require
```

Example:

```text
jdbc:postgresql://ep-example.eu-central-1.aws.neon.tech/neondb?sslmode=require
```

`sslmode=require` is required. Do not put the password in the URL.

## 2. Deploy the API on Render

1. Sign in at [https://dashboard.render.com](https://dashboard.render.com).
2. Choose **New** → **Web Service** and connect the `helpdesk-system` GitHub repository. Grant Render access if the repository is private.
3. Use these settings:

| Setting | Value |
| --- | --- |
| Name | `helpdesk-api` (any name is fine) |
| Region | Same region as the Neon project, if it is offered |
| Root Directory | `backend` |
| Runtime | Docker |
| Instance type | Free |

Render builds `backend/Dockerfile`. That image compiles with Eclipse Temurin 17 and runs the jar on `${PORT:8080}` with the `prod` profile.

4. Add the environment variables before the first deploy:

| Key | Value |
| --- | --- |
| `JDBC_DATABASE_URL` | The JDBC URL from step 1 |
| `SPRING_DATASOURCE_USERNAME` | Neon role |
| `SPRING_DATASOURCE_PASSWORD` | Neon password |
| `JWT_SECRET` | A random secret of at least 32 characters |

Generate a secret in PowerShell:

```powershell
-join ((1..32 | ForEach-Object { '{0:x2}' -f (Get-Random -Max 256) }))
```

5. Set **Health Check Path** to `/health`.
6. Create the service and wait for the deploy to finish. The first build downloads Maven and can take several minutes. The free instance sleeps after inactivity, so the first request after a pause can take about a minute while Neon and Render wake up.
7. Copy the service URL, for example `https://helpdesk-api.onrender.com`. Open `https://helpdesk-api.onrender.com/health` and confirm it returns `{"status":"UP"}`.

## 3. Deploy the frontend on Vercel

1. Sign in at [https://vercel.com](https://vercel.com) and import the same GitHub repository.
2. Set the project root to `frontend`. Vercel detects Vite. The build command is `npm run build` and the output directory is `dist`.
3. Add this environment variable **before** deploying. Vite reads it at build time:

| Key | Value |
| --- | --- |
| `VITE_API_BASE_URL` | `https://helpdesk-api.onrender.com` |

Use your Render URL. No trailing slash, and do not add `/api`. The Axios client appends `/api`, and locally it falls back to `http://localhost:8081`.

4. Deploy. `frontend/vercel.json` rewrites every path to `index.html`, so refreshing `/tickets` or any other page stays in the app.
5. Open the `https://….vercel.app` URL. CORS already allows `http://localhost:5173` and any `https://*.vercel.app` host, including preview deployments.

If you change `VITE_API_BASE_URL` later, redeploy the frontend. A new value is not picked up until the next build.

## 4. Sign in

After the API has started once against Neon, these accounts exist:

| Role | Email | Password |
| --- | --- | --- |
| Admin | admin@helpdesk.local | Admin123! |
| Agent | agent@helpdesk.local | Agent123! |
| Customer | customer@helpdesk.local | Customer123! |

The Vercel URL is the link to share. API docs are at `https://YOUR-RENDER-URL/swagger-ui.html`.
