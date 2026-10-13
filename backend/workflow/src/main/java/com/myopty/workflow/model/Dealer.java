package com.myopty.workflow.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("DEALER")
public class Dealer {

    @Id
    private Integer dealerId;

    private String name;

    private String type;

    private String email;

    private String phone;

    private boolean active;

    private java.time.LocalDate createdAt;

    private java.time.LocalDate updatedAt;

    public Dealer() {
        this.active = true;
    }

    public Integer getDealerId() { return dealerId; }
    public void setDealerId(Integer dealerId) { this.dealerId = dealerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public java.time.LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDate createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDate updatedAt) { this.updatedAt = updatedAt; }
}