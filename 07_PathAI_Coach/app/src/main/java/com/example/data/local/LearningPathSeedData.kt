package com.example.data.local

import com.example.data.model.FirestoreTask
import com.example.data.model.LearningModule
import com.example.data.model.LearningPath
import com.example.data.model.ModuleQuizQuestion

object LearningPathSeedData {

    val initialPaths: List<LearningPath> = listOf(
        LearningPath(
            id = "python",
            name = "Python",
            description = "Master production Python from core language mechanics, OOP, and data structures to async programming, APIs, and testing.",
            order_index = 0,
            modules = listOf(
                "python_01_basics",
                "python_02_control_flow",
                "python_03_functions",
                "python_04_oop",
                "python_05_data_handling",
                "python_06_apis",
                "python_07_testing"
            ),
            total_estimated_hours = 38
        ),
        LearningPath(
            id = "aiml",
            name = "AI/ML",
            description = "Foundational to modern machine learning: linear algebra, gradient descent, classical algorithms, neural nets, and deep learning with PyTorch.",
            order_index = 1,
            modules = listOf(
                "aiml_01_math",
                "aiml_02_supervised",
                "aiml_03_evaluation",
                "aiml_04_neural_nets",
                "aiml_05_deep_learning",
                "aiml_06_cnns_rnns",
                "aiml_07_transformers"
            ),
            total_estimated_hours = 52
        ),
        LearningPath(
            id = "llm_rag",
            name = "LLM/RAG",
            description = "Modern Generative AI pipelines: prompt engineering, dense embeddings, vector databases, hybrid retrieval, and automated evaluation.",
            order_index = 2,
            modules = listOf(
                "llm_01_prompting",
                "llm_02_embeddings",
                "llm_03_vector_dbs",
                "llm_04_rag_arch",
                "llm_05_eval"
            ),
            total_estimated_hours = 32
        ),
        LearningPath(
            id = "projects",
            name = "Projects",
            description = "Portfolio-grade capstone projects designed to demonstrate production readiness and clear architectural thinking to hiring leads.",
            order_index = 3,
            modules = listOf(
                "proj_01_rag_engine",
                "proj_02_lora_finetune",
                "proj_03_mlops_serving",
                "proj_04_agentic_workflow"
            ),
            total_estimated_hours = 48
        )
    )

