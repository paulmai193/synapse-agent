import { useState, useEffect } from 'react';
import { apiClient } from '../utils/api';
import { API_CONFIG } from '../config/api';

interface SearchAnalytics {
  query: string;
  timestamp: string;
  resultsCount: number;
  clickedResults: string[];
  language: string;
}

export const useSearchAnalytics = () => {
  const [analytics, setAnalytics] = useState<SearchAnalytics[]>([]);

  const trackSearch = async (query: string, resultsCount: number, language: string = 'en') => {
    const searchData: SearchAnalytics = {
      query,
      timestamp: new Date().toISOString(),
      resultsCount,
      clickedResults: [],
      language,
    };

    // Store locally for immediate use
    setAnalytics(prev => [searchData, ...prev.slice(0, 99)]); // Keep last 100 searches

    // Send to backend for analytics
    try {
      await apiClient.post('/analytics/search', searchData);
    } catch (error) {
      console.error('Failed to track search analytics:', error);
    }
  };

  const trackResultClick = async (query: string, documentId: string) => {
    try {
      await apiClient.post('/analytics/search/click', {
        query,
        documentId,
        timestamp: new Date().toISOString(),
      });

      // Update local analytics
      setAnalytics(prev =>
        prev.map(item =>
          item.query === query
            ? { ...item, clickedResults: [...item.clickedResults, documentId] }
            : item
        )
      );
    } catch (error) {
      console.error('Failed to track result click:', error);
    }
  };

  const getPopularQueries = () => {
    const queryCount = analytics.reduce((acc, item) => {
      acc[item.query] = (acc[item.query] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);

    return Object.entries(queryCount)
      .sort(([, a], [, b]) => b - a)
      .slice(0, 10)
      .map(([query, count]) => ({ query, count }));
  };

  const getLanguageDistribution = () => {
    const langCount = analytics.reduce((acc, item) => {
      acc[item.language] = (acc[item.language] || 0) + 1;
      return acc;
    }, {} as Record<string, number>);

    return Object.entries(langCount).map(([language, count]) => ({ language, count }));
  };

  return {
    analytics,
    trackSearch,
    trackResultClick,
    getPopularQueries,
    getLanguageDistribution,
  };
};