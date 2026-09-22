# PathAI Coach

> An AI-powered learning companion exploring Generative AI, AI agents, adaptive learning, career guidance, and modern Android application development.

## 📌 Overview

**PathAI Coach** is a learning-focused Android application created to explore **Generative AI, AI agents, multi-agent systems, and modern application development**.

The project acts as a personal learning companion for **students and fresh graduates preparing for technology roles**. It combines structured learning paths, AI-assisted study support, career guidance, and job discovery into one experimental application.

The project is primarily intended for **learning, experimentation, and portfolio demonstration**, rather than production use.

---

## 📝 Abstract

PathAI Coach explores how multiple specialized AI agents can cooperate to support a learner.

The application includes learning paths such as:

* Python
* Artificial Intelligence & Machine Learning
* Large Language Models & RAG
* Portfolio Projects

It can help users:

* Navigate structured learning modules
* Generate daily learning activities
* Understand technical concepts through AI explanations
* Practice through quizzes
* Plan and develop portfolio projects
* Explore entry-level job opportunities
* Track learning progress

The project also explores **authenticated user experiences, cloud-backed data storage, model-provider integration, and AI-agent orchestration**.

It was developed through an iterative, prompt-assisted workflow using **Google AI Studio and Firebase**, with the goal of understanding how modern GenAI applications are designed, connected, tested, and improved.

---

## 🎯 Learning Objectives

This project was created to learn and practice:

* Generative AI application development
* Prompt engineering
* Structured model responses
* AI agent design
* Tool-oriented AI workflows
* Multi-agent systems
* Adaptive learning plans
* AI-generated task and activity creation
* Tutor agents for explanations and quizzes
* Career and job-search agent concepts
* Firebase Authentication
* User-specific cloud data management
* Firestore-based data storage
* Android application development
* Modern mobile UI development
* Separation of client-side and server-side AI services
* Model-provider integration and fallback behavior
* Prompt-assisted development and experimentation
* Iterative "vibe coding" workflows

---

# 🚀 Main Capabilities

## 📚 Learning Paths

The application organizes learning into structured learning paths, including:

* **Python Programming**
* **Artificial Intelligence & Machine Learning**
* **LLMs & Retrieval-Augmented Generation**
* **Portfolio & Practical Projects**

Each learning path can contain:

* Modules
* Prerequisites
* Learning activities
* Progress tracking
* Quizzes
* Tasks
* Learning recommendations

The goal is to provide learners with a structured path rather than relying only on open-ended AI conversations.

---

## 🤖 AI Learning Support

PathAI Coach explores AI-assisted learning through specialized agents.

### Tutor Agent

The Tutor Agent focuses on understanding and practice.

It can help with:

* Technical explanations
* Real-world examples
* Concept clarification
* Module-specific quizzes
* Practice questions
* Feedback
* Learning assistance

### Coach Agent

The Coach Agent focuses on learning organization.

It can help with:

* Creating learning activities
* Prioritizing topics
* Organizing study tasks
* Suggesting next learning steps
* Supporting daily learning routines

---

# 🧠 Multi-Agent Architecture

One of the main goals of PathAI Coach is to explore how **specialized AI agents can cooperate instead of relying on one general-purpose AI response**.

The project explores the following agent roles:

| Agent                   | Responsibility                                                  |
| ----------------------- | --------------------------------------------------------------- |
| 🧭 **Coach Agent**      | Learning plans, priorities, and study activities                |
| 👨‍🏫 **Tutor Agent**   | Explanations, examples, quizzes, and feedback                   |
| 🛠️ **Project Agent**   | Project ideas, architecture, milestones, and portfolio guidance |
| 🔬 **Research Agent**   | Technology updates and learning-related research                |
| 💼 **Career/Job Agent** | Entry-level job discovery and profile-based matching            |
| ✅ **Verifier Agent**    | Validating generated results before storage or presentation     |

This architecture is intended as an exploration of **agent specialization, orchestration, validation, and AI-assisted workflows**.

---

# 💼 Career & Job Exploration

PathAI Coach also explores AI-assisted career discovery.

Users can maintain a profile containing information such as:

* Education
* Technical skills
* Preferred roles
* Preferred locations
* Work preferences
* Experience level

The job workflow explores how AI can help:

1. Discover opportunities
2. Filter relevant roles
3. Summarize job descriptions
4. Match opportunities with user profiles
5. Organize entry-level opportunities
6. Support job-search workflows

The application focuses on opportunities such as:

* Internships
* Fresher roles
* Apprenticeships
* Graduate opportunities
* Entry-level positions

---

# 🔥 Firebase Integration

Firebase is used to explore cloud-backed application functionality.

### Authentication

Firebase Authentication is used to explore:

* User registration
* User login
* Authenticated sessions
* User-specific application experiences

### Cloud Firestore

Firestore is explored for storing:

