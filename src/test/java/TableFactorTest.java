import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class TableFactorTest {

  private DiscreteVariable x = new DiscreteVariable(2, "X");
  private DiscreteVariable y = new DiscreteVariable(2, "Y");
  private DiscreteVariable z = new DiscreteVariable(3, "Z");
  private DiscreteVariable a = new DiscreteVariable(4, "A"); //lazy 1-index
  private DiscreteVariable b = new DiscreteVariable(3, "B"); //lazy 1-index
  private DiscreteVariable c = new DiscreteVariable(3, "C"); //lazy 1-index
  private TableFactor xor;
  private TableFactor xPlusTenY;
  private TableFactor multiplied;
  private TableFactor zMinusX;
  private TableFactor figureFourThreeA;
  private TableFactor figureFourThreeB;

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

    Map<Set<Assignment>, Double> zMinusX = new HashMap<>();
    for (Assignment xAssignment : xAssignments) {
      for (Assignment zAssignment : zAssignments) {
        zMinusX.put(ImmutableSet.of(xAssignment, zAssignment),
            (double) (zAssignment.getValue() - xAssignment.getValue()));
      }
    }
    this.zMinusX = new TableFactor(zMinusX);

    figureFourThreeA = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
          .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 1)), 0.5)
          .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 2)), 0.8)
          .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 1)), 0.1)
          .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 2)), 0.0)
          .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 1)), 0.3)
          .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 2)), 0.9)
        .build()
    );

    figureFourThreeB = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(b, 1), new Assignment(c, 1)), 0.5)
            .put(ImmutableSet.of(new Assignment(b, 1), new Assignment(c, 2)), 0.7)
            .put(ImmutableSet.of(new Assignment(b, 2), new Assignment(c, 1)), 0.1)
            .put(ImmutableSet.of(new Assignment(b, 2), new Assignment(c, 2)), 0.2)
            .build()
    );
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
    assertThat(xMarginalXor.table).hasSize(2);
    assertThat(xMarginalXor.scope).isEqualTo(ImmutableSet.of(x));
    assertThat(xMarginalXor.table.values().toArray()).isEqualTo(new double[] { 1.0, 1.0});

    final TableFactor xMarginalPlus = xPlusTenY.marginalizeOut(y);
    assertThat(xMarginalPlus.table).hasSize(2);
    assertThat(xMarginalPlus.scope).isEqualTo(ImmutableSet.of(x));
    assertThat(xMarginalPlus.table.values().toArray()).isEqualTo(new double[] { 10.0, 12.0});

    final TableFactor yMarginalizedOut = multiplied.marginalizeOut(y);
    assertThat(yMarginalizedOut.table).hasSize(6);
    assertThat(yMarginalizedOut.scope).isEqualTo(ImmutableSet.of(x, z));
    assertThat(yMarginalizedOut.table.values().toArray()).isEqualTo(new double[] { 0d*0d, 0d*1d, 0d*2d, 1d*0d, 1d*1d, 1d*2d});
    assertThat(yMarginalizedOut.evaluate(ImmutableSet.of(new Assignment(x, 1), new Assignment(z, 2)))).isEqualTo(2d);
  }

  @Test
  public void product() {
    final TableFactor product = xPlusTenY.product(zMinusX);
    assertThat(product).isEqualTo(zMinusX.product(xPlusTenY)); //commutative
    assertThat(product.scope).isEqualTo(Sets.union(xPlusTenY.scope, zMinusX.scope));
    assertThat(product.table).hasSize(12);
    assertThat(product.table.values())
        .containsExactlyInAnyOrder(
              //       X Y Z  X+10Y Z-X
              0d, //   0 0 0   0    0
              0d, //   0 0 1   0    1
              0d, //   0 0 2   0    2
              0d, //   0 1 0   10   0
              10d, //  0 1 1   10   1
              20d, //  0 1 2   10   2
              -1d, //  1 0 0   1   -1
              0d, //   1 0 1   1    0
              1d, //   1 0 2   1    1
              -11d, // 1 1 0   11  -1
              0d, //   1 1 1   11   0
              11d //   1 1 2   11   1
            );

    // spot checks
    assertThat(product.evaluate(ImmutableSet.of(new Assignment(x, 0), new Assignment(y, 0), new Assignment(z, 0)))).isEqualTo(0d);
    assertThat(product.evaluate(ImmutableSet.of(new Assignment(x, 0), new Assignment(y, 0), new Assignment(z, 1)))).isEqualTo(0d);
    assertThat(product.evaluate(ImmutableSet.of(new Assignment(x, 1), new Assignment(y, 0), new Assignment(z, 0)))).isEqualTo(-1d);
    assertThat(product.evaluate(ImmutableSet.of(new Assignment(x, 1), new Assignment(y, 1), new Assignment(z, 0)))).isEqualTo(-11d);

    TableFactor figureFourThreeProduct = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 1), new Assignment(c, 1)), 0.25)
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 1), new Assignment(c, 2)), 0.35)
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 2), new Assignment(c, 1)), 0.08)
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 2), new Assignment(c, 2)), 0.16)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 1), new Assignment(c, 1)), 0.05)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 1), new Assignment(c, 2)), 0.07)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 2), new Assignment(c, 1)), 0.0)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 2), new Assignment(c, 2)), 0.0)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 1), new Assignment(c, 1)), 0.15)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 1), new Assignment(c, 2)), 0.21)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 2), new Assignment(c, 1)), 0.09)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 2), new Assignment(c, 2)), 0.18)
            .build()
    );

    // Koller page 107
    assertThat(figureFourThreeA.product(figureFourThreeB).equals(figureFourThreeProduct, 1e-6)).isTrue();
    assertThat(figureFourThreeB.product(figureFourThreeA).equals(figureFourThreeProduct, 1e-6)).isTrue();
  }
}