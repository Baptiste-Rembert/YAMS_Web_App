package com.yams.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: invitation vers salon privé, statut

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
