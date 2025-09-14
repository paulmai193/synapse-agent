package com.synapse.core.dto;

import java.util.List;

public class SearchResponse {
    
    private String query;
    private List<SearchResult> results;
    private int totalResults;
    private long searchTimeMs;
    private String language;
    private String cursor; // Next cursor for pagination
    private boolean hasMore; // Whether there are more results
    private int offset;
    private int limit;

    public SearchResponse() {}

    public SearchResponse(String query, List<SearchResult> results, int totalResults, long searchTimeMs) {
        this.query = query;
        this.results = results;
        this.totalResults = totalResults;
        this.searchTimeMs = searchTimeMs;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public long getSearchTimeMs() {
        return searchTimeMs;
    }

    public void setSearchTimeMs(long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
    
    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
    
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}