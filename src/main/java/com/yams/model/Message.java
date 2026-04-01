package com.yams.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: contenu, auteur, timestamp, partie associée

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
