package com.yams.service;

import java.util.Map;

public class ScorecardSummary {
    private Map<String, Integer> scores;
    private int upperTotal;
    private int upperBonus;
    private int lowerTotal;
    private int total;

    public ScorecardSummary() {}

    public ScorecardSummary(Map<String, Integer> scores, int upperTotal, int upperBonus, int lowerTotal, int total) {
        this.scores = scores;
        this.upperTotal = upperTotal;
        this.upperBonus = upperBonus;
        this.lowerTotal = lowerTotal;
        this.total = total;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public int getUpperTotal() {
        return upperTotal;
    }

    public void setUpperTotal(int upperTotal) {
        this.upperTotal = upperTotal;
    }

    public int getUpperBonus() {
        return upperBonus;
    }

    public void setUpperBonus(int upperBonus) {
        this.upperBonus = upperBonus;
    }

    public int getLowerTotal() {
        return lowerTotal;
    }

    public void setLowerTotal(int lowerTotal) {
        this.lowerTotal = lowerTotal;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
