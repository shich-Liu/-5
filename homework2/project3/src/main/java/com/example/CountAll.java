package com.example;

public class CountAll {
    private Long sum;  
    private Double[] value;  
    private int k;  
    public CountAll(){}  
    public CountAll(Long sum, Double[] value,int k){  
        this.sum = sum;  
        this.value = value;  
        this.k = k;  
    }  
    public Double[] getValue() {  
        return value;  
    }  
    public void setValue(Double[] value) {  
        this.value = value;  
    }  
    public Long getSum() {  
        return sum;  
    }  
    public void setSum(Long sum) {  
        this.sum = sum;  
    }  
    public int getK() {  
        return k;  
    }  
    public void setK(int k) {  
        this.k = k;  
    }  
}  