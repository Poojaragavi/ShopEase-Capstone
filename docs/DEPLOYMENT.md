# ShopEase Production Deployment Guide

This guide describes how to build, deploy, and host the **ShopEase Multi-Seller E-Commerce Application** on local, containerized, and public cloud platforms.

---

## 1. Build Verification & Packaging

To generate the production deployable WAR archive:

```bash
mvn clean package
```

- **Output Artifact**: `target/shopease.war`
- **Quality Gates**: 0 Checkstyle violations, 0 SpotBugs warnings, 32/32 tests passing.

---

## 2. Deployment Option A: Standalone Apache Tomcat 9 Server (Traditional Evaluation)

This is the standard Java EE deployment method evaluated by university faculties:

1. Download and extract **Apache Tomcat 9.0.x** ([tomcat.apache.org](https://tomcat.apache.org/download-90.cgi)).
2. Copy `target/shopease.war` to the Tomcat `webapps` folder as `ROOT.war`:
   ```bash
   cp target/shopease.war $CATALINA_HOME/webapps/ROOT.war
   ```
3. (Optional) Set custom persistent data directory in `$CATALINA_HOME/bin/setenv.sh` (or `setenv.bat` on Windows):
   ```bash
   export DB_PATH=/var/data/shopease
   export JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
   ```
4. Start Tomcat:
   ```bash
   $CATALINA_HOME/bin/startup.sh
   # On Windows:
   $CATALINA_HOME\bin\startup.bat
   ```
5. Access the application at **`http://localhost:8080/`**.

---

## 3. Deployment Option B: Containerized Docker Deployment

### Local Docker Run
```bash
# Build the production Docker image
docker build -t shopease:1.0.0 .

# Run container with persistent host volume for H2 database
docker run -d \
  -p 8080:8080 \
  -v $(pwd)/data:/app/data \
  --name shopease-server \
  shopease:1.0.0
```

### Docker Compose
```bash
docker-compose up -d
```
The `./data` folder on your host machine will hold the persistent database file `shopease.mv.db`.

---

## 4. Deployment Option C: Free Public Cloud Hosting

### 1. Deploying on Render (Recommended — 1-Click with Persistent Disk)
1. Push your repository to **GitHub**.
2. Log in to [Render.com](https://render.com) and click **New + $\to$ Blueprint**.
3. Select your GitHub repository containing [`render.yaml`](../render.yaml).
4. Render automatically configures:
   - Multi-stage Docker build from `Dockerfile`.
   - 1 GB persistent disk mounted at `/app/data` to ensure zero database loss across redeployments.
   - Health check endpoint `/api/v1/health`.
5. Your public web application will be live at `https://<your-subdomain>.onrender.com`.

### 2. Deploying on Railway
1. Go to [railway.app](https://railway.app) $\to$ **New Project $\to$ Deploy from GitHub repo**.
2. Add a persistent volume mounted at `/app/data`.
3. Add environment variable `DB_PATH=/app/data/shopease`.
4. Railway will build the `Dockerfile` and publish a public URL.

### 3. Deploying on Heroku / Fly.io / AWS Elastic Beanstalk
- **Procfile** is included in the project root:
  ```
  web: catalina.sh run
  ```
- Configure environment variable `PORT` and `DB_PATH`.

---

## 5. Production Environment Variables Reference

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | HTTP port for incoming web traffic |
| `DB_PATH` | `./data/shopease` | File path for persistent H2 database |
| `DB_URL` | `jdbc:h2:file:./data/shopease;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE` | Complete JDBC connection string |
| `AI_CHATBOT_PROVIDER` | `mock` | `mock` (domain matcher) or `gemini` (Google AI) |
| `AI_CHATBOT_GEMINI_KEY` | *(empty)* | Google Gemini API Key (if `gemini` provider is active) |
| `SESSION_TIMEOUT_SECONDS`| `1800` | Session timeout in seconds (30 minutes) |

---

## 6. Health & Diagnostics Endpoints

- **Public Health Check**: `GET /api/v1/health`
  ```json
  {
    "success": true,
    "data": {
      "status": "UP",
      "db": "UP",
      "version": "1.0.0",
      "uptimeSeconds": 120
    }
  }
  ```
- **H2 Web Console**: `GET /h2-console`
