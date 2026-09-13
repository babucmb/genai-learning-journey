# LoRA Fine-Tuning 
# A Base Model

In this experiment, I learned how to fine-tune an open-source language model using **LoRA (Low-Rank Adaptation)** with a small instruction dataset.

## What I Learned

* What fine-tuning means for Large Language Models
* Difference between full fine-tuning and parameter-efficient fine-tuning
* How **LoRA** trains a small number of additional parameters instead of updating the complete model
* How to prepare and inspect an instruction dataset
* How to use **Hugging Face Transformers, Datasets, PEFT and TRL**
* How to train a model using `SFTTrainer`
* How to save and load a trained LoRA adapter
* How to compare the base model with the fine-tuned model
* How to verify whether the fine-tuned model has learned from the training data

## Tools & Technologies

* Python
* Google Colab + GPU
* Hugging Face Transformers
* Hugging Face Datasets
* PEFT
* TRL
* LoRA
* Supervised Fine-Tuning (SFT)

## Model & Dataset

**Base Model:** `Qwen/Qwen2.5-0.5B-Instruct`

**Dataset:** `trl-lib/Capybara`

For the experiment, I used a small subset of the dataset to make the training practical on Google Colab.

## Experiment

```text
Dataset
   ↓
Dataset Preparation
   ↓
Qwen 0.5B Base Model
   ↓
LoRA Fine-Tuning
   ↓
LoRA Adapter
   ↓
Base Model + LoRA Adapter
   ↓
Compare Before vs After
```

## Result

I compared the original Qwen model with the fine-tuned model using the same questions.

For example, for:

> "Recommend a movie to watch."

The base model recommended a different movie, while the fine-tuned model produced a response consistent with an example from the training dataset.

This helped me understand how LoRA fine-tuning can change a model's response behavior without training the entire model.

## Notebooks

* `01_dataset_preparation.ipynb`
* `02_sft_basics.ipynb`
* `03_lora_fine_tuning.ipynb`
* `04_model_evaluation.ipynb`
