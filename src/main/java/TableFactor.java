import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
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

  public TableFactor product(TableFactor other) {
    Map<Set<Assignment>, Double> results = new HashMap<>();
    Set<RandomVariable> variableIntersection = Sets.intersection(this.scope, other.scope);
    for (Set<Assignment> myAssignments : this.table.keySet()) {
      final Set<Assignment> relevantAssignments = myAssignments.stream()
          .filter(assignment -> variableIntersection.contains(assignment.getVariable())).collect(
              Collectors.toSet());
      for (Set<Assignment> theirAssignments : other.table.keySet()) {
        if (theirAssignments.containsAll(relevantAssignments)) {
          results.put(Sets.union(myAssignments, theirAssignments), this.evaluate(myAssignments) * other.evaluate(theirAssignments));
        }
      }
    }
    return new TableFactor(results);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TableFactor that = (TableFactor) o;

    if (!table.equals(that.table)) {
      return false;
    }
    return scope != null ? scope.equals(that.scope) : that.scope == null;
  }

  public boolean equals(Object o, double epsilon) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TableFactor that = (TableFactor) o;

    if (!table.keySet().equals(that.table.keySet())) {
      return false;
    }

    for (Set<Assignment> assignments : table.keySet()) {
      if (!that.table.keySet().contains(assignments)) {
        return false;
      }
      if (Math.abs(table.get(assignments) - that.table.get(assignments)) > epsilon) {
        return false;
      }
    }
    return scope != null ? scope.equals(that.scope) : that.scope == null;
  }

  @Override
  public int hashCode() {
    int result = table.hashCode();
    result = 31 * result + (scope != null ? scope.hashCode() : 0);
    return result;
  }
}
