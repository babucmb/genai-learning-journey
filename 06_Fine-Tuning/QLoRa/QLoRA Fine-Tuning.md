# QLoRA Fine-Tuning

In this experiment, I learned how to fine-tune an open-source language model using **QLoRA (Quantized Low-Rank Adaptation)** with a small instruction dataset.

## What I Learned

* What QLoRA is and how it differs from LoRA
* How **4-bit quantization** reduces GPU memory usage
* How QLoRA combines **quantization + LoRA**
* How to fine-tune a model efficiently on limited GPU resources
* How to use `bitsandbytes` for 4-bit model quantization
* How to configure LoRA adapters with a quantized model
* How to save and load a QLoRA adapter
* How to test the fine-tuned model

## Tools & Technologies

* Python
* Google Colab + GPU
* Hugging Face Transformers
* Hugging Face Datasets
* PEFT
* TRL
* BitsAndBytes
* QLoRA
* Supervised Fine-Tuning (SFT)

## Model & Dataset

**Base Model:** `Qwen/Qwen2.5-0.5B-Instruct`

**Dataset:** `trl-lib/Capybara`

A small subset of the dataset was used to make the experiment practical on Google Colab.

## Experiment

```text
Dataset
   ↓
4-bit Quantization
   ↓
Qwen 0.5B
   ↓
LoRA Adapter
   ↓
QLoRA Fine-Tuning
   ↓
Save Adapter
   ↓
Load & Test
```

## Result

The QLoRA-trained model was successfully trained and tested using the same instruction dataset.

This experiment helped me understand how **QLoRA combines 4-bit quantization with LoRA to perform memory-efficient fine-tuning on limited GPU resources.**
