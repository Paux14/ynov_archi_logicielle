package com.ynov.coworking.member.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType subscriptionType;

    @Column(nullable = false)
    private boolean suspended = false;

    @Column(nullable = false)
    private Integer maxConcurrentBookings;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public SubscriptionType getSubscriptionType() { return subscriptionType; }
    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
        // auto-dérive le max selon le type d'abonnement
        if (subscriptionType != null) {
            this.maxConcurrentBookings = switch (subscriptionType) {
                case BASIC -> 2;
                case PRO -> 5;
                case ENTERPRISE -> 10;
            };
        }
    }

    public boolean isSuspended() { return suspended; }
    public void setSuspended(boolean suspended) { this.suspended = suspended; }

    public Integer getMaxConcurrentBookings() { return maxConcurrentBookings; }
    public void setMaxConcurrentBookings(Integer maxConcurrentBookings) {
        this.maxConcurrentBookings = maxConcurrentBookings;
    }
}
