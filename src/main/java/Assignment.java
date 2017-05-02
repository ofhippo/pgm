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
}
