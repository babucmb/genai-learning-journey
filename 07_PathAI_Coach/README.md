# PathAI Coach 🚀

> An AI-assisted learning and career companion for students and fresh graduates.

## Overview

PathAI Coach is an educational Android project exploring how Generative AI,
specialized agents, and cloud-backed user data can support technical learning
and career preparation.

The idea is to connect structured learning, daily practice, portfolio projects,
and entry-level job discovery in one application.

I developed this project to gain hands-on experience with GenAI application
architecture, agent orchestration, Firebase integration, and prompt-assisted
development. It is an evolving prototype, not a production-ready career platform.

## The Problem

Students and fresh graduates often use separate tools for learning, planning,
interview preparation, project development, and job searching.

PathAI Coach explores a connected approach:

Career goals → Skill gaps → Learning activities → Projects → Job opportunities

The aim is to help learners understand what to work on next and how it connects
to their career goals.

## Features Explored

### Personalized Learning

- Sequential learning paths for Python, AI/ML, LLM/RAG, and portfolio projects.
- Modules with prerequisites, learning objectives, and progress tracking.
- A Today screen for daily learning activities and task completion.
- Module-aware tutor explanations and quizzes.
- Quiz scores and learning progress stored per user.

### Specialized AI Agents

The project explores an orchestrator-based design with specialized roles:

- Coach Agent: study planning, priorities, and learning-task recommendations.
- Tutor Agent: concept explanations, examples, quizzes, and feedback.
- Project Agent: project ideas, milestones, and portfolio guidance.
- Research Agent: technology updates and learning-related research.
- Career Agent: career guidance and coordination of job-related workflows.
- Verifier Agent: validation of structured outputs and task constraints.

Agent screens and backend workflows are implemented at different stages of
maturity. Some interactions may use explicitly labeled offline or template
fallbacks when a live service is unavailable.

### Career Preparation

- Education, skills, and job preferences in the user profile.
- Job-discovery workflows focused on internships, fresher roles, and
  apprenticeships.
- Job filtering, matching explanations, and application tracking.
- Interview-practice and resume-assistance workflows.
- Notification interfaces and scheduled-alert experiments.

Live job availability depends on configured data sources and backend access.
Continuous monitoring and reliable notification delivery remain areas for
further validation.

### Firebase Integration

- Firebase Authentication for user accounts.
- Cloud Firestore for profiles, tasks, learning progress, and related data.
- User-scoped data access and Firestore Security Rules.
- Persistent progress across application sessions.

## Architecture

The intended request flow for AI features is:

```text
Android application
        ↓
Firebase Authentication ID token
        ↓
Node.js / Express backend
        ↓
Orchestrator and specialized agent workflows
        ↓
Model-provider abstraction
        ↓
Gemini or an optionally configured Hugging Face provider
```

Firestore supports persistent user data alongside these workflows.

The backend is designed to verify Firebase ID tokens, derive the authenticated
user identity, validate model responses, and keep model-provider credentials
outside the Android application.

A provider abstraction was introduced to make model integration easier to
change without rewriting the mobile client. Provider availability and output
quality still require separate testing.

## Technology Stack

| Area | Technologies |
|------|--------------|
| Android application | Kotlin, Jetpack Compose, Material 3 |
| Authentication | Firebase Authentication |
| Data storage | Cloud Firestore |
| Backend | Node.js, Express |
| Generative AI | Gemini, optional Hugging Face provider integration |
| Deployment preparation | Docker, Google Cloud Run |
| Development assistance | Google AI Studio |

## What I Learned

### GenAI and Agent Design

- Defining specialized agent responsibilities.
- Separating conversational responses from actions that change application state.
- Passing relevant learner context into model requests.
- Working with structured model outputs and validation.
- Understanding how orchestration differs from simply displaying multiple
  agent personas.

### Full-Stack Integration

- Connecting an Android application to Firebase and backend APIs.
- Organizing user-specific profiles, tasks, and learning progress.
- Understanding Firebase ID-token verification and UID-based access control.
- Separating mobile configuration from server-side model credentials.

### Evaluation and Reliability

One of the most valuable lessons was auditing the difference between what the
application appeared to do and what its implementation actually supported.

The audit helped identify:

- Template-generated tasks presented alongside AI features.
- Backend endpoints that were not yet called by the Android client.
- Direct client-to-model calls that needed migration.
- Background scheduling code that was not proof of a deployed scheduler.
- Fallback responses that needed clearer labeling.

This helped me understand why generated code, passing tests, and a polished UI
do not automatically establish a reliable end-to-end system.

## Development Approach

Google AI Studio supported an iterative, prompt-assisted development workflow.

I used it to explore features, generate and refine code, review implementation
summaries, and identify integration gaps through follow-up audits and testing.

The project introduced me to vibe-coding workflows while reinforcing the
importance of understanding the architecture, checking generated assumptions,
and validating real application behavior.

## Current Status and Limitations

This project is currently paused as a learning and portfolio prototype.

Implemented or explored areas include authentication, learning paths,
progress tracking, agent interfaces, backend provider abstraction, and
career-support workflows.

Important limitations:

- Seeded curriculum and practice content are included.
- Some features can use offline or template fallbacks.
- Backend deployment and end-to-end connectivity require verification.
- Background automation and push-notification delivery are not claimed as
  production-verified.
- Job coverage, freshness, and eligibility depend on external sources.
- Model responses and job recommendations may be incomplete or incorrect.
- Security-related implementation choices require continued testing and review.

## Future Improvements

- Validate the complete Android-to-backend-to-model request flow.
- Improve dynamic task generation and adaptive scheduling.
- Deploy and test reliable background workflows.
- Add verified push-notification delivery.
- Strengthen job-source provenance, freshness checks, and deduplication.
- Expand agent evaluations and failure reporting.
- Improve accessibility and learner feedback.
- Extend learning paths based on practical user testing.

## Security and Configuration

Real credentials are not included in documentation or example configuration.

Model-provider tokens, Firebase Admin credentials, and other server secrets
should remain outside the Android application and public repository.

Firebase client configuration is separate from server-side credentials.
Example files should contain placeholders rather than real configuration values.

## Why I Built This

PathAI Coach reflects my interest in Generative AI, agent-based systems, and
building useful applications while learning.

The project helped me move beyond prompting a model toward understanding the
surrounding system: authentication, persistence, orchestration, validation,
fallback behavior, deployment, and evaluation.

It is a record of hands-on exploration and continuous improvement.

## Disclaimer

PathAI Coach is intended for learning and experimentation. It does not guarantee
job eligibility, placement, assessment accuracy, or the correctness of
AI-generated recommendations.