    val initialModules: List<LearningModule> = listOf(
        // --- PYTHON PATH ---
        LearningModule(
            id = "python_01_basics",
            path_id = "python",
            title = "Python Basics & Data Types",
            description = "Core primitives, mutable vs immutable types, memory references, and idiomatic Python syntax.",
            order_index = 0,
            prerequisites = emptyList(),
            estimated_hours = 4,
            skills = listOf("Python Syntax", "Data Types", "Memory Model", "Strings & Lists"),
            related_concepts = listOf("Memory references", "String slicing", "List slicing", "Dictionary hashing"),
            content = """
                ### 1. Variables and Memory Model
                In Python, variables are named references (pointers) to objects in heap memory. Integers, floats, booleans, and tuples are **immutable**, whereas lists, dicts, and sets are **mutable**.
                
                ```python
                # Immutable behavior
                a = [1, 2, 3]
                b = a
                b.append(4)
                print(a) # Output: [1, 2, 3, 4] - both point to same object!
                ```
                
                ### 2. Slicing & Collections
                Slicing syntax `[start:stop:step]` creates a shallow copy of sequences. Dictionary lookups operate in O(1) average time through hash table buckets.
            """.trimIndent()
        ),
        LearningModule(
            id = "python_02_control_flow",
            path_id = "python",
            title = "Control Flow & Comprehensions",
            description = "Master conditionals, loop efficiency, list/dictionary comprehensions, and structural pattern matching.",
            order_index = 1,
            prerequisites = listOf("python_01_basics"),
            estimated_hours = 5,
            skills = listOf("List Comprehensions", "Dict Comprehensions", "Match-Case", "Generators"),
            related_concepts = listOf("Generator expressions", "Short-circuit evaluation", "Pattern matching"),
            content = """
                ### 1. List & Dictionary Comprehensions
                Comprehensions are executed at C-speed in CPython and provide a concise, declarative syntax:
                
                ```python
                matrix = [[1, 2, 3], [4, 5, 6]]
                flattened = [val for row in matrix for val in row if val % 2 == 0]
                # Result: [2, 4, 6]
                ```
                
                ### 2. Structural Pattern Matching
                Python 3.10+ provides `match ... case` for destructuring tuples, dicts, and dataclasses cleanly.
            """.trimIndent()
        ),
        LearningModule(
            id = "python_03_functions",
            path_id = "python",
            title = "Functions, Lambdas & Decorators",
            description = "First-class functions, closures, variable arguments (*args, **kwargs), decorators, and type hints.",
            order_index = 2,
            prerequisites = listOf("python_02_control_flow"),
            estimated_hours = 6,
            skills = listOf("Decorators", "Closures", "Args & Kwargs", "Type Hinting"),
            related_concepts = listOf("functools.wraps", "Execution closures", "Higher-order functions"),
            content = """
                ### 1. Decorator Mechanics
                A decorator is a callable that takes another function and returns an augmented wrapper:
                
                ```python
                import time
                from functools import wraps

                def timed(func):
                    @wraps(func)
                    def wrapper(*args, **kwargs):
                        start = time.perf_counter()
                        res = func(*args, **kwargs)
                        print(f"{func.__name__} took {time.perf_counter() - start:.4f}s")
                        return res
                    return wrapper
                ```
            """.trimIndent()
        ),
        LearningModule(
            id = "python_04_oop",
            path_id = "python",
            title = "Object-Oriented Programming & Protocols",
            description = "Classes, inheritance, dunder methods (__repr__, __len__, __iter__), dataclasses, and abstract base classes.",
            order_index = 3,
            prerequisites = listOf("python_03_functions"),
            estimated_hours = 6,
            skills = listOf("OOP", "Dunder Methods", "Dataclasses", "Encapsulation"),
            related_concepts = listOf("MRO (Method Resolution Order)", "__slots__", "Duck typing"),
            content = """
                ### 1. Custom Dunder Methods & Dataclasses
                Using `@dataclass` removes boilerplate for constructor, equality checks, and representation:
                
                ```python
                from dataclasses import dataclass

                @dataclass(frozen=True)
                class EmbeddingVector:
                    model_name: String
                    dim: Int
                    vector: List[Float]
                ```
            """.trimIndent()
        ),
        LearningModule(
            id = "python_05_data_handling",
            path_id = "python",
            title = "Data Wrangling with NumPy & Pandas",
            description = "Fast vectorization, tensor indexing, broadcasting, DataFrame filtering, grouping, and aggregations.",
            order_index = 4,
            prerequisites = listOf("python_04_oop"),
            estimated_hours = 6,
            skills = listOf("NumPy", "Pandas", "Broadcasting", "Vectorization"),
            related_concepts = listOf("Strided memory", "Groupby operations", "Handling missing values"),
            content = """
                ### 1. Vectorized NumPy Operations
                Avoid slow Python for-loops over matrices. NumPy operations compile directly to SIMD vectorized assembly:
                
                ```python
                import numpy as np
                x = np.random.randn(1000, 512)
                # Compute cosine similarities in parallel:
                norms = np.linalg.norm(x, axis=1, keepdims=True)
                normalized = x / norms
                sim_matrix = normalized @ normalized.T
                ```
            """.trimIndent()
        ),
        LearningModule(
            id = "python_06_apis",
            path_id = "python",
            title = "Async Python & REST APIs with FastAPI",
            description = "Asyncio event loop, coroutines (async/await), Pydantic validation, and production FastAPI services.",
            order_index = 5,
            prerequisites = listOf("python_05_data_handling"),
            estimated_hours = 6,
            skills = listOf("FastAPI", "Asyncio", "Pydantic", "REST APIs"),
            related_concepts = listOf("Event loop blocking", "Dependency injection", "OpenAPI schema"),
            content = """
                ### 1. Building High-Performance Inference APIs
                FastAPI combines Starlette's async ASGI server with Pydantic type validation:
                
                ```python
                from fastapi import FastAPI
                from pydantic import BaseModel

                app = FastAPI(title="Embedding Service")

                class QueryRequest(BaseModel):
                    query: str
                    top_k: int = 5

                @app.post("/search")
                async def search(req: QueryRequest):
                    return {"results": [], "query": req.query}
                ```
            """.trimIndent()
        ),
        LearningModule(
            id = "python_07_testing",
            path_id = "python",
            title = "Testing, Packaging & Best Practices",
            description = "Unit testing with Pytest, fixtures, mocking API calls, linters (Ruff), and virtual environment isolation.",
            order_index = 6,
            prerequisites = listOf("python_06_apis"),
            estimated_hours = 5,
            skills = listOf("Pytest", "Mocking", "Ruff", "Type Safety (MyPy)"),
            related_concepts = listOf("Pytest fixtures", "Parametrized tests", "Unit test isolation"),
            content = """
                ### 1. Pytest Fixtures and Mocking
                Use fixtures to share initialization state and `unittest.mock` to intercept network dependencies.
            """.trimIndent()
        ),

        // --- AI/ML PATH ---
        LearningModule(
            id = "aiml_01_math",
            path_id = "aiml",
            title = "Linear Algebra & Gradient Foundations",
            description = "Vectors, matrices, dot products, eigenvalues, partial derivatives, and Gradient Descent convergence.",
            order_index = 0,
            prerequisites = emptyList(),
            estimated_hours = 6,
            skills = listOf("Linear Algebra", "Calculus", "Gradient Descent", "Loss Functions"),
            related_concepts = listOf("Matrix dot products", "Jacobian & Hessian", "Learning rate scheduling"),
            content = """
                ### 1. Gradient Descent Update Rule
                Parameters are iteratively updated in the direction opposite to the gradient of the loss:
                $$\theta_{t+1} = \theta_t - \eta \cdot \nabla_\theta L(\theta_t)$$
                Where $\eta$ represents the learning rate.
            """.trimIndent()
        ),
        LearningModule(
            id = "aiml_02_supervised",
            path_id = "aiml",
            title = "Supervised Learning & Tree Ensembles",
            description = "Linear/logistic regression, decision trees, random forests, and gradient boosting (XGBoost / LightGBM).",
            order_index = 1,
            prerequisites = listOf("aiml_01_math"),
            estimated_hours = 7,
            skills = listOf("Logistic Regression", "Random Forest", "XGBoost", "Feature Engineering"),
            related_concepts = listOf("Information gain / Gini", "Bagging vs Boosting", "Hyperparameter tuning"),
            content = """
                ### 1. Gradient Boosting (XGBoost)
                Unlike Random Forests that train trees independently in parallel (bagging), gradient boosting trains each successive tree to predict the residual errors of the prior ensemble.
            """.trimIndent()
        ),
        LearningModule(
            id = "aiml_03_evaluation",
            path_id = "aiml",
            title = "Model Evaluation & Validation Strategies",
            description = "Confusion matrices, Precision, Recall, F1, ROC-AUC, K-fold cross-validation, and the bias-variance tradeoff.",
            order_index = 2,
            prerequisites = listOf("aiml_02_supervised"),
            estimated_hours = 6,
            skills = listOf("ROC-AUC", "F1 Score", "Cross-Validation", "Bias-Variance"),
            related_concepts = listOf("Precision-Recall curve", "Data leakage prevention", "Stratified K-Fold"),
            content = """
                ### 1. Precision vs Recall Tradeoff
                - **Precision**: Of all predicted positives, how many were actual positives? $\frac{TP}{TP + FP}$
                - **Recall (Sensitivity)**: Of all true positives in the dataset, how many did we catch? $\frac{TP}{TP + FN}$
            """.trimIndent()
        ),
        LearningModule(
            id = "aiml_04_neural_nets",
            path_id = "aiml",
            title = "Neural Networks & Backpropagation",
            description = "Multilayer perceptrons, activation functions (ReLU, GELU, Sigmoid), computational graphs, and backward passes.",
            order_index = 3,
            prerequisites = listOf("aiml_03_evaluation"),
            estimated_hours = 8,
            skills = listOf("Backpropagation", "Activations (GELU/ReLU)", "Cross-Entropy Loss", "Adam Optimizer"),
            related_concepts = listOf("Chain rule in computational graphs", "Vanishing/exploding gradients", "Weight initialization (He/Xavier)"),
            content = """
                ### 1. The Backpropagation Algorithm
                Applying the chain rule backward from scalar loss L through network layers to compute dL/dW_l.
            """.trimIndent()
        ),
        LearningModule(
            id = "aiml_05_deep_learning",
            path_id = "aiml",
            title = "Deep Learning with PyTorch",
            description = "Tensor operations, GPU acceleration, building custom `nn.Module`, DataLoader pipelines, and training loops.",
            order_index = 4,
            prerequisites = listOf("aiml_04_neural_nets"),
            estimated_hours = 9,
            skills = listOf("PyTorch", "nn.Module", "CUDA / MPS", "Training Loops"),
            related_concepts = listOf("optimizer.zero_grad()", "loss.backward()", "torch.no_grad()"),
            content = """
                ### 1. Standard PyTorch Training Loop
                ```python
                model.train()
                for x, y in dataloader:
                    optimizer.zero_grad()
                    preds = model(x)
                    loss = criterion(preds, y)
                    loss.backward()
                    optimizer.step()
                ```
            """.trimIndent()
        ),
        LearningModule(
            id = "aiml_06_cnns_rnns",
            path_id = "aiml",
            title = "Computer Vision & Sequence Modeling",
            description = "Convolutions, pooling, ResNet skip connections, RNNs, and LSTMs for time-series and token sequences.",
            order_index = 5,
            prerequisites = listOf("aiml_05_deep_learning"),
            estimated_hours = 8,
            skills = listOf("CNNs", "ResNet", "Skip Connections", "Sequence Models"),
            related_concepts = listOf("Receptive field", "Batch Normalization", "Gradient highway"),
            content = """
                ### 1. Residual Connections
                ResNet introduces shortcut connections F(x) + x, allowing gradients to flow directly through hundreds of layers without vanishing.
            """.trimIndent()
        ),
        LearningModule(
            id = "aiml_07_transformers",
            path_id = "aiml",
            title = "Attention Mechanisms & Transformers",
            description = "Scaled dot-product attention, multi-head projection, positional embeddings, encoder vs decoder architectures.",
            order_index = 6,
            prerequisites = listOf("aiml_06_cnns_rnns"),
            estimated_hours = 8,
            skills = listOf("Transformers", "Self-Attention", "Multi-Head Attention", "KV Caching"),
            related_concepts = listOf("Attention scaling factor", "Causal masking", "LayerNorm / RMSNorm"),
            content = """
                ### 1. Scaled Dot-Product Attention
                $$\text{Attention}(Q, K, V) = \text{softmax}\left(\frac{QK^T}{\sqrt{d_k}}\right)V$$
                Dividing by $\sqrt{d_k}$ prevents inner products from saturating the softmax in high dimensions.
            """.trimIndent()
        ),

        // --- LLM/RAG PATH ---
        LearningModule(
            id = "llm_01_prompting",
            path_id = "llm_rag",
            title = "Prompt Engineering & Structured Outputs",
            description = "System instructions, few-shot prompting, Chain-of-Thought (CoT), function calling, and strict JSON schemas.",
            order_index = 0,
            prerequisites = emptyList(),
            estimated_hours = 5,
            skills = listOf("Prompt Engineering", "Few-Shot", "Chain-of-Thought", "Structured JSON"),
            related_concepts = listOf("In-context learning", "System prompts", "Hallucination mitigation"),
            content = """
                ### 1. Chain-of-Thought Prompting
                Instructing the model to "think step by step before generating the answer" triggers intermediate reasoning states that drastically reduce hallucination.
            """.trimIndent()
        ),
        LearningModule(
            id = "llm_02_embeddings",
            path_id = "llm_rag",
            title = "Embeddings & Semantic Chunking",
            description = "Dense vector embeddings (text-embedding-3, BGE), semantic chunking strategies, and token budget management.",
            order_index = 1,
            prerequisites = listOf("llm_01_prompting"),
            estimated_hours = 6,
            skills = listOf("Embeddings", "Chunking", "Cosine Similarity", "Token Management"),
            related_concepts = listOf("Recursive character chunking", "Embedding normalization", "Semantic overlap"),
            content = """
                ### 1. Chunking Tradeoffs
                - Small chunks (128–256 tokens): High precision, but risk losing contextual narrative.
                - Large chunks (512–1024 tokens): Rich context, but lower similarity precision due to embedding dilution.
            """.trimIndent()
        ),
        LearningModule(
            id = "llm_03_vector_dbs",
            path_id = "llm_rag",
            title = "Vector Databases & HNSW Indexing",
            description = "Vector stores (Chroma, Pinecone, FAISS), Approximate Nearest Neighbors (ANN), HNSW graphs, and metadata filters.",
            order_index = 2,
            prerequisites = listOf("llm_02_embeddings"),
            estimated_hours = 7,
            skills = listOf("Vector DBs", "HNSW Indexing", "ChromaDB", "Metadata Filtering"),
            related_concepts = listOf("Hierarchical Navigable Small World", "Recall@K", "Hybrid filtering"),
            content = """
                ### 1. HNSW Indexing
                HNSW organizes embedding vectors into hierarchical multi-layer skip-lists, providing sub-millisecond nearest neighbor search over millions of vectors.
            """.trimIndent()
        ),
        LearningModule(
            id = "llm_04_rag_arch",
            path_id = "llm_rag",
            title = "Advanced RAG & Hybrid Retrieval",
            description = "Hybrid search (BM25 lexical + dense embeddings), Reciprocal Rank Fusion (RRF), and cross-encoder re-ranking.",
            order_index = 3,
            prerequisites = listOf("llm_03_vector_dbs"),
            estimated_hours = 7,
            skills = listOf("Hybrid Search", "BM25", "Reciprocal Rank Fusion", "Re-ranking"),
            related_concepts = listOf("Cross-encoder reranking (Cohere/BGE)", "Context compression", "Query decomposition"),
            content = """
                ### 1. Reciprocal Rank Fusion (RRF)
                $$\text{RRF Score}(d) = \sum_{m \in M} \frac{1}{60 + r_m(d)}$$
                Combines keyword precision with semantic nuance without requiring score normalization across different scales.
            """.trimIndent()
        ),
        LearningModule(
            id = "llm_05_eval",
            path_id = "llm_rag",
            title = "RAG Evaluation & Production Guardrails",
            description = "Automated evaluation with RAGAS (Faithfulness, Context Recall, Answer Relevance), latency caching, and guardrails.",
            order_index = 4,
            prerequisites = listOf("llm_04_rag_arch"),
            estimated_hours = 7,
            skills = listOf("RAGAS", "Faithfulness", "Guardrails", "Semantic Caching"),
            related_concepts = listOf("Answer relevance vs hallucination", "Semantic cache hit rate", "Production telemetry"),
            content = """
                ### 1. RAG Triad Metrics
                1. **Context Relevance**: Was the retrieved context relevant to the user query?
                2. **Faithfulness / Groundedness**: Is every statement in the answer directly supported by the context?
                3. **Answer Relevance**: Does the final answer directly solve the user's intent?
            """.trimIndent()
        ),

        // --- PROJECTS PATH ---
        LearningModule(
            id = "proj_01_rag_engine",
            path_id = "projects",
            title = "Multimodal Document RAG Assistant",
            description = "Production RAG service processing PDFs, technical manuals, and code repos with hybrid search and streaming responses.",
            order_index = 0,
            prerequisites = listOf("llm_04_rag_arch"),
            estimated_hours = 12,
            skills = listOf("FastAPI", "ChromaDB", "LangChain/LlamaIndex", "Streaming UI"),
            related_concepts = listOf("PDF parsing", "Streaming tokens", "RRF integration"),
            content = """
                ### Project Deliverable
                Build and open-source a containerized FastAPI service with a Streamlit/Compose frontend that ingests technical PDFs and benchmarks retrieval accuracy using RAGAS.
            """.trimIndent()
        ),
        LearningModule(
            id = "proj_02_lora_finetune",
            path_id = "projects",
            title = "Parameter-Efficient Fine-Tuning (LoRA)",
            description = "Fine-tune an open-source LLM (Llama-3 / Mistral) using QLoRA for custom domain code generation or SQL translation.",
            order_index = 1,
            prerequisites = listOf("aiml_07_transformers"),
            estimated_hours = 12,
            skills = listOf("PEFT", "LoRA", "HuggingFace", "Quantization (4-bit)"),
            related_concepts = listOf("Low-rank adaptation", "bitsandbytes", "Instruction tuning datasets"),
            content = """
                ### Project Deliverable
                Prepare an instruction-tuning dataset, execute 4-bit QLoRA fine-tuning using Unsloth/HuggingFace SFTTrainer, and publish evaluation loss charts to Weights & Biases.
            """.trimIndent()
        ),
        LearningModule(
            id = "proj_03_mlops_serving",
            path_id = "projects",
            title = "High-Throughput ML Serving & MLOps",
            description = "Containerize a transformer model with Docker, export to ONNX Runtime, and set up Prometheus latency monitoring.",
            order_index = 2,
            prerequisites = listOf("python_06_apis", "aiml_05_deep_learning"),
            estimated_hours = 12,
            skills = listOf("Docker", "ONNX Runtime", "Prometheus", "CI/CD"),
            related_concepts = listOf("Multi-stage Docker builds", "Inference latency (p95/p99)", "GitHub Actions test suite"),
            content = """
                ### Project Deliverable
                Demonstrate sub-20ms p95 latency under concurrent load with complete Docker setup, GitHub Actions CI testing, and Prometheus metrics endpoint.
            """.trimIndent()
        ),
        LearningModule(
            id = "proj_04_agentic_workflow",
            path_id = "projects",
            title = "Autonomous Multi-Agent Research Engine",
            description = "Orchestrate multi-agent cycles with supervisor routing, live search tool execution, state memory, and report drafting.",
            order_index = 3,
            prerequisites = listOf("llm_05_eval"),
            estimated_hours = 12,
            skills = listOf("Multi-Agent", "LangGraph", "Tool Use", "Persistent Memory"),
            related_concepts = listOf("Supervisor pattern", "State graphs", "Human-in-the-loop validation"),
            content = """
                ### Project Deliverable
                Implement a multi-agent system where a Research Agent queries APIs, an Evaluation Agent critiques reasoning, and a Synthesizer compiles an executive summary.
            """.trimIndent()
        )
    )

