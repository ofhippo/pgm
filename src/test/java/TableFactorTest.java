import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class TableFactorTest {

  private TableFactor xor;
  private DiscreteVariable x = new DiscreteVariable(2, "X");
  private DiscreteVariable y = new DiscreteVariable(2, "Y");
  private TableFactor xPlusTenY;

  @Before
  public void setup() {
    final Set<Assignment> xAssignments = x.allAssignments();
    final Set<Assignment> yAssignments = y.allAssignments();

    Map<Set<Assignment>, Double> xor = new HashMap<>();
    for (Assignment xAssignment : xAssignments) {
      for (Assignment yAssignment : yAssignments) {
        xor.put(ImmutableSet.of(xAssignment, yAssignment),
            (double) (xAssignment.getValue() ^ yAssignment.getValue()));
      }
    }
    this.xor = new TableFactor(xor);


    Map<Set<Assignment>, Double> xPlusTenY = new HashMap<>();
    for (Assignment xAssignment : xAssignments) {
      for (Assignment yAssignment : yAssignments) {
        xPlusTenY.put(ImmutableSet.of(xAssignment, yAssignment),
            (double) (xAssignment.getValue() + 10 * yAssignment.getValue()));
      }
    }
    this.xPlusTenY = new TableFactor(xPlusTenY);
  }

  @Test
  public void evaluate() throws Exception {
    assertThat(xor.scope).isEqualTo(ImmutableSet.of(x, y));
    for (Set<Assignment> assignments : xor.table.keySet()) {
      final Integer[] values = assignments.stream().map(Assignment::getValue).toArray(Integer[]::new);
      assertThat(xor.evaluate(assignments)).isEqualTo(values[0] ^ values[1]);
      assertThat(xPlusTenY.evaluate(assignments)).isEqualTo(values[0] + 10 * values[1]);
    }
  }

  @Test
  public void marginalize() {
    final TableFactor xMarginalXor = xor.marginalizeOut(y);
    assertThat(xMarginalXor.table.size()).isEqualTo(2);
    assertThat(xMarginalXor.scope).isEqualTo(ImmutableSet.of(x));
    assertThat(xMarginalXor.table.values().toArray()).isEqualTo(new double[] { 1.0, 1.0});

    final TableFactor xMarginalPlus = xPlusTenY.marginalizeOut(y);
    assertThat(xMarginalPlus.table.size()).isEqualTo(2);
    assertThat(xMarginalPlus.scope).isEqualTo(ImmutableSet.of(x));
    assertThat(xMarginalPlus.table.values().toArray()).isEqualTo(new double[] { 10.0, 12.0});
  }
}