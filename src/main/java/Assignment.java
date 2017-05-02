import com.google.common.base.Preconditions;

public class Assignment {
  private final RandomVariable variable;
  private final int value;

  public Assignment(RandomVariable variable, int value) {
    Preconditions.checkArgument(variable.isValidAssignment(value));
    this.variable = variable;
    this.value = value;
  }

  public RandomVariable getVariable() {
    return variable;
  }

  public int getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Assignment that = (Assignment) o;

    if (value != that.value) {
      return false;
    }
    return variable.equals(that.variable);
  }

  @Override
  public int hashCode() {
    int result = variable.hashCode();
    result = 31 * result + value;
    return result;
  }
}
