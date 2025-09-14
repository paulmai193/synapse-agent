package com.synapse.data.llm;

import java.util.List;
import java.util.Map;

public class PromptTemplate {
    
    public static final String QA_SYSTEM_PROMPT = """
            You are Synapse, an AI-powered knowledge management assistant. Your role is to provide accurate, helpful answers based on the provided context from internal documents.
            
            Guidelines:
            1. Answer questions using ONLY the information provided in the context
            2. If the context doesn't contain enough information, clearly state this limitation
            3. Cite specific sources when providing information
            4. Maintain a professional and helpful tone
            5. If asked in a specific language, respond in that same language
            6. Provide confidence levels for your answers when appropriate
            
            Context Documents:
            {context}
            
            Question: {question}
            
            Please provide a comprehensive answer based on the available context.
            """;
    
    public static final String SEARCH_QUERY_ENHANCEMENT = """
            Enhance the following search query to improve semantic search results. 
            Consider synonyms, related terms, and different phrasings that might help find relevant documents.
            
            Original query: {query}
            Language: {language}
            
            Enhanced query (return only the enhanced query, no explanation):
            """;
    
    public static final String MULTILINGUAL_QA_PROMPT = """
            You are a multilingual AI assistant. Answer the question in the same language it was asked.
            Use the provided context to give accurate information.
            
            Context: {context}
            Question: {question}
            
            Answer in {language}:
            """;
    
    public static final String UNCERTAINTY_RESPONSE = """
            Based on the available context, I cannot provide a complete answer to your question.
            
            Available information:
            {partial_context}
            
            Limitations:
            - The context may not contain all relevant information
            - Additional documents might be needed for a complete answer
            
            Suggestions:
            1. Try rephrasing your question with different keywords
            2. Check if you have access to additional relevant documents
            3. Contact the document authors for more specific information
            
            Would you like me to help you rephrase your question or suggest related topics to search for?
            """;

    public static String buildQAPrompt(String question, List<String> contextChunks, String language) {
        String context = String.join("\n\n", contextChunks);
        
        if (language != null && !language.equalsIgnoreCase("en")) {
            return MULTILINGUAL_QA_PROMPT
                    .replace("{context}", context)
                    .replace("{question}", question)
                    .replace("{language}", language);
        }
        
        return QA_SYSTEM_PROMPT
                .replace("{context}", context)
                .replace("{question}", question);
    }
    
    public static String buildSearchEnhancementPrompt(String query, String language) {
        return SEARCH_QUERY_ENHANCEMENT
                .replace("{query}", query)
                .replace("{language}", language != null ? language : "English");
    }
    
    public static String buildUncertaintyResponse(List<String> partialContext) {
        String context = partialContext.isEmpty() ? 
                "No relevant context found in available documents." :
                String.join("\n", partialContext);
                
        return UNCERTAINTY_RESPONSE.replace("{partial_context}", context);
    }
    
    public static String formatSourceCitation(String documentTitle, String chunkContent, double confidence) {
        return String.format(
                "**Source:** %s (Confidence: %.1f%%)\n**Content:** %s\n",
                documentTitle,
                confidence * 100,
                chunkContent.length() > 200 ? chunkContent.substring(0, 200) + "..." : chunkContent
        );
    }
    
    public static String buildContextWithCitations(List<Map<String, Object>> contextChunks) {
        StringBuilder contextBuilder = new StringBuilder();
        
        for (int i = 0; i < contextChunks.size(); i++) {
            Map<String, Object> chunk = contextChunks.get(i);
            String content = (String) chunk.get("content");
            String documentTitle = (String) chunk.get("documentTitle");
            Double confidence = (Double) chunk.getOrDefault("confidence", 0.8);
            
            contextBuilder.append(String.format("[Source %d] %s\n", i + 1, documentTitle));
            contextBuilder.append(content);
            contextBuilder.append(String.format("\n(Confidence: %.1f%%)\n\n", confidence * 100));
        }
        
        return contextBuilder.toString();
    }
}