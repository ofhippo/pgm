import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class TableFactorTest {

  @Test
  public void evaluate() throws Exception {
    DiscreteVariable x = new DiscreteVariable(2);
    DiscreteVariable y = new DiscreteVariable(2);

    final Set<Assignment> xAssignments = x.allAssignments();
    final Set<Assignment> yAssignments = y.allAssignments();

    Map<Set<Assignment>, Double> table = new HashMap<>();

    for (Assignment xAssignment : xAssignments) {
      for (Assignment yAssignment : yAssignments) {
        table.put(ImmutableSet.of(xAssignment, yAssignment),
            (double) (xAssignment.getValue() ^ yAssignment.getValue()));
      }
    }

    TableFactor cpd = new TableFactor(table);
    assertThat(cpd.scope).isEqualTo(ImmutableSet.of(x, y));
    for (Set<Assignment> assignments : cpd.table.keySet()) {
      final Integer[] values = assignments.stream().map(Assignment::getValue).toArray(Integer[]::new);
      assertThat(cpd.evaluate(assignments)).isEqualTo(values[0] ^ values[1]);
    }
  }

}