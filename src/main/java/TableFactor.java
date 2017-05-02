import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TableFactor implements Factor {

  public final Map<Set<Assignment>, Double> table;
  public Set<RandomVariable> scope = null;

  public TableFactor(Map<Set<Assignment>, Double> table) {
    for (Set<Assignment> fullAssignment : table.keySet()) {
      final Set<RandomVariable> assignmentScope = fullAssignment.stream().map(
          Assignment::getVariable)
          .collect(Collectors.toSet());

      if (this.scope == null) {
        this.scope = assignmentScope;
      } else {
        Preconditions.checkArgument(assignmentScope.equals(scope), "All rows must have full scope");
      }
    }
    this.table = table;
  }

  public double evaluate(Set<Assignment> assignment) {
    Preconditions.checkArgument(assignment.stream().map(Assignment::getVariable).collect(Collectors.toSet()).equals(scope), "Must evaluate on full scope");
    return table.get(assignment);
  }

  public TableFactor marginalizeOut(DiscreteVariable variable) {
    Map<Set<Assignment>, Double> results = new HashMap<>();
    for (Set<Assignment> assignments : table.keySet()) {
      Set<Assignment> otherAssignments = new HashSet<>();
      for (Assignment assignment : assignments) {
        if (!assignment.getVariable().equals(variable)) {
          otherAssignments.add(assignment);
        }
      }

      final Double priorValue = results.get(otherAssignments);
      results.put(otherAssignments, ((priorValue == null) ? 0 : priorValue) + table.get(assignments));
    }
    return new TableFactor(results);
  }
}
