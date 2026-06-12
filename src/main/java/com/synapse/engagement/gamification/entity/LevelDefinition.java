package com.synapse.engagement.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "level_definitions")
public class LevelDefinition {

    @Id
    private UUID id;

    @Column(name = "level_number", nullable = false, unique = true)
    private int levelNumber;

    @Column(name = "min_xp", nullable = false, unique = true)
    private int minXp;

    @Column(nullable = false, length = 50)
    private String title;

    protected LevelDefinition() {
    }

    public int levelNumber() {
        return levelNumber;
    }

    public int minXp() {
        return minXp;
    }

    public String title() {
        return title;
    }
}
