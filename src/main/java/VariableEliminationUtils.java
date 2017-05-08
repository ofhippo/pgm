import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VariableEliminationUtils {
  // Koller page 298
  public static Set<TableFactor> sumProductEliminateVariable(Set<TableFactor> factors, Variable z) {
    Set<TableFactor> factorsWithZ = factors.stream().filter(tableFactor -> tableFactor.scope.contains(z)).collect(
        Collectors.toSet());

    Set<TableFactor> results = new HashSet<>(factors);
    results.removeAll(factorsWithZ);
    results.add(TableFactor.product(factorsWithZ).marginalizeOut(z));
    return results;
  }

  public static TableFactor sumProductVariableElimination(Set<TableFactor> factors, List<Variable> variablesToEliminate) {
    Set<TableFactor> phi = new HashSet<>(factors);
    for (Variable variable : variablesToEliminate) {
      phi = sumProductEliminateVariable(phi, variable);
    }
    return TableFactor.product(phi);
  }

  // Koller page 304
  // variables to eliminate should be (all variables) - (query variables) - (evidence variables)
  public static TableFactor conditionalProbVariableElimination(Set<TableFactor> factors, Set<Assignment> evidence, List<Variable> variablesToEliminate) {
    Set<TableFactor> conditionedFactors = factors.stream().map(factor -> factor.reduce(evidence)).collect(
        Collectors.toSet());
    final Set<Variable> evidenceVariables = evidence.stream().map(
        Assignment::getVariable).collect(Collectors.toSet());

    List<Variable> variablesToEliminateWithEvidence = new ArrayList<>(variablesToEliminate);
    variablesToEliminateWithEvidence.addAll(evidenceVariables);
    return sumProductVariableElimination(conditionedFactors, variablesToEliminateWithEvidence);
  }
}