    /**
     * Generate 2-4 daily tasks (30-90 min each) for any given module.
     */
    fun generateTasksForModule(uid: String, module: LearningModule): List<FirestoreTask> {
        val now = "Today"
        return when (module.id) {
            "python_01_basics" -> listOf(
                FirestoreTask(
                    id = "${module.id}_task_1",
                    uid = uid,
                    module_id = module.id,
                    title = "Read: Memory model & mutable vs immutable types",
                    description = "Study Python heap references, `id()`, and shallow vs deep copies in `copy` module.",
                    estimate_minutes = 35,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "READING"
                ),
                FirestoreTask(
                    id = "${module.id}_task_2",
                    uid = uid,
                    module_id = module.id,
                    title = "Solve 3 slice manipulation & hashing problems",
                    description = "Implement palindrome checks, frequency maps without `Counter`, and strided reverse indexing.",
                    estimate_minutes = 50,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "PRACTICE"
                ),
                FirestoreTask(
                    id = "${module.id}_task_3",
                    uid = uid,
                    module_id = module.id,
                    title = "Complete Module 1 Quiz & Concept Verification",
                    description = "Answer 4 socratic questions on variable bindings and collection mutability.",
                    estimate_minutes = 30,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "QUIZ"
                )
            )
            "aiml_01_math" -> listOf(
                FirestoreTask(
                    id = "${module.id}_task_1",
                    uid = uid,
                    module_id = module.id,
                    title = "Study: Vector Projections & Dot Product Geometry",
                    description = "Review cosine similarity derivation and matrix multiplication dimension rules.",
                    estimate_minutes = 45,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "READING"
                ),
                FirestoreTask(
                    id = "${module.id}_task_2",
                    uid = uid,
                    module_id = module.id,
                    title = "Implement Gradient Descent for Linear Regression from scratch",
                    description = "Write raw NumPy loop calculating MSE loss, analytical derivatives, and parameter updates.",
                    estimate_minutes = 60,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "PRACTICE"
                ),
                FirestoreTask(
                    id = "${module.id}_task_3",
                    uid = uid,
                    module_id = module.id,
                    title = "Complete Mathematics Foundation Quiz",
                    description = "Test understanding of learning rates, convex surfaces, and local vs global minima.",
                    estimate_minutes = 30,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "QUIZ"
                )
            )
            "llm_01_prompting" -> listOf(
                FirestoreTask(
                    id = "${module.id}_task_1",
                    uid = uid,
                    module_id = module.id,
                    title = "Deep-dive: Few-shot & Chain-of-Thought Patterns",
                    description = "Analyze exemplar structures that force reasoning tokens before final conclusion outputs.",
                    estimate_minutes = 40,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "READING"
                ),
                FirestoreTask(
                    id = "${module.id}_task_2",
                    uid = uid,
                    module_id = module.id,
                    title = "Build JSON Schema Extractor with Gemini API",
                    description = "Create a robust prompt that extracts candidate resumes into strict Pydantic JSON objects.",
                    estimate_minutes = 60,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "PRACTICE"
                ),
                FirestoreTask(
                    id = "${module.id}_task_3",
                    uid = uid,
                    module_id = module.id,
                    title = "Take Prompt Engineering Socratic Check",
                    description = "Test knowledge of system roles, temperature, top_p, and JSON mode guarantees.",
                    estimate_minutes = 30,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "QUIZ"
                )
            )
            "proj_01_rag_engine" -> listOf(
                FirestoreTask(
                    id = "${module.id}_task_1",
                    uid = uid,
                    module_id = module.id,
                    title = "Design Multimodal RAG System Architecture",
                    description = "Diagram ingestion pipeline, chunking policy, vector database selection, and retrieval fallback.",
                    estimate_minutes = 60,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "PROJECT"
                ),
                FirestoreTask(
                    id = "${module.id}_task_2",
                    uid = uid,
                    module_id = module.id,
                    title = "Implement ChromaDB Embedding Ingestion Pipeline",
                    description = "Write Python script reading PDFs and chunking with 10% overlap into Chroma collections.",
                    estimate_minutes = 75,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "PRACTICE"
                ),
                FirestoreTask(
                    id = "${module.id}_task_3",
                    uid = uid,
                    module_id = module.id,
                    title = "Setup RAGAS automated evaluation harness",
                    description = "Run ground-truth Q&A pairs through pipeline and calculate Answer Faithfulness score.",
                    estimate_minutes = 60,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "PROJECT"
                )
            )
            else -> listOf(
                FirestoreTask(
                    id = "${module.id}_task_1",
                    uid = uid,
                    module_id = module.id,
                    title = "Study: ${module.title} core concepts",
                    description = "Read ${module.skills.joinToString(", ")} architecture and code patterns.",
                    estimate_minutes = 45,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "READING"
                ),
                FirestoreTask(
                    id = "${module.id}_task_2",
                    uid = uid,
                    module_id = module.id,
                    title = "Hands-on Implementation on ${module.skills.firstOrNull() ?: "Core Logic"}",
                    description = "Write and verify working code sample applying the module principles.",
                    estimate_minutes = 60,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "PRACTICE"
                ),
                FirestoreTask(
                    id = "${module.id}_task_3",
                    uid = uid,
                    module_id = module.id,
                    title = "Complete ${module.title} Knowledge Quiz",
                    description = "Verify mastery with Tutor Agent evaluation.",
                    estimate_minutes = 30,
                    status = "todo",
                    scheduled_date = now,
                    task_type = "QUIZ"
                )
            )
        }
    }

