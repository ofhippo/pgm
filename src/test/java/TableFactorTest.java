import static org.assertj.core.api.Assertions.fail;
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
  private DiscreteVariable z = new DiscreteVariable(3, "Z");
  private TableFactor xPlusTenY;
  private TableFactor multiplied;

  @Before
  public void setup() {
    final Set<Assignment> xAssignments = x.allAssignments();
    final Set<Assignment> yAssignments = y.allAssignments();
    final Set<Assignment> zAssignments = z.allAssignments();

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

    Map<Set<Assignment>, Double> multiplied = new HashMap();
    for (Assignment xAssignment : xAssignments) {
      for (Assignment yAssignment : yAssignments) {
        for (Assignment zAssignment : zAssignments) {
          multiplied.put(ImmutableSet.of(xAssignment, yAssignment, zAssignment),
              (double) (xAssignment.getValue() * yAssignment.getValue() * zAssignment.getValue()));
        }
      }
    }
    this.multiplied = new TableFactor(multiplied);
  }

  @Test
  public void evaluate() throws Exception {
    assertThat(xor.scope).isEqualTo(ImmutableSet.of(x, y));
    for (Set<Assignment> assignments : xor.table.keySet()) {
      final Integer[] values = assignments.stream().map(Assignment::getValue).toArray(Integer[]::new);
      assertThat(xor.evaluate(assignments)).isEqualTo(values[0] ^ values[1]);
      assertThat(xPlusTenY.evaluate(assignments)).isEqualTo(values[0] + 10 * values[1]);

      try {
        multiplied.evaluate(assignments);
        fail("should fail evaluating on not full scope");
      } catch (IllegalArgumentException e) {
        assertThat(e.getMessage()).isEqualTo("Must evaluate on full scope");
      }
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

    final TableFactor yMarginalizedOut = multiplied.marginalizeOut(y);
    assertThat(yMarginalizedOut.table.size()).isEqualTo(6);
    assertThat(yMarginalizedOut.scope).isEqualTo(ImmutableSet.of(x, z));
    assertThat(yMarginalizedOut.table.values().toArray()).isEqualTo(new double[] { 0d*0d, 0d*1d, 0d*2d, 1d*0d, 1d*1d, 1d*2d});
    assertThat(yMarginalizedOut.evaluate(ImmutableSet.of(new Assignment(x, 1), new Assignment(z, 2)))).isEqualTo(2d);
  }
}