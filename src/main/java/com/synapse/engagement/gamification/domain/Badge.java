package com.synapse.engagement.gamification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "badges")
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 30)
    private BadgeConditionType conditionType;

    @Column(name = "condition_value", nullable = false)
    private int conditionValue;

    protected Badge() {
    }

    private Badge(
            String code,
            String name,
            String description,
            String iconUrl,
            BadgeConditionType conditionType,
            int conditionValue
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
    }

    public static Badge create(
            String code,
            String name,
            String description,
            String iconUrl,
            BadgeConditionType conditionType,
            int conditionValue
    ) {
        return new Badge(code, name, description, iconUrl, conditionType, conditionValue);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public BadgeConditionType getConditionType() {
        return conditionType;
    }

    public int getConditionValue() {
        return conditionValue;
    }
}