    /**
     * Socratic quizzes for each module to support the Tutor Agent offline or as backup.
     */
    fun getSampleQuizForModule(moduleId: String): List<ModuleQuizQuestion> {
        return when (moduleId) {
            "python_01_basics" -> listOf(
                ModuleQuizQuestion(
                    id = "py1_q1",
                    question = "In Python, which of the following objects is mutable?",
                    options = listOf("tuple", "str", "list", "frozenset"),
                    correctIndex = 2,
                    explanation = "Lists in Python can have their elements mutated in-place without changing object identity."
                ),
                ModuleQuizQuestion(
                    id = "py1_q2",
                    question = "What happens when you execute `a = [1]; b = a; b += [2]`?",
                    options = listOf(
                        "`a` remains `[1]` and `b` is `[1, 2]`",
                        "Both `a` and `b` reference `[1, 2]`",
                        "A TypeError is raised",
                        "`b` becomes `[[1], 2]`"
                    ),
                    correctIndex = 1,
                    explanation = "`+=` on lists invokes `__iadd__`, which extends the list in-place. Since `a` and `b` reference the same memory address, both reflect the change."
                ),
                ModuleQuizQuestion(
                    id = "py1_q3",
                    question = "What is the average time complexity of looking up a key in a Python dictionary?",
                    options = listOf("O(n)", "O(log n)", "O(1)", "O(n log n)"),
                    correctIndex = 2,
                    explanation = "Python dictionaries are hash tables providing average O(1) key lookups."
                )
            )
            "aiml_01_math" -> listOf(
                ModuleQuizQuestion(
                    id = "aiml1_q1",
                    question = "In Gradient Descent, what happens if the learning rate is set excessively high?",
                    options = listOf(
                        "The model converges monotonically to the global minimum",
                        "The weights diverge and the loss oscillates or reaches NaN",
                        "Training stops immediately after 1 step",
                        "Vanishing gradient occurs"
                    ),
                    correctIndex = 1,
                    explanation = "An excessively high learning rate overshoots the minimum and causes numerical divergence."
                ),
                ModuleQuizQuestion(
                    id = "aiml1_q2",
                    question = "If two normalized vectors u and v have a dot product of 0, what is the geometric relationship between them?",
                    options = listOf("Parallel", "Opposite", "Orthogonal (90 degrees)", "Identical"),
                    correctIndex = 2,
                    explanation = "A dot product of 0 between non-zero vectors indicates cosine of 90 degrees, meaning they are orthogonal."
                ),
                ModuleQuizQuestion(
                    id = "aiml1_q3",
                    question = "Why is Mean Squared Error (MSE) typically preferred over Mean Absolute Error (MAE) when large outlier errors must be penalised heavily?",
                    options = listOf(
                        "MSE squares the differences, disproportionately penalizing large residuals",
                        "MSE is always smaller than MAE",
                        "MSE does not require differentiation",
                        "MAE only works with integer labels"
                    ),
                    correctIndex = 0,
                    explanation = "The quadratic penalty in (y - y_hat)^2 assigns drastically higher cost to large errors."
                )
            )
            "llm_01_prompting" -> listOf(
                ModuleQuizQuestion(
                    id = "llm1_q1",
                    question = "How does Chain-of-Thought (CoT) prompting primarily improve complex reasoning in LLMs?",
                    options = listOf(
                        "By increasing model parameter count during inference",
                        "By allocating output token compute to stepwise reasoning before generating the conclusion",
                        "By clearing model memory after every word",
                        "By forcing the model to query Wikipedia"
                    ),
                    correctIndex = 1,
                    explanation = "CoT gives the decoder intermediate scratchpad tokens where each step conditions subsequent tokens."
                ),
                ModuleQuizQuestion(
                    id = "llm1_q2",
                    question = "When requesting strict JSON output from an LLM, what is the safest practice?",
                    options = listOf(
                        "Add 'please only reply with JSON' and nothing else",
                        "Use Structured Outputs / JSON Schema validation with low temperature",
                        "Set temperature to 2.0",
                        "Prompt in binary encoding"
                    ),
                    correctIndex = 1,
                    explanation = "Structured outputs constrain grammar decoding directly to the provided schema."
                ),
                ModuleQuizQuestion(
                    id = "llm1_q3",
                    question = "What does the `temperature` parameter govern in LLM sampling?",
                    options = listOf(
                        "The execution speed of the GPU",
                        "The sharpness of the probability distribution over token logits",
                        "The number of input tokens allowed",
                        "The hardware heat limit"
                    ),
                    correctIndex = 1,
                    explanation = "Lower temperature sharpens probabilities toward the argmax (greedy), while higher temperature flattens them for entropy/creativity."
                )
            )
            else -> listOf(
                ModuleQuizQuestion(
                    id = "${moduleId}_q1",
                    question = "What is the primary architectural goal of this module?",
                    options = listOf(
                        "Build reliable, production-ready engineering patterns",
                        "Memorize syntax without understanding execution",
                        "Rely exclusively on deprecated libraries",
                        "Skip verification checks"
                    ),
                    correctIndex = 0,
                    explanation = "Production-grade development emphasizes verified, clean architecture."
                ),
                ModuleQuizQuestion(
                    id = "${moduleId}_q2",
                    question = "Why is modular decomposition preferred in scalable AI/ML pipelines?",
                    options = listOf(
                        "It makes debugging and automated unit testing straightforward",
                        "It increases latency",
                        "It prevents version control from tracking changes",
                        "It requires more disk space"
                    ),
                    correctIndex = 0,
                    explanation = "Decoupled modular components allow isolated testing, profiling, and maintainability."
                )
            )
        }
    }
}
