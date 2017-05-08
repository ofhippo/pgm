import static org.assertj.core.api.Java6Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

public class VariableEliminationUtilsTest {

  private Variable x = new Variable(2, "X");
  private Variable y = new Variable(2, "Y");
  private Variable z = new Variable(3, "Z");
  private Variable a = new Variable(4, "A"); //lazy 1-index
  private Variable b = new Variable(3, "B"); //lazy 1-index
  private Variable c = new Variable(3, "C"); //lazy 1-index
  private Variable d = new Variable(2, "D");
  private Variable i = new Variable(2, "I");
  private Variable g = new Variable(4, "G"); //lazy 1-index
  private Variable s = new Variable(2, "S");
  private Variable l = new Variable(2, "L");

  private TableFactor xor;
  private TableFactor xPlusTenY;
  private TableFactor zMinusX;
  private TableFactor figureFourThreeA;
  private TableFactor figureFourThreeB;
  private TableFactor difficulty;
  private TableFactor intelligence;
  private TableFactor grade;
  private TableFactor sat;
  private TableFactor letter;

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

    // Koller page 53
    difficulty = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(d, 0)), 0.6)
            .put(ImmutableSet.of(new Assignment(d, 1)), 0.4)
            .build()
    );

    intelligence = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(i, 0)), 0.7)
            .put(ImmutableSet.of(new Assignment(i, 1)), 0.3)
            .build()
    );

    grade = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(d, 0), new Assignment(g, 1)), 0.3)
            .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(d, 0), new Assignment(g, 2)), 0.4)
            .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(d, 0), new Assignment(g, 3)), 0.3)
            .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(d, 1), new Assignment(g, 1)), 0.05)
            .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(d, 1), new Assignment(g, 2)), 0.25)
            .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(d, 1), new Assignment(g, 3)), 0.7)
            .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(d, 0), new Assignment(g, 1)), 0.9)
            .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(d, 0), new Assignment(g, 2)), 0.08)
            .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(d, 0), new Assignment(g, 3)), 0.02)
            .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(d, 1), new Assignment(g, 1)), 0.5)
            .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(d, 1), new Assignment(g, 2)), 0.3)
            .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(d, 1), new Assignment(g, 3)), 0.2)
            .build()
    );

    letter = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(l, 0), new Assignment(g, 1)), 0.1)
            .put(ImmutableSet.of(new Assignment(l, 0), new Assignment(g, 2)), 0.4)
            .put(ImmutableSet.of(new Assignment(l, 0), new Assignment(g, 3)), 0.99)
            .put(ImmutableSet.of(new Assignment(l, 1), new Assignment(g, 1)), 0.9)
            .put(ImmutableSet.of(new Assignment(l, 1), new Assignment(g, 2)), 0.6)
            .put(ImmutableSet.of(new Assignment(l, 1), new Assignment(g, 3)), 0.01)
            .build()
    );

    sat = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(s, 0)), 0.95)
            .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(s, 1)), 0.05)
            .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(s, 0)), 0.2)
            .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(s, 1)), 0.8)
            .build()
    );
  }

  @Test
  public void sumProductVariableElimination() throws Exception {
    final ImmutableSet<TableFactor> factors = ImmutableSet
        .of(xor, figureFourThreeA, figureFourThreeB, xPlusTenY, zMinusX);

    assertVariableEliminationScope(factors, ImmutableList.of(), ImmutableSet.of(a, b, c, x, y, z));
    assertVariableEliminationScope(factors, ImmutableList.of(z), ImmutableSet.of(a, b, c, x, y));
    assertVariableEliminationScope(factors, ImmutableList.of(z, x), ImmutableSet.of(a, b, c, y));
    assertVariableEliminationScope(factors, ImmutableList.of(b, z, x), ImmutableSet.of(a, c, y));
    assertVariableEliminationScope(factors, ImmutableList.of(b, x, z), ImmutableSet.of(a, c, y));
    assertVariableEliminationScope(factors, ImmutableList.of(x, z, b), ImmutableSet.of(a, c, y));
    assertVariableEliminationScope(factors, ImmutableList.of(x, b, z), ImmutableSet.of(a, c, y));
    assertVariableEliminationScope(factors, ImmutableList.of(z, b, x), ImmutableSet.of(a, c, y));
    assertVariableEliminationScope(factors, ImmutableList.of(z, x, b), ImmutableSet.of(a, c, y));
    assertVariableEliminationScope(factors, ImmutableList.of(b, a, z, x), ImmutableSet.of(c, y));
    assertVariableEliminationScope(factors, ImmutableList.of(b, a, y, z, x), ImmutableSet.of(c));
    assertVariableEliminationScope(factors, ImmutableList.of(b, a, y, z, x, c), ImmutableSet.of());
  }

  @Test
  public void studentExample() {
    Set<TableFactor> studentFactors = ImmutableSet.of(difficulty, grade, intelligence, sat, letter);
    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        studentFactors,
        ImmutableList.of()).evaluate(ImmutableSet.of(
        new Assignment(i, 1),
        new Assignment(d, 0),
        new Assignment(g, 2),
        new Assignment(s, 1),
        new Assignment(l, 0)
    ))).isCloseTo(0.004608, Offset.offset(1e-6)); // Koller page 54

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        studentFactors,
        ImmutableList.of(i, d, g, s)).evaluate(ImmutableSet.of(
        new Assignment(l, 1)
    ))).isCloseTo(0.502, Offset.offset(1e-3)); // Koller page 54

    assertThat(VariableEliminationUtils.conditionalProbVariableElimination(
        studentFactors,
        ImmutableSet.of(new Assignment(i, 0)),
        ImmutableList.of(d, g, s)).renormalize().evaluate(ImmutableSet.of(
        new Assignment(l, 1)
    ))).isCloseTo(0.389, Offset.offset(1e-3)); // Koller page 54, after we learn i=0

    assertThat(VariableEliminationUtils.conditionalProbVariableElimination(
        studentFactors,
        ImmutableSet.of(new Assignment(i, 0), new Assignment(d, 0)),
        ImmutableList.of(g, s)).renormalize().evaluate(ImmutableSet.of(
        new Assignment(l, 1)
    ))).isCloseTo(0.513, Offset.offset(1e-3)); // Koller page 54, after we learn i=0, d=0

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        studentFactors,
        ImmutableList.of(d, g, s, l)).renormalize().evaluate(ImmutableSet.of(
        new Assignment(i, 1)
    ))).isEqualTo(0.3);

    assertThat(VariableEliminationUtils.conditionalProbVariableElimination(
        studentFactors,
        ImmutableSet.of(new Assignment(g, 3)),
        ImmutableList.of(d, s, l)).renormalize().evaluate(ImmutableSet.of(
        new Assignment(i, 1)
    ))).isCloseTo(0.079, Offset.offset(1e-3));

    assertThat(VariableEliminationUtils.conditionalProbVariableElimination(
        studentFactors,
        ImmutableSet.of(new Assignment(g, 3)),
        ImmutableList.of(i, s, l)).renormalize().evaluate(ImmutableSet.of(
        new Assignment(d, 1)
    ))).isCloseTo(0.629, Offset.offset(1e-3));
  }

  private IterableAssert<Variable> assertVariableEliminationScope(ImmutableSet<TableFactor> factors,
      ImmutableList<Variable> variablesToEliminate,
      ImmutableSet<Variable> expectedScope) {
    return assertThat(VariableEliminationUtils.sumProductVariableElimination(
        factors,
        variablesToEliminate).scope).isEqualTo(expectedScope);
  }

}