import java.util.HashSet;
import java.util.Set;

class DiscreteVariable implements RandomVariable {
  private final Integer numValues;
  private final String name;

  public DiscreteVariable(Integer numValues, String name) {
    this.numValues = numValues;
    this.name = name;
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

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DiscreteVariable that = (DiscreteVariable) o;

    if (!numValues.equals(that.numValues)) {
      return false;
    }
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    int result = numValues.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}