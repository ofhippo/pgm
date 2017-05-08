import java.util.Set;

public class Cluster {
  private final Set<Factor> factors;

  public Cluster(Set<Factor> factors) {
    this.factors = factors;
  }

  public Set<Factor> getFactors() {
    return factors;
  }
}
