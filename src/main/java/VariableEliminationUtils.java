import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VariableEliminationUtils {
  // Koller page 298
  public static Set<TableFactor> sumProductEliminateVariable(Set<TableFactor> factors, DiscreteVariable z) {
    Set<TableFactor> factorsWithZ = factors.stream().filter(tableFactor -> tableFactor.scope.contains(z)).collect(
        Collectors.toSet());

    Set<TableFactor> results = new HashSet<>(factors);
    results.removeAll(factorsWithZ);
    results.add(TableFactor.product(factorsWithZ).marginalizeOut(z));
    return results;
  }

  public static TableFactor sumProductVariableElimination(Set<TableFactor> factors, List<DiscreteVariable> variables) {
    Set<TableFactor> phi = new HashSet<>(factors);
    for (DiscreteVariable variable : variables) {
      phi = sumProductEliminateVariable(phi, variable);
    }
    return TableFactor.product(phi);
  }
}
