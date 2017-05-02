import java.util.HashSet;
import java.util.Set;

class DiscreteVariable implements RandomVariable {
  private final Integer numValues;

  public DiscreteVariable(Integer numValues) {
    this.numValues = numValues;
  }

  public boolean isValidAssignment(int value) {
    return value < numValues;
  }

  public Set<Assignment> allAssignments() {
   Set<Assignment> results = new HashSet<>(numValues);
    for (int i = 0; i < numValues; i++) {
      results.add(new Assignment(this, i));
    }
    return results;
  }
}