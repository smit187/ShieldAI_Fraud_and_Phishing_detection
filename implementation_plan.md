# ShieldAI - Enterprise Fraud & Phishing Detection Platform

## Goal Description
Build a production-ready, enterprise-grade AI-powered cybersecurity assistant (ShieldAI) to detect and prevent digital scams across SMS, Email, Calls, QR Codes, URLs, and more. The platform will consist of an Android mobile app, a Spring Boot microservices backend, AI detection models (Python/TensorFlow/PyTorch), and an Admin Dashboard, architected to scale to 10M+ users.

## User Review Required
> [!WARNING]
> Building a full enterprise-scale startup product from scratch is a massive, multi-month endeavor for a large team of engineers. To deliver this effectively, we must tackle it iteratively, phase by phase. I have outlined the 10 phases below based on your request. I will provide fully functional code for each component as we progress through these phases.

> [!IMPORTANT]
> The tech stack is extensive (Spring Boot, Kotlin Jetpack Compose, Python AI Models, Kafka, Redis, PostgreSQL, Kubernetes). I propose creating a **monorepo structure** (in `C:\Users\DTAdmin\.gemini\antigravity\scratch\shieldai`) to house the Android app, Backend Microservices, AI services, and Admin Portal. This will allow us to move quickly and maintain consistency across the stack.

## Open Questions
- Do you have a preference for the exact structure of the microservices (e.g., separate repositories vs monorepo)? I recommend a monorepo for this iterative build.
- For Phase 2 (Database Design), do you want to start with a local Docker Compose setup that spins up PostgreSQL, Redis, and Kafka to support the backend development?

## Proposed Changes

### Phase 1: System Architecture & Project Setup
- **Monorepo Initialization**: Create directories for `android-app`, `backend-services`, `ai-models`, and `admin-dashboard`.
- **Backend Setup**: Initialize a Spring Boot parent project with API Gateway, Eureka Discovery, and core microservices (User Service, Scan Service, Threat Intelligence Service).
- **Android Setup**: Initialize a Kotlin Jetpack Compose project with Clean Architecture, Hilt, and Room.
- **AI Setup**: Initialize a Python FastAPI project for the AI scoring engine and model inference.

### Phase 2: Database Design
- **PostgreSQL**: Define schemas for Users, Devices, ThreatHistory, and Scan Logs.
- **Redis**: Setup caching schemas for offline/online blacklist/whitelist and rapid threat lookups.
- **Docker Compose**: Create a `docker-compose.yml` to spin up the required databases and message brokers (Kafka/RabbitMQ) locally.

### Phase 3: Backend Implementation
- Implement OAuth2/JWT authentication.
- Implement REST endpoints for scanning (SMS, URL, Email).
- Integrate Kafka for async processing of threat logs and model feedback.
- Connect to third-party APIs (Google Safe Browsing, VirusTotal).

### Phase 4: Android App Implementation
- Build Jetpack Compose UI (Dashboard, Scanner, Threat History).
- Implement background services and BroadcastReceivers for SMS, Call, and Notification scanning.
- Integrate CameraX for QR Code scanning.
- Integrate local TensorFlow Lite models for on-device inference.

### Phase 5: AI Models Implementation
- Build NLP pipelines for text classification (Spam, Phishing).
- Implement ensemble risk scoring engine.
- Integrate LangChain/LLM for AI explanations.

### Phase 6-10: Admin Dashboard, Security, Testing, Deployment, Optimization
- To be detailed and executed in subsequent iterations upon completion of core systems. 

## Verification Plan

### Automated Tests
- We will write JUnit/Mockito tests for the Spring Boot microservices and Android ViewModels.
- PyTest for the AI inference models.

### Manual Verification
- Deploy the backend using Docker Compose locally.
- Run the Android app via an emulator.
- End-to-end test: Scan a test URL and SMS from the Android app, verify it hits the API Gateway, gets processed by the AI service, and returns the correct risk score.
