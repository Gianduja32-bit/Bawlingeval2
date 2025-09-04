package com.estiam.tarbes.cours_jee;

import org.springframework.stereotype.Component;

@Component
public class TaxCalculator {
    
    public double computeTax(double income){
        return 325.8;
    }
}
