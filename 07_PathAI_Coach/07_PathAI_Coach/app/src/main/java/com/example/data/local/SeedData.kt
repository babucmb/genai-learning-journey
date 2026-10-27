package com.example.data.local

import com.example.data.model.*

object SeedData {
    val initialProfile = UserProfile(
        id = 1,
        name = "Alex Rivera",
        targetRole = "AIML & GenAI Engineer",
        targetCompanyTier = "Top Tier Tech & AI Labs",
        deadlineWeeks = 12,
        hoursPerDay = 4,
        skillsText = "Python, NumPy, Pandas, Scikit-Learn, basic PyTorch, Git, CS Fundamentals",
        resumeSummary = "CS graduate with academic project building a ResNet-18 image classifier and a FastAPI backend. Looking to break into AI/ML engineering with a focus on LLM applications and DSA mastery.",
        currentStreak = 4,
        longestStreak = 9,
        lastActiveEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24),
        isOnboarded = true
    )

    val initialSkillGaps = listOf(
        SkillGap(
            category = "LLM / RAG",
            skillName = "RAG Architectures & Vector DBs",
            priority = "HIGH",
            currentLevel = "Beginner",
            targetRequirement = "End-to-end RAG with semantic search, reranking, and hybrid retrieval (Milvus/Pinecone/Chroma)",
            recommendedAction = "Build a multi-document RAG assistant with LangChain/LlamaIndex and evaluate chunking strategies.",
            gapDescription = "Target AIML roles test vector search indexing, latency tradeoffs, and hallucination mitigation."
        ),
        SkillGap(
            category = "Machine Learning",
            skillName = "Deep Learning & Transformer Mechanics",
            priority = "HIGH",
            currentLevel = "Intermediate",
            targetRequirement = "Multi-head Self-Attention, Positional Embeddings, KV Caching, and FlashAttention intuition",
            recommendedAction = "Implement attention mechanism from scratch in PyTorch without high-level nn.MultiheadAttention.",
            gapDescription = "Core architectural questions in fresher interviews test math and tensor shapes."
        ),
        SkillGap(
            category = "DSA",
            skillName = "Graph Algorithms & Dynamic Programming",
            priority = "HIGH",
            currentLevel = "Intermediate",
            targetRequirement = "BFS/DFS, Dijkstra, Topological Sort, 1D/2D DP memoization and tabulation",
            recommendedAction = "Solve 20 high-frequency LeetCode graph and DP problems in Python.",
            gapDescription = "Screening rounds at FAANG/Tier 1 heavily weight graph traversals and DP states."
        ),
        SkillGap(
            category = "MLOps & System Design",
            skillName = "Model Serving & Dockerization",
            priority = "MEDIUM",
            currentLevel = "Beginner",
            targetRequirement = "Containerization, Triton / ONNX runtime, asynchronous FastAPI streaming, and latency metrics",
            recommendedAction = "Dockerize a quantized HuggingFace model server with Prometheus metrics.",
            gapDescription = "Production readiness differentiates junior applicants from average freshers."
        ),
        SkillGap(
            category = "SQL & Data",
            skillName = "Analytical SQL & Window Functions",
            priority = "MEDIUM",
            currentLevel = "Intermediate",
            targetRequirement = "RANK, DENSE_RANK, LAG/LEAD, CTEs, and query indexing execution plans",
            recommendedAction = "Practice 15 window-function challenges on PostgreSQL.",
            gapDescription = "Data modeling and SQL round is a gating prerequisite in machine learning interviews."
        )
    )

    val initialRoadmapWeeks = listOf(
        RoadmapWeek(
            weekNumber = 1,
            title = "Week 1: Algorithmic Foundations & Vector Math",
            focusDomain = "DSA & Math",
            summary = "Array two-pointer techniques, HashMaps, Matrix operations, Eigenvalues, and Gradient Descent math.",
            keyMilestone = "Solve 15 LeetCode Mediums + Implement SGD from scratch",
            isCompleted = true
        ),
        RoadmapWeek(
            weekNumber = 2,
            title = "Week 2: Trees, Graphs & Classical ML Algorithms",
            focusDomain = "DSA & Classical ML",
            summary = "Binary Trees, BFS/DFS, Random Forests, Decision Trees, Gradient Boosting (XGBoost/LightGBM).",
            keyMilestone = "Pass ML coding interview simulator on Trees & Ensembles",
            isCompleted = true
        ),
        RoadmapWeek(
            weekNumber = 3,
            title = "Week 3: Deep Learning Architectures & PyTorch",
            focusDomain = "Deep Learning",
            summary = "Backpropagation dynamics, Convolution, BatchNorm, Residual connections, Custom PyTorch Datasets.",
            keyMilestone = "Train custom ResNet & calculate training loss curves",
            isCompleted = false
        ),
        RoadmapWeek(
            weekNumber = 4,
            title = "Week 4: Transformer Architecture & Attention Deep Dive",
            focusDomain = "LLM / Transformers",
            summary = "Query-Key-Value math, causal masking, positional encodings, RoPE, and tokenizer tokenization algorithms.",
            keyMilestone = "Implement nanoGPT from scratch in 200 lines of PyTorch",
            isCompleted = false
        ),
        RoadmapWeek(
            weekNumber = 5,
            title = "Week 5: Modern RAG Systems & Vector Databases",
            focusDomain = "RAG & GenAI",
            summary = "Dense embeddings, cosine distance, HNSW indexing, chunking trade-offs, BM25 hybrid search, and rerankers.",
            keyMilestone = "Deploy enterprise document Q&A engine with Chroma & FastAPI",
            isCompleted = false
        ),
        RoadmapWeek(
            weekNumber = 6,
            title = "Week 6: Fine-Tuning LLMs (PEFT, LoRA & QLoRA)",
            focusDomain = "GenAI Fine-Tuning",
            summary = "Parameter Efficient Fine Tuning (LoRA rank & alpha), 4-bit quantization, HuggingFace TRL & SFTTrainer.",
            keyMilestone = "Fine-tune 3B model on domain specific Q&A dataset",
            isCompleted = false
        ),
        RoadmapWeek(
            weekNumber = 7,
            title = "Week 7: ML System Design & Latency Optimization",
            focusDomain = "System Design & MLOps",
            summary = "KV cache memory sizing, vLLM / Ollama serving, batching strategies, Redis caching, streaming responses.",
            keyMilestone = "Design real-time recommendation & LLM copilot system",
            isCompleted = false
        ),
        RoadmapWeek(
            weekNumber = 8,
            title = "Week 8: SQL Mastery & Data Pipeline Architecture",
            focusDomain = "SQL & Data Engineering",
            summary = "Window functions, partitioning, subqueries, PostgreSQL query optimization, and Airflow orchestration.",
            keyMilestone = "Complete 20 advanced SQL interview challenges",
            isCompleted = false
        ),
        RoadmapWeek(
            weekNumber = 9,
            title = "Week 9: Capstone Portfolio Project Polish",
            focusDomain = "Portfolio & GitHub",
            summary = "Production-grade README, Docker Compose setup, live demo deployment, benchmarking latency vs accuracy.",
            keyMilestone = "Live hosted portfolio project with demo video",
            isCompleted = false
        ),
        RoadmapWeek(
            weekNumber = 10,
            title = "Week 10: Mock Technical & Coding Interviews",
            focusDomain = "Interview Sprints",
            summary = "Timed live coding on LeetCode Medium/Hard, ML theory Q&A, behavioral STAR responses for tech leads.",
            keyMilestone = "Achieve 85%+ score in 5 mock technical interviews",
            isCompleted = false
        ),
        RoadmapWeek(
            weekNumber = 11,
            title = "Week 11: Targeted Job Applications & Referral Blitz",
            focusDomain = "Job Applications",
            summary = "Tailor resumes for 25 target job descriptions, outreach to alumni & engineering managers on LinkedIn.",
            keyMilestone = "Submit 25 tailored applications with 5 referral requests",
            isCompleted = false
        ),
        RoadmapWeek(
            weekNumber = 12,
            title = "Week 12: Offer Negotiation & Final Rounds",
            focusDomain = "Offer & Closes",
            summary = "Onsite loop preparation, system design defense, compensation benchmarking and offer evaluation.",
            keyMilestone = "Secure first offer in AIML engineering",
            isCompleted = false
        )
    )

    val initialTasks = listOf(
        TaskItem(
            weekNumber = 3,
            dayNumber = 1,
            title = "Implement Multi-Head Self-Attention from Scratch",
            description = "Write a PyTorch module `class MultiHeadAttention(nn.Module)` computing Scaled Dot-Product Attention with batching.",
            deliverable = "Passing pytest checking output tensor shape matches (B, T, D) and attention weights sum to 1.0.",
            category = "ML",
            estimatedMinutes = 90,
            isCompleted = true,
            isMissed = false,
            dueDateText = "Today"
        ),
        TaskItem(
            weekNumber = 3,
            dayNumber = 1,
            title = "Solve LeetCode #102: Binary Tree Level Order Traversal",
            description = "Use queue-based BFS to return list of level nodes. Time complexity O(N), space complexity O(N).",
            deliverable = "Accepted submission in Python with clean variable names and docstring.",
            category = "DSA",
            estimatedMinutes = 45,
            isCompleted = true,
            isMissed = false,
            dueDateText = "Today"
        ),
        TaskItem(
            weekNumber = 3,
            dayNumber = 1,
            title = "Study RoPE (Rotary Position Embeddings) Math",
            description = "Understand why RoPE allows relative position encoding via complex rotation matrices in 2D chunks.",
            deliverable = "Write 5-bullet summary in notes and sketch the 2D rotation matrix formula.",
            category = "LLM",
            estimatedMinutes = 45,
            isCompleted = false,
            isMissed = false,
            dueDateText = "Today"
        ),
        TaskItem(
            weekNumber = 3,
            dayNumber = 1,
            title = "Complete 10-Question Transformer Mechanics Quiz",
            description = "Review cross-entropy loss, temperature sampling, and key-value cache sizing formulas.",
            deliverable = "Score 80%+ on PathAI Practice Quiz.",
            category = "INTERVIEW",
            estimatedMinutes = 30,
            isCompleted = false,
            isMissed = false,
            dueDateText = "Today"
        ),
        TaskItem(
            weekNumber = 3,
            dayNumber = 2,
            title = "Solve LeetCode #200: Number of Islands",
            description = "Implement grid traversal with visited matrix or in-place marking using DFS recursion.",
            deliverable = "Accepted solution with O(M*N) time analysis.",
            category = "DSA",
            estimatedMinutes = 45,
            isCompleted = false,
            isMissed = false,
            dueDateText = "Tomorrow"
        ),
        TaskItem(
            weekNumber = 3,
            dayNumber = 2,
            title = "Build Vector DB Ingestion Pipeline with Chroma",
            description = "Load 5 PDF research papers, chunk with RecursiveCharacterTextSplitter (chunk_size=500, overlap=50), and store embeddings.",
            deliverable = "Working script querying nearest 3 chunks for a given sample prompt.",
            category = "LLM",
            estimatedMinutes = 75,
            isCompleted = false,
            isMissed = false,
            dueDateText = "Tomorrow"
        )
    )

    val initialPracticeItems = listOf(
        PracticeItem(
            category = "DSA",
            topic = "Arrays & Two Pointers",
            title = "Container With Most Water (LC #11)",
            difficulty = "Medium",
            description = "Given n non-negative integers a1, a2, ..., an where each represents a point at coordinate (i, ai). Find two lines that together with the x-axis forms a container that holds the most water.",
            hints = "Start with maximum width (pointers at 0 and n-1). Move the pointer pointing to the shorter line inward.",
            starterCode = "def maxArea(height: list[int]) -> int:\n    left, right = 0, len(height) - 1\n    max_water = 0\n    # TODO: Implement two-pointer scan\n    return max_water",
            solutionCode = "def maxArea(height: list[int]) -> int:\n    left, right = 0, len(height) - 1\n    max_water = 0\n    while left < right:\n        w = right - left\n        h = min(height[left], height[right])\n        max_water = max(max_water, w * h)\n        if height[left] < height[right]:\n            left += 1\n        else:\n            right -= 1\n    return max_water",
            testCases = "height = [1,8,6,2,5,4,8,3,7] -> Output: 49",
            isSolved = true
        ),
        PracticeItem(
            category = "DSA",
            topic = "Trees & Graph Traversal",
            title = "Lowest Common Ancestor in Binary Tree (LC #236)",
            difficulty = "Medium",
            description = "Given a binary tree, find the lowest common ancestor (LCA) of two given nodes p and q in the tree.",
            hints = "If current root is p or q or null, return root. Recursively search left and right subtrees. If both return non-null, root is LCA.",
            starterCode = "def lowestCommonAncestor(root, p, q):\n    # TODO: Recursive tree traversal\n    pass",
            solutionCode = "def lowestCommonAncestor(root, p, q):\n    if not root or root == p or root == q:\n        return root\n    left = lowestCommonAncestor(root.left, p, q)\n    right = lowestCommonAncestor(root.right, p, q)\n    if left and right:\n        return root\n    return left or right",
            testCases = "root = [3,5,1,6,2,0,8], p = 5, q = 1 -> Output: 3",
            isSolved = false
        ),
        PracticeItem(
            category = "DSA",
            topic = "Dynamic Programming",
            title = "Coin Change (LC #322)",
            difficulty = "Medium",
            description = "You are given an integer array coins and an integer amount. Return the fewest number of coins that you need to make up that amount, or -1 if impossible.",
            hints = "Use bottom-up DP array dp[i] where dp[i] is min coins for amount i. Initialize with infinity.",
            starterCode = "def coinChange(coins: list[int], amount: int) -> int:\n    # TODO: Bottom-up DP\n    pass",
            solutionCode = "def coinChange(coins: list[int], amount: int) -> int:\n    dp = [float('inf')] * (amount + 1)\n    dp[0] = 0\n    for c in coins:\n        for i in range(c, amount + 1):\n            dp[i] = min(dp[i], dp[i - c] + 1)\n    return dp[amount] if dp[amount] != float('inf') else -1",
            testCases = "coins = [1,2,5], amount = 11 -> Output: 3",
            isSolved = false
        ),
        PracticeItem(
            category = "ML_CODING",
            topic = "Deep Learning Fundamentals",
            title = "Implement Scaled Dot-Product Attention in PyTorch",
            difficulty = "Medium",
            description = "Write Attention(Q, K, V) = softmax(Q * K.T / sqrt(d_k)) * V with optional attention mask.",
            hints = "Matrix multiply Q with K transposed along last two dimensions. Divide by math.sqrt(d_k). Apply mask where mask==0 with -1e9.",
            starterCode = "import torch\nimport math\n\ndef scaled_dot_product_attention(Q, K, V, mask=None):\n    # Q, K, V: (batch, heads, seq_len, d_k)\n    # TODO: compute attention\n    pass",
            solutionCode = "import torch\nimport math\n\ndef scaled_dot_product_attention(Q, K, V, mask=None):\n    d_k = Q.size(-1)\n    scores = torch.matmul(Q, K.transpose(-2, -1)) / math.sqrt(d_k)\n    if mask is not None:\n        scores = scores.masked_fill(mask == 0, -1e9)\n    weights = torch.softmax(scores, dim=-1)\n    return torch.matmul(weights, V), weights",
            testCases = "Q.shape=(2, 4, 16, 64) -> Output shape: (2, 4, 16, 64)",
            isSolved = true
        ),
        PracticeItem(
            category = "ML_CODING",
            topic = "Classical Machine Learning",
            title = "Linear Regression with Gradient Descent from Scratch",
            difficulty = "Easy",
            description = "Implement gradient calculation and parameter updates for weights and bias using pure NumPy.",
            hints = "Gradient w.r.t weights is (1/m) * X.T @ (y_pred - y). Gradient w.r.t bias is (1/m) * sum(y_pred - y).",
            starterCode = "import numpy as np\n\nclass LinearRegression:\n    def __init__(self, lr=0.01, epochs=1000):\n        self.lr = lr\n        self.epochs = epochs\n        self.w = None\n        self.b = None\n\n    def fit(self, X, y):\n        # TODO: Gradient descent loop\n        pass",
            solutionCode = "import numpy as np\n\nclass LinearRegression:\n    def __init__(self, lr=0.01, epochs=1000):\n        self.lr = lr\n        self.epochs = epochs\n        self.w = None\n        self.b = 0\n\n    def fit(self, X, y):\n        m, n = X.shape\n        self.w = np.zeros(n)\n        for _ in range(self.epochs):\n            y_pred = X @ self.w + self.b\n            dw = (1 / m) * (X.T @ (y_pred - y))\n            db = (1 / m) * np.sum(y_pred - y)\n            self.w -= self.lr * dw\n            self.b -= self.lr * db",
            testCases = "X shape: (100, 2), y shape: (100,)",
            isSolved = false
        ),
        PracticeItem(
            category = "MINI_PROJECT",
            topic = "RAG & GenAI",
            title = "Mini-Project 1: Multi-Document RAG with Hybrid Search & Reranker",
            difficulty = "Hard",
            description = "Build an end-to-end RAG application capable of ingesting PDF/Markdown documents, generating dense embeddings with BAAI/bge-small-en-v1.5, running sparse BM25 search, combining with Reciprocal Rank Fusion (RRF), and synthesizing answers with citation sources.",
            hints = "Use ChromaDB or FAISS for dense indexing. Rank-BM25 for lexical matching. Use flash-attn or quantized local LLM / Gemini API for answer generation.",
            starterCode = "# Step 1: Ingestion & Chunking\n# Step 2: Vector DB & BM25 Index\n# Step 3: Hybrid Retrieval & Re-ranking\n# Step 4: Context Injection & Prompt Template",
            solutionCode = "# Reference Architecture:\n# Ingestion -> LangChain RecursiveSplitter -> Chroma (dense) + BM25 (sparse)\n# Retrieval -> Reciprocal Rank Fusion -> Flashrank Reranker (top 3)\n# Generation -> Gemini 3.5 Flash with strict grounding instructions",
            testCases = "Query: 'What is the KV cache memory formula in Llama 3?' -> Returns correct equation with page citation",
            isSolved = false
        ),
        PracticeItem(
            category = "MINI_PROJECT",
            topic = "Fine-Tuning & Quantization",
            title = "Mini-Project 2: QLoRA Domain Adaptation of Llama-3-8B",
            difficulty = "Hard",
            description = "Fine-tune an open-source 8B parameter model for specialized Python coding assistance on a single T4/A10G GPU using bitsandbytes 4-bit NormalFloat quantization and PEFT LoRA matrices.",
            hints = "Use lora_r=16, lora_alpha=32, target_modules=['q_proj', 'k_proj', 'v_proj', 'o_proj']. Use SFTTrainer from TRL library.",
            starterCode = "from transformers import AutoModelForCausalLM, BitsAndBytesConfig\nfrom peft import LoraConfig, get_peft_model\n# TODO: Configure 4-bit bnb and LoRA adapter",
            solutionCode = "bnb_config = BitsAndBytesConfig(load_in_4bit=True, bnb_4bit_quant_type='nf4', bnb_4bit_compute_dtype=torch.bfloat16)\nmodel = AutoModelForCausalLM.from_pretrained(model_id, quantization_config=bnb_config)\npeft_config = LoraConfig(r=16, lora_alpha=32, target_modules=['q_proj', 'v_proj'], lora_dropout=0.05, task_type='CAUSAL_LM')",
            testCases = "Evaluate perplexity before vs after 2 epochs on test split",
            isSolved = false
        )
    )

    val initialQuizzes = listOf(
        QuizItem(
            category = "LLM_RAG",
            question = "In modern RAG systems, why is Hybrid Search (Dense + Sparse/BM25) often superior to dense semantic search alone?",
            optionA = "Dense embeddings require O(N^2) search time while BM25 is strictly O(1)",
            optionB = "Dense embeddings can miss exact keyword matches like SKU codes, error codes, and exact names",
            optionC = "BM25 eliminates the need for any chunking strategy",
            optionD = "BM25 automatically generates conversational answers without an LLM",
            correctIndex = 1,
            explanation = "Dense vector embeddings capture semantic meaning but frequently fail on rare out-of-vocabulary tokens, specific IDs, error logs, or exact product codes. Combining BM25 with dense vectors captures both semantic meaning and lexical precision."
        ),
        QuizItem(
            category = "ML_CORE",
            question = "What is the primary motivation behind the Scaled Dot-Product Attention scaling factor (1 / sqrt(d_k)) in Transformers?",
            optionA = "To prevent the softmax gradients from vanishing due to large magnitude dot-products",
            optionB = "To invert the covariance matrix of Query and Key vectors",
            optionC = "To ensure token positions are invariant under permutation",
            optionD = "To force the attention matrix to have rank equal to d_k",
            correctIndex = 0,
            explanation = "For large values of d_k, dot products grow large in magnitude, pushing the softmax function into regions with extremely small gradients. Dividing by sqrt(d_k) stabilizes the variance of the dot-products around 1.0."
        ),
        QuizItem(
            category = "LLM_RAG",
            question = "What does the 'rank' parameter (r) control in Low-Rank Adaptation (LoRA)?",
            optionA = "The number of prompt tokens injected before the user prompt",
            optionB = "The inner dimension of the decomposed update matrices A and B where ΔW = B * A",
            optionC = "The percentage of weights completely pruned from the base model",
            optionD = "The learning rate multiplier for optimizer momentum",
            correctIndex = 1,
            explanation = "LoRA decomposes the weight update ΔW of size (d x k) into two low-rank matrices B of size (d x r) and A of size (r x k). Rank r determines the expressive capacity and trainable parameter count."
        ),
        QuizItem(
            category = "SYSTEM_DESIGN",
            question = "During LLM inference serving, why does the KV Cache memory scale linearly with batch size and sequence length?",
            optionA = "Because each generated token must re-tokenize the entire dictionary vocabulary",
            optionB = "Because past Key and Value vectors for all prior tokens must be stored to avoid recomputing self-attention in autoregressive decoding",
            optionC = "Because weights are dynamically re-trained on each generated token",
            optionD = "Because modern GPUs cannot store floating-point numbers in SRAM",
            correctIndex = 1,
            explanation = "In autoregressive decoding, each new token attends to all previous tokens. To avoid recomputing past key and value representations at every generation step, they are cached in GPU VRAM (KV Cache)."
        ),
        QuizItem(
            category = "DSA",
            question = "What is the worst-case time complexity of QuickSelect to find the K-th largest element in an unsorted array of size N?",
            optionA = "O(N log N)",
            optionB = "O(N) worst case and O(log N) average",
            optionC = "O(N^2) worst case and O(N) average case",
            optionD = "O(K log N) with a min-heap",
            correctIndex = 2,
            explanation = "QuickSelect has an average time complexity of O(N) when balanced partitions are chosen, but worst case is O(N^2) when poor pivots (e.g. sorted array with first element pivot) are chosen repeatedly."
        )
    )

    val initialJobs = listOf(
        JobItem(
            company = "Anthropic",
            role = "AI Resident / Associate ML Engineer",
            status = "INTERVIEW",
            location = "San Francisco, CA / Hybrid",
            salaryRange = "$130,000 - $165,000",
            jdText = "Looking for freshers and early career researchers in LLM alignment, RLHF, PyTorch model evaluation, and Python data pipelines.",
            tailoredResumeNotes = "Highlight: PyTorch Multi-Head Attention from scratch, RLHF reward modeling concepts, and fast Python optimization.",
            notes = "Technical round 1 scheduled for next Tuesday. Focus on Transformer architectural details and KV Cache math.",
            appliedDate = "3 days ago"
        ),
        JobItem(
            company = "Scale AI",
            role = "Machine Learning Engineer - GenAI",
            status = "APPLIED",
            location = "New York, NY / Remote",
            salaryRange = "$120,000 - $145,000",
            jdText = "Develop RAG pipelines, fine-tune models using LoRA/QLoRA, and build automated LLM benchmarking suites.",
            tailoredResumeNotes = "Emphasized: Hybrid RAG project with ChromaDB, BM25 retrieval, and LoRA fine-tuning benchmarks.",
            notes = "Application submitted via employee referral.",
            appliedDate = "5 days ago"
        ),
        JobItem(
            company = "Databricks",
            role = "Associate Software Engineer - ML Platform",
            status = "SAVED",
            location = "San Francisco, CA",
            salaryRange = "$135,000 - $160,000",
            jdText = "Build high-throughput distributed training and inference infrastructure using Spark, MLflow, and Kubernetes.",
            tailoredResumeNotes = "Tailor resume to highlight distributed systems coursework, Docker, and MLflow pipeline experience.",
            notes = "Target application deadline: Friday.",
            appliedDate = "Saved"
        ),
        JobItem(
            company = "Weights & Biases",
            role = "Junior Solutions Engineer - AI/ML",
            status = "OFFER",
            location = "Remote",
            salaryRange = "$115,000 - $135,000",
            jdText = "Help enterprise AI teams monitor, track experiments, and optimize LLM pipelines using W&B Weave and Models.",
            tailoredResumeNotes = "Highlighted experiment tracking in PyTorch capstone project and strong communication skills.",
            notes = "Received written offer! Reviewing compensation package before final decision.",
            appliedDate = "2 weeks ago"
        )
    )

    val initialProjects = listOf(
        ProjectItem(
            title = "Enterprise Document RAG with Hybrid Search & Citation Guardrails",
            domain = "LLM / RAG",
            description = "Production-grade question answering system over complex financial PDFs with dense embeddings, BM25 lexical search, reciprocal rank fusion, and hallucination detection.",
            techStack = "Python, LangChain, ChromaDB, FastAPI, Docker, Gemini 3.5 Flash",
            deliverables = "GitHub repo, Dockerfile, live Streamlit UI, evaluation metrics on 100 benchmark queries",
            status = "IN_PROGRESS"
        ),
        ProjectItem(
            title = "End-to-End MLOps Pipeline for Real-time Fraud Detection",
            domain = "MLOps & System Design",
            description = "Streaming transaction anomaly detection with XGBoost, MLflow model tracking, Triton server deployment, and Grafana drift monitoring.",
            techStack = "Python, XGBoost, MLflow, Kafka, Docker, Prometheus",
            deliverables = "Automated CI/CD GitHub Actions for model retraining and latency benchmarking under 15ms",
            status = "NOT_STARTED"
        ),
        ProjectItem(
            title = "Domain-Adapted LLM Code Assistant via QLoRA & SFT",
            domain = "GenAI Fine-Tuning",
            description = "Instruction fine-tuning of Llama-3-8B on 50k specialized API code-completion pairs using 4-bit quantization and parameter-efficient adapters.",
            techStack = "PyTorch, HuggingFace TRL, BitsAndBytes, PEFT, Weights & Biases",
            deliverables = "Adapter weights on HuggingFace Hub, benchmark evaluation showing 18% improvement on human-eval subset",
            status = "NOT_STARTED"
        )
    )
}
