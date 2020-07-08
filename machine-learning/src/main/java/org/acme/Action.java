package org.acme;

public class Action {
    
    private String operation;
    private double value;

    public Action(String operation, double value) {
        this.operation = operation;
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public double getValue() {
        return value;
    }

    public String toString() {
        return operation + " (" + value + ")";
    }

}
