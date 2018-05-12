package com.himebaugh.popularmovies.model;

import java.util.List;

public class Result {

    // *************************************************************
    // NOT USING THIS ANYMORE
    // Was using with MovieUtils.parseMovieResultsJson
    // NOT necessary as now getting directly to the "results" with...
    // Map<String, Object> map = gson.fromJson(moviesJsonResults, new TypeToken<Map<String, Object>>(){}.getType());
    // map.get("results")
    // *************************************************************

    private int page;
    private List<String> results = null;
    private int totalResults;
    private int totalPages;

    public Result() {
    }

    public Result(List<String> results) {
        this.results = results;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