* User profiles
* Learning progress
* Learning tasks
* Notifications
* Job-match information
* Application-related data

### Security

The project also explores:

* Firestore security rules
* User-specific data access
* Authentication-based authorization
* Separation of user data

---

# 🛠️ Development Approach

PathAI Coach was developed incrementally.

The project started with a basic learning-planner concept and was gradually expanded with:

* Authentication
* Structured learning paths
* AI agent panels
* Technology updates
* Job discovery
* Career guidance
* Progress tracking
* Cloud-backed data

## Prompt-Assisted Development

**Google AI Studio** was used as a prompt-assisted development and experimentation tool.

The development workflow generally followed:

```text
Idea
  ↓
Describe Feature
  ↓
Generate / Modify Implementation
  ↓
Review Generated Changes
  ↓
Run & Test
  ↓
Identify Issues
  ↓
Refine Through Prompts
  ↓
Repeat
```

This workflow helped explore **vibe coding** — describing functionality using natural language, reviewing generated implementation, testing the result, and iteratively improving the application.

The generated code and behavior were reviewed through:

* Application testing
* Backend checks
* Data-model validation
* Authentication testing
* Feature testing
* Iterative improvements

The goal was to use AI-assisted development as a **learning methodology**, rather than treating it as a replacement for understanding the underlying architecture.

---

# 🧰 Technology Areas Explored

### Artificial Intelligence

* Generative AI
* Gemini model integration
* Large Language Models
* AI Agents
* Multi-Agent Systems
* Agent Orchestration
* Prompt Engineering
* Structured Outputs
* Retrieval-Augmented Generation
* Knowledge-assisted workflows

### Mobile Development

* Android Application Development
* Modern Android UI
* Client-side application architecture

### Backend & Cloud

* Firebase Authentication
* Cloud Firestore
* Firestore Security Rules
* Backend API Design
* Cloud Deployment Concepts

### AI Application Engineering

* Model-provider integration
* AI response validation
* Fallback handling
* Agent workflows
* Job-data filtering
* Notification workflows
* Testing and observability

---

# 📂 High-Level Project Flow

```text
                    ┌──────────────────────┐
                    │      User / Learner  │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │   PathAI Coach App   │
                    │      Android UI      │
                    └──────────┬───────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
        Learning Paths     AI Agents       Career / Jobs
              │                │                │
              │        ┌───────┴────────┐       │
              │        │                │       │
              │        ▼                ▼       │
              │      Tutor           Coach      │
              │        │                │       │
              │        ├── Project ─────┤       │
              │        ├── Research ────┤       │
              │        ├── Career ──────┤       │
              │        └── Verifier ────┘       │
              │                │                │
              └────────────────┼────────────────┘
                               ▼
                    ┌──────────────────────┐
                    │ Firebase / Firestore │
                    │ Authentication      │
                    │ User Data           │
                    │ Progress            │
                    │ Tasks & Notifications│
                    └──────────────────────┘
```

---

# 📊 Project Status

**Status:** 🚧 Educational / Experimental

PathAI Coach is an ongoing learning and experimentation project.

Some features may currently use:

* Seeded curriculum content
* Mock data
* Fallback responses
* Provider-specific integrations
* Experimental AI workflows

The project may be extended in the future with:

* More learning modules
* Better AI-agent evaluations
* Additional agent tools
* Improved validation
* More reliable external-data integrations
* Enhanced personalization
* Improved career workflows
* Better observability and testing

---

# 💡 What This Project Demonstrates

PathAI Coach demonstrates hands-on exploration of how modern AI technologies can be combined into a practical application.

The project brings together:

```text
Generative AI
      +
AI Agents
      +
Multi-Agent Workflows
      +
Learning Systems
      +
Firebase
      +
Android
      +
Career Assistance
```

It reflects an interest in understanding how **AI models, specialized agents, authentication, cloud data, and mobile interfaces can work together** to solve practical problems.

More importantly, the project follows a continuous-learning approach:

> **Learn → Build → Test → Identify Limitations → Improve → Repeat**

Each feature provides an opportunity to understand both the capabilities and limitations of AI-assisted application development.

---

# ⚠️ Disclaimer

PathAI Coach is built for **learning, experimentation, and portfolio demonstration**.

It should **not** be treated as:

* A guaranteed job recommendation system
* An official career service
* Professional career advice
* A professional education platform
* A production-grade recruitment platform

Job information, AI-generated recommendations, learning content, and other outputs should be independently verified before making important decisions.

---

# 👨‍💻 Project Purpose

The primary purpose of PathAI Coach is to explore and demonstrate practical skills in:

**Generative AI • AI Agents • RAG • Android Development • Firebase • Cloud Applications • Prompt Engineering • AI-Assisted Development**

The project represents an ongoing journey of learning, experimentation, and building with modern AI technologies.
