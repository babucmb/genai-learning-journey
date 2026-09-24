const GeminiProvider = require('./geminiProvider');
const HuggingFaceProvider = require('./huggingFaceProvider');

class LLMProviderManager {
  constructor() {
    this.geminiProvider = new GeminiProvider();
    this.huggingFaceProvider = new HuggingFaceProvider();
  }

  getActiveProviderName() {
    const requested = (process.env.LLM_PROVIDER || 'gemini').toLowerCase().trim();
    if (requested === 'huggingface') {
      return 'huggingface';
    }
    return 'gemini';
  }

  getActiveProvider() {
    const providerName = this.getActiveProviderName();
    if (providerName === 'huggingface') {
      return this.huggingFaceProvider;
    }
    return this.geminiProvider;
  }

  async generateText(request) {
    const provider = this.getActiveProvider();
    return await provider.generateText(request);
  }

  async generateStructured(request, schema = null) {
    const provider = this.getActiveProvider();
    return await provider.generateStructured({ ...request, schema });
  }

  async streamText(request) {
    const provider = this.getActiveProvider();
    return await provider.streamText(request);
  }

  async getProviderHealth() {
    const providerName = this.getActiveProviderName();
    const activeProvider = this.getActiveProvider();
    const health = await activeProvider.getProviderHealth();

    return {
      activeProvider: providerName,
      ...health,
      timestamp: new Date().toISOString()
    };
  }
}

// Export singleton instance as well as Class for testing
const defaultManager = new LLMProviderManager();

module.exports = {
  LLMProviderManager,
  generateText: (req) => defaultManager.generateText(req),
  generateStructured: (req, schema) => defaultManager.generateStructured(req, schema),
  streamText: (req) => defaultManager.streamText(req),
  getProviderHealth: () => defaultManager.getProviderHealth(),
  getActiveProviderName: () => defaultManager.getActiveProviderName()
};
