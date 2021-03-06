import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class TableFactor implements Factor {

  public final Map<Set<Assignment>, Double> table;
  public Set<Variable> scope = null;
  public static Random random = new Random(123456);

  public TableFactor(Map<Set<Assignment>, Double> table) {
    for (Set<Assignment> fullAssignment : table.keySet()) {
      final Set<Variable> assignmentScope = fullAssignment.stream().map(
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

  public TableFactor marginalizeOut(Variable variable) {
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
    Set<Variable> variableIntersection = Sets.intersection(this.scope, other.scope);
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

  static TableFactor product(Set<TableFactor> factorsOriginal) {
    List<TableFactor> factors = new ArrayList<>(factorsOriginal);
    if (factors.size() == 0) {
      throw new IllegalArgumentException("Can't product nothin'");
    }

    if (factors.size() == 1) {
      return factors.get(0);
    } else {
      final List<TableFactor> results = factors.subList(2, factors.size());
      results.add(factors.get(0).product(factors.get(1)));
      return product(new HashSet(results));
    }
  }

  // Koller page 111
  public TableFactor reduce(Assignment assignment) {
    Map<Set<Assignment>, Double> results = new HashMap<>();

    for (Set<Assignment> assignments : table.keySet()) {
      if (assignments.contains(assignment)) {
        results.put(assignments, table.get(assignments));
      }
    }


    return new TableFactor(results);
  }

  // Koller page 111
  public TableFactor reduce(Set<Assignment> uAssignments) {
    Set<Assignment> uPrimeAssignments = uAssignments.stream().filter(assignment -> this.scope.contains(assignment.getVariable())).collect(
        Collectors.toSet());

    TableFactor results = this;
    for (Assignment assignment : uPrimeAssignments) {
      results = results.reduce(assignment);
    }

    return results;
  }

  public Set<Assignment> sample(Set<Assignment> evidence) {
    return reduce(evidence).sample();
  }

  public Set<Assignment> sample() {
    double sum = table.values().stream().mapToDouble(Double::valueOf).sum();
    double target = random.nextDouble() * sum;

    double seen = 0;
    for (Entry<Set<Assignment>, Double> entry : table.entrySet()) {
      seen += entry.getValue();

      if (seen >= target) {
        return entry.getKey();
      }
    }
    throw new RuntimeException("Should be unreachable");
  }

  public TableFactor renormalize() {
    double z = table.values().stream().reduce(0d, (a, b) -> a + b);

    Map<Set<Assignment>, Double> results = new HashMap<>();
    for (Set<Assignment> assignments : table.keySet()) {
        results.put(assignments, table.get(assignments) / z);
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
