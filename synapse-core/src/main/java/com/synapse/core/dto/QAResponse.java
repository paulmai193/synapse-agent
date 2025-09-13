package com.synapse.core.dto;

import java.util.List;

public class QAResponse {
    
    private String question;
    private String answer;
    private String conversationId;
    private Float confidence;
    private List<SearchResult> sources;
    private long responseTimeMs;
    private String language;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    public List<SearchResult> getSources() {
        return sources;
    }

    public void setSources(List<SearchResult> sources) {
        this.sources = sources;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}