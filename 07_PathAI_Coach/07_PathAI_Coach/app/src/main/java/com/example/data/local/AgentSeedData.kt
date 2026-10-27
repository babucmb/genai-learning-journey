package com.example.data.local

import com.example.data.model.AgentItem
import com.example.data.model.TechRadarItem

object AgentSeedData {
    val initialAgents = listOf(
        AgentItem(
            id = "coach",
            name = "Coach Agent",
            roleTitle = "Strategic Adaptive Planner",
            iconName = "Coach",
            description = "Analyzes your daily study hours, tracks roadmap velocity, and intelligently reschedules when life happens.",
            status = "ACTIVE",
            activeTask = "Monitoring Week 4 Transformer curriculum • Streak intact (4 days)",
            samplePrompts = listOf(
                "I fell behind on multi-head attention. Adapt my sprint for today.",
                "How should I distribute my 4 hours today between coding and theory?",
                "Am I on track for my 12-week AI Engineer goal?"
            )
        ),
        AgentItem(
            id = "tutor",
            name = "Tutor Agent",
            roleTitle = "Socratic Concept & Quiz Specialist",
            iconName = "Tutor",
            description = "Explains complex ML/DL concepts with analogies, tensor diagrams, intuitive code breakdowns, and quizzes.",
            status = "ACTIVE",
            activeTask = "Ready to quiz on Q-K-V projections & Causal Attention Masking",
            samplePrompts = listOf(
                "Why do we divide by sqrt(d_k) in scaled dot-product attention?",
                "Give me a 3-question rapid quiz on Vector Databases and HNSW indexing.",
                "Explain the difference between cross-entropy and contrastive loss simply."
            )
        ),
        AgentItem(
            id = "project",
            name = "Project Agent",
            roleTitle = "Architecture & Portfolio Reviewer",
            iconName = "Project",
            description = "Guides portfolio-grade capstone design, reviews GitHub architecture, suggests Docker/CI improvements, and prepares portfolio talking points.",
            status = "ACTIVE",
            activeTask = "Reviewing RAG Pipeline chunking strategy and latency tradeoffs",
            samplePrompts = listOf(
                "How do I upgrade my RAG project to make it stand out to hiring managers?",
                "Review my FastAPI model server design for high-throughput batching.",
                "What metrics should I showcase on my resume for my fine-tuning project?"
            )
        ),
        AgentItem(
            id = "research",
            name = "Research Agent",
            roleTitle = "AI/ML Tech Radar & Trends Intelligence",
            iconName = "Research",
            description = "Scans arXiv papers, AI release notes, emerging open-source frameworks, and tech hiring demand to keep your skills cutting-edge.",
            status = "ACTIVE",
            activeTask = "Indexing latest deep-dive on Reasoning Models & Speculative Decoding",
            samplePrompts = listOf(
                "What are the top 3 open-source tool trends I need to know this month?",
                "Summarize recent breakthroughs in Test-Time Compute and Reasoning models.",
                "Which vector database is dominating enterprise production right now?"
            )
        )
    )

    val initialTechRadar = listOf(
        TechRadarItem(
            id = "radar_1",
            title = "DeepSeek-R1 & Test-Time Compute Scaling",
            domain = "LLM / Reasoning",
            summary = "Reinforcement learning without supervised warm-up triggers emergent chain-of-thought behaviors and verification loops.",
            impact = "Hot Skill",
            source = "ArXiv / Open Weights",
            timeAgo = "1d ago"
        ),
        TechRadarItem(
            id = "radar_2",
            title = "Agentic Workflows: LangGraph & Multi-Agent Orchestration",
            domain = "Tooling & Infra",
            summary = "Production teams shifting from static chains to cyclical state graphs with human-in-the-loop validation and specialized sub-agents.",
            impact = "High Impact",
            source = "LangChain / Industry Trends",
            timeAgo = "2d ago"
        ),
        TechRadarItem(
            id = "radar_3",
            title = "vLLM & PagedAttention Performance Gains",
            domain = "MLOps & Inference",
            summary = "New KV cache quantization and chunked prefill reduce GPU memory fragmentation by 4x for high-concurrency model serving.",
            impact = "Trending",
            source = "vLLM Project",
            timeAgo = "3d ago"
        ),
        TechRadarItem(
            id = "radar_4",
            title = "AIML Hiring Shift: Evaluators & Guardrails over Simple Prompting",
            domain = "Job Market",
            summary = "Tier 1 tech screens increasingly test automated LLM evaluation suites (RAGAS, DeepEval) and deterministic guardrails over baseline prompts.",
            impact = "Market Demand",
            source = "Tech Recruiter Pulse",
            timeAgo = "4d ago"
        )
    )
}
