package com.synapse.data.llm;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class LLMRequest {
    
    private String model;
    private String prompt;
    private List<String> context;
    private Options options;
    private boolean stream = false;

    public LLMRequest() {}

    public LLMRequest(String model, String prompt, List<String> context) {
        this.model = model;
        this.prompt = prompt;
        this.context = context;
        this.options = new Options();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<String> getContext() {
        return context;
    }

    public void setContext(List<String> context) {
        this.context = context;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public static class Options {
        @JsonProperty("num_predict")
        private int numPredict = 2048;
        
        private double temperature = 0.7;
        
        @JsonProperty("top_k")
        private int topK = 40;
        
        @JsonProperty("top_p")
        private double topP = 0.9;

        public int getNumPredict() {
            return numPredict;
        }

        public void setNumPredict(int numPredict) {
            this.numPredict = numPredict;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public double getTopP() {
            return topP;
        }

        public void setTopP(double topP) {
            this.topP = topP;
        }
    }
}