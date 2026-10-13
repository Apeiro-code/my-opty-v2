package com.myopty.billing;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_method")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int methodId;

    public String name;
    public String provider;

    public void verifyMethod() {
        System.out.println("Verifying payment method: " + this.name + " (" + this.provider + ")");
    }
}
