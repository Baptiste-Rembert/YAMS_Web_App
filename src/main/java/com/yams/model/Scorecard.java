package com.yams.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Scorecard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: definition des cases et relations

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
