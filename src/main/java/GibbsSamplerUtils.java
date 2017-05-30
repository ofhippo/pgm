import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GibbsSamplerUtils {
  static List<Set<Assignment>> run(Set<TableFactor> factors, Set<Assignment> initialAssignments, List<Variable> variablesWithoutEvidence, int numIterations) {
    Set<Assignment> assignments = initialAssignments;
    List<Set<Assignment>> samples = new ArrayList<>();

    for (int i = 0; i < numIterations; i++) {
      assignments = GibbsSamplerUtils.runOneIteration(factors, variablesWithoutEvidence, assignments);
      samples.add(assignments);
    }
    return samples;
  }

  static Set<Assignment> runOneIteration(Set<TableFactor> factors, List<Variable> variablesWithoutEvidence, Set<Assignment> initialAssignments) {
    Set<Assignment> currentAssignments = initialAssignments;
    for(Variable variable : variablesWithoutEvidence) {
      Set<Assignment> otherAssignments = currentAssignments.stream().filter(a -> a.getVariable() != variable).collect(Collectors.toSet());
      currentAssignments = sampleOneVariable(factors, variable, otherAssignments);
    }
    return currentAssignments;
  }

  static Set<Assignment> sampleOneVariable(Set<TableFactor> factors, Variable variableToSample, Set<Assignment> others) {
    Set<TableFactor> reducedFactorsWithVariable = factors.stream()
        .filter(f -> f.scope.contains(variableToSample))
        .map(f -> f.reduce(others)).collect(Collectors.toSet());

    Set<Assignment> results = new HashSet<>(others);
    final TableFactor productOfReducedFactorsWithVariable = TableFactor.product(reducedFactorsWithVariable);
    final Assignment sampleAssignment = productOfReducedFactorsWithVariable.renormalize().sample(others).stream()
        .filter(a -> a.getVariable() == variableToSample).findAny().get();
    results.add(sampleAssignment);
    return results;
  }
}
