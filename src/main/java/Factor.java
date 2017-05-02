import java.util.Set;

public interface Factor {
  double evaluate(Set<Assignment> assignment);
}
