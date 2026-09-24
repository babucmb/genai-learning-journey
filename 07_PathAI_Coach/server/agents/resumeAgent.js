const axios = require('axios');
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || '';

/**
 * Resume Agent: Tailors resume action bullets and highlights to match a specific Job Description.
 */
async function tailorResumeToJob({ jobDescription, jobTitle, company, userSkills, userProjects }) {
  if (!GEMINI_API_KEY) {
    return `• Engineered end-to-end Python / ML pipelines aligned with ${company}'s ${jobTitle} stack, optimizing data preprocessing latency by 35%.\n• Designed and evaluated deep learning models with PyTorch, validating against rigorous benchmark metrics.\n• Collaborated on RESTful API integration for low-latency model inference in production environments.`;
  }

  try {
    const prompt = `
You are the Resume Agent in PathAI's Multi-Agent Career Platform.
Your task is to produce 3-4 high-impact, ATS-optimized resume bullet points tailored specifically for:
Role: ${jobTitle} at ${company}
Job Description: ${jobDescription}
Candidate Skills: ${(userSkills || []).join(', ')}

Guidelines:
- Use strong action verbs (Architected, Engineered, Implemented, Benchmarked).
- Include concrete quantification (e.g., % improvement, latency reduction, accuracy).
- Highlight direct skill overlaps.
- Format with clean bullet points (•).
`;

    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}`;
    const resp = await axios.post(url, {
      contents: [{ parts: [{ text: prompt }] }]
    }, { timeout: 12000 });

    return resp.data?.candidates?.[0]?.content?.parts?.[0]?.text || 'No response generated.';
  } catch (err) {
    console.warn('[ResumeAgent] Gemini error:', err.message);
    return `• Implemented modular Python & ML workflows aligned with ${jobTitle} requirements at ${company}.\n• Evaluated deep learning architectures in PyTorch, focusing on generalization and clean reproducibility.`;
  }
}

module.exports = {
  tailorResumeToJob
};
