package org.acme;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SimpleAlgorithm {

  private double sensitivity = 0.0001;

  private Double previous;

  public SimpleAlgorithm() {
  }

  public SimpleAlgorithm(double sensitivity) {
    this.sensitivity = sensitivity;
  }
  
  public Action predict(double value) {
    Double reference = previous;
    this.previous = value;

    if (reference != null && value < reference * (1 - sensitivity)) {
      return new Action("buy", value);
    } else if (reference != null && value > reference * (1 + sensitivity)) {
      return new Action("sell", value);
    }
    return null;
  }

}
