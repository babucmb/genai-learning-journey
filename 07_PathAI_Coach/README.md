# PathAI Coach 🚀

> **Personal AI Career Operating System & Autonomous Career Accelerator**  
> An intelligent mobile and cloud ecosystem featuring multi-agent AI career orchestration, live job discovery (internships, freshers, apprenticeships), scheduled 9:00 AM alerts, dynamic learning roadmaps, interactive mock interview coaching, and zero-trust backend isolation.

---

## 🌟 Key Features

### 📱 Android Application (Jetpack Compose + Material 3)
- **Today Dashboard**: Dynamic daily coaching routines, personalized daily goals, daily streak tracking, and active task progress.
- **Autonomous Multi-Agent Brain**:
  - **Orchestrator Agent**: Delegates user career queries across domain-specialized agents.
  - **Curator & Roadmap Agent**: Generates custom milestone-based learning paths tailored to specific dream roles.
  - **Interviewer Agent**: Delivers real-time simulated technical and behavioral mock interview sessions with immediate actionable feedback.
  - **Verifier Agent**: Validates schema compliance, checks question bounds, and eliminates duplicate tasks.
- **Live Job Search Hub**:
  - Live filtering for internships, apprenticeships, and entry-level fresher roles.
  - Location, work mode (Remote/Hybrid/Onsite), and technology stack filters.
- **Skill Map & Competency Tracker**: Visual competency tracking to measure skill readiness against target industry profiles.
- **Interactive Practice Arena**: Practice technical questions, system design prompts, and situational judgment scenarios.
- **Notifications & Daily 9:00 AM Job Radar**: Push alert center delivering curated daily openings.
- **Zero-Key Security Architecture**: No sensitive API keys or credentials stored on device; all AI and external calls route securely through the authenticated backend proxy.

---

### 🛡️ Secure Backend (`/server`)
- **Node.js & Express REST API** with automated lifecycle hooks.
- **Multi-Provider LLM Abstraction**:
  - Google Gemini (`gemini-2.5-flash`) as primary fast inference provider.
  - Hugging Face API fallback support.
- **Firebase Admin & Firestore Integration**:
  - Secure token verification via Bearer authentication (`Authorization: Bearer <token>`).
  - Strict user data isolation enforcing UID derivation directly from verified tokens.
- **Automated Cron Job**: Scheduled 9:00 AM (Asia/Kolkata) alert service (`node-cron`) for daily job scraping and notification delivery.
- **Health Check Endpoint**: `GET /health` with real-time status and provider diagnostics.
- **Container Ready**: Production-grade Dockerfile with multi-stage build running on Alpine Linux.

---

## 🏗️ Architecture Overview

```
┌────────────────────────────────────────────────────────┐
│            Android Client (Jetpack Compose)            │
│  - Today Dashboard     - Multi-Agent Chat Panel        │
│  - Roadmap Generator   - Live Job Radar                │
│  - Mock Interviews     - Offline Fallback Support      │
└───────────────────────────┬────────────────────────────┘
                            │ HTTPS (Token-authenticated)
                            ▼
┌────────────────────────────────────────────────────────┐
│             PathAI Backend (Cloud Run / Node)          │
│  - /health              - /api/agent/chat              │
│  - /api/jobs/search     - /api/roadmap/generate        │
│  - 9:00 AM Cron Alerts  - Verifier Engine              │
└───────────────┬────────────────────────┬───────────────┘
                │                        │
       Gemini / HuggingFace API      Firebase Firestore
```

---

## 📂 Project Structure

```
├── app/                                # Android Jetpack Compose project
│   ├── src/main/java/com/example/
│   │   ├── data/                       # Models, repositories, remote API client
│   │   │   └── remote/BackendClient.kt # HTTPS backend client & fallback config
│   │   ├── ui/
│   │   │   ├── screens/                # 13 Compose screens (Today, Roadmap, Jobs, etc.)
│   │   │   ├── components/             # Reusable UI widgets, cards, bars
│   │   │   └── theme/                  # Material 3 typography, color schemes
│   │   └── viewmodel/                  # ViewModels for reactive state management
│   └── build.gradle.kts                # App Gradle config with Secrets Plugin
├── server/                             # Cloud Run / Express backend microservice
│   ├── llm/                            # Gemini & HuggingFace provider implementations
│   ├── services/                       # Agent orchestrator, verifier, job scraper
│   ├── tests/                          # Automated Node test suites (Mission 5)
│   ├── Dockerfile                      # Production container spec
│   ├── index.js                        # Express server entrypoint & routes
│   └── package.json                    # Backend dependencies & test scripts
├── .env.example                        # Reference environment configuration
├── firestore.rules                     # Firebase Firestore security rules
└── metadata.json                       # Project descriptor metadata
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Ladybug (or newer)** with Android SDK 35 support.
- **Java 17 or Java 21** configured as JDK.
- **Node.js v20+** and `npm` (for running or testing the backend locally).

---

### 1. Setting Up the Backend

1. Navigate to the `server/` directory:
   ```bash
   cd server
   npm install
   ```

2. Create a `.env` file inside `server/` (or copy from `.env.example`):
   ```env
   PORT=5000
   FIREBASE_PROJECT_ID=pathai-coach
   LLM_PROVIDER=gemini
   GEMINI_MODEL=gemini-2.5-flash
   GEMINI_API_KEY=your_gemini_api_key_here
   ```

3. Run the backend unit tests:
   ```bash
   npm test
   ```

4. Start the server locally:
   ```bash
   npm start
   ```
   Verify health status at:
   ```bash
   curl http://localhost:5000/health
   ```

---

### 2. Setting Up the Android App

1. In the project root, configure your local environment file `.env`:
   ```properties
   # For local emulator testing:
   BACKEND_BASE_URL=http://10.0.2.2:5000
   
   # Or for deployed Cloud Run backend:
   # BACKEND_BASE_URL=https://pathai-backend-xxxxx.asia-south1.run.app
   ```

2. Open the project in **Android Studio**.
3. Sync Gradle and build the project:
   ```bash
   gradle assembleDebug
   ```
4. Run the app on an Android Emulator or physical device (API 26+).

---

## ☁️ Cloud Run Deployment Guide

To deploy the backend to **Google Cloud Run**:

```bash
gcloud run deploy pathai-backend \
  --source server \
  --project pathai-coach \
  --region asia-south1 \
  --allow-unauthenticated \
  --set-env-vars FIREBASE_PROJECT_ID=pathai-coach,LLM_PROVIDER=gemini,GEMINI_MODEL=gemini-2.5-flash \
  --set-secrets GEMINI_API_KEY=GEMINI_API_KEY:latest
```

Once deployed, set the resulting service URL as `BACKEND_BASE_URL` in your app's configuration.

---

## 🧪 Testing

- **Backend Test Suite**:
  ```bash
  cd server && npm test
  ```
  Runs 11 test cases covering token rejection, UID isolation, provider routing, verifier JSON validations, duplicate task checks, and `/health` availability.

- **Android Unit Tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```

---

## 🔒 Security Best Practices

- **Zero Client-Side Secrets**: No Gemini API keys or service account credentials are built into the APK.
- **Identity Isolation**: The backend uses Firebase Authentication tokens to verify identity; user IDs are extracted directly from the verified decoded token rather than trusting payload bodies.
- **Resilient Fallback Mode**: If connectivity to the backend is interrupted during local development, the app's `allowOfflineFallback` mode maintains UI responsiveness with structured local fallbacks.

---

## 📄 License
This project is licensed under the Apache 2.0 License.
