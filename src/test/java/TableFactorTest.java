import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.data.Percentage;
import org.junit.Before;
import org.junit.Test;

public class TableFactorTest {

  public static final double EPSILON = 1e-6;
  private Variable x = new Variable(2, "X");
  private Variable y = new Variable(2, "Y");
  private Variable z = new Variable(3, "Z");
  private Variable a = new Variable(4, "A"); //lazy 1-index
  private Variable b = new Variable(3, "B"); //lazy 1-index
  private Variable c = new Variable(3, "C"); //lazy 1-index
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
    assertThat(figureFourThreeA.product(figureFourThreeB).equals(figureFourThreeProduct, EPSILON)).isTrue();
    assertThat(figureFourThreeB.product(figureFourThreeA).equals(figureFourThreeProduct, EPSILON)).isTrue();
    assertThat(TableFactor.product(
        ImmutableSet.of(figureFourThreeA, figureFourThreeB)).equals(figureFourThreeProduct, EPSILON)).isTrue();

  }

  @Test
  public void reduceSingle() {
    TableFactor expected = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 1), new Assignment(c, 1)), 0.25)
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 2), new Assignment(c, 1)), 0.08)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 1), new Assignment(c, 1)), 0.05)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 2), new Assignment(c, 1)), 0.0)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 1), new Assignment(c, 1)), 0.15)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 2), new Assignment(c, 1)), 0.09)
            .build()
    );

    final TableFactor reduced = figureFourThreeA.product(figureFourThreeB)
        .reduce(new Assignment(c, 1));
    assertThat(reduced.equals(expected, EPSILON)).isTrue();
  }

  @Test
  public void reduceSet() {
    TableFactor expectedReducedByCOne = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 1), new Assignment(c, 1)), 0.25)
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 2), new Assignment(c, 1)), 0.08)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 1), new Assignment(c, 1)), 0.05)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 2), new Assignment(c, 1)), 0.0)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 1), new Assignment(c, 1)), 0.15)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 2), new Assignment(c, 1)), 0.09)
            .build()
    );

    TableFactor expectedReducedByCOneAndBTwo = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 2), new Assignment(c, 1)), 0.08)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 2), new Assignment(c, 1)), 0.0)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 2), new Assignment(c, 1)), 0.09)
            .build()
    );

    final TableFactor product = figureFourThreeA.product(figureFourThreeB);
    assertThat(product.reduce(ImmutableSet.of())).isEqualTo(product);
    assertThat(product
        .reduce(ImmutableSet.of(new Assignment(c, 1))).equals(expectedReducedByCOne, EPSILON)).isTrue();

    assertThat(product.reduce(ImmutableSet.of(new Assignment(c, 1), new Assignment(b, 2))).equals(expectedReducedByCOneAndBTwo, EPSILON)).isTrue();
  }

  @Test
  public void sample() {
    final double numSamples = 1e5;
    final Percentage acceptableDeviation = Percentage.withPercentage(1);

    TableFactor factor = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(a, 0)), 0.1)
            .put(ImmutableSet.of(new Assignment(a, 1)), 0.2)
            .put(ImmutableSet.of(new Assignment(a, 2)), 0.3)
            .put(ImmutableSet.of(new Assignment(a, 3)), 0.4)
            .build()
    );

    List<Set<Assignment>> samples = drawSamples(factor, numSamples);

    Map<Set<Assignment>, Integer> counts = getCountsFromSamples(samples);

    assertThat(counts.get(ImmutableSet.of(new Assignment(a, 0))) / numSamples).isCloseTo(0.1, acceptableDeviation);
    assertThat(counts.get(ImmutableSet.of(new Assignment(a, 1))) / numSamples).isCloseTo(0.2, acceptableDeviation);
    assertThat(counts.get(ImmutableSet.of(new Assignment(a, 2))) / numSamples).isCloseTo(0.3, acceptableDeviation);
    assertThat(counts.get(ImmutableSet.of(new Assignment(a, 3))) / numSamples).isCloseTo(0.4, acceptableDeviation);

    TableFactor factor2 = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(a, 0), new Assignment(c, 1)), 10d)
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(c, 2)), 20d)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(c, 1)), 30d)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(c, 2)), 40d)
            .build()
    );

    List<Set<Assignment>> samples2 = drawSamples(factor2, numSamples);
    Map<Set<Assignment>, Integer> counts2 = getCountsFromSamples(samples2);

    assertThat(counts2.get(ImmutableSet.of(new Assignment(a, 0), new Assignment(c, 1))) / numSamples).isCloseTo(0.1, acceptableDeviation);
    assertThat(counts2.get(ImmutableSet.of(new Assignment(a, 1), new Assignment(c, 2))) / numSamples).isCloseTo(0.2, acceptableDeviation);
    assertThat(counts2.get(ImmutableSet.of(new Assignment(a, 2), new Assignment(c, 1))) / numSamples).isCloseTo(0.3, acceptableDeviation);
    assertThat(counts2.get(ImmutableSet.of(new Assignment(a, 3), new Assignment(c, 2))) / numSamples).isCloseTo(0.4, acceptableDeviation);

    List<Set<Assignment>> samplesWithEvidences = drawSamplesWithEvidence(factor2, numSamples, ImmutableSet.of(new Assignment(c, 2)));
    Map<Set<Assignment>, Integer> countsWithEvidence = getCountsFromSamples(samplesWithEvidences);
    assertThat(countsWithEvidence.size()).isEqualTo(2);
    assertThat(countsWithEvidence.get(ImmutableSet.of(new Assignment(a, 1), new Assignment(c, 2))) / numSamples).isCloseTo(0.333, acceptableDeviation);
    assertThat(countsWithEvidence.get(ImmutableSet.of(new Assignment(a, 3), new Assignment(c, 2))) / numSamples).isCloseTo(0.666, acceptableDeviation);
  }


  private List<Set<Assignment>> drawSamplesWithEvidence(TableFactor factor, double numSamples, Set<Assignment> evidence) {
    List<Set<Assignment>> samples = new ArrayList<>();
    for (int i = 0; i < numSamples; i++) {
      samples.add(factor.sample(evidence));
    }
    return samples;
  }

  private List<Set<Assignment>> drawSamples(TableFactor factor, double numSamples) {
    List<Set<Assignment>> samples = new ArrayList<>();
    for (int i = 0; i < numSamples; i++) {
      samples.add(factor.sample());
    }
    return samples;
  }

  private Map<Set<Assignment>, Integer> getCountsFromSamples(List<Set<Assignment>> samples) {
    Map<Set<Assignment>, Integer> counts = new HashMap<>();
    for (Set<Assignment> sample : samples) {
      final Integer countForSample = counts.get(sample);
      if (countForSample == null) {
        counts.put(sample, 1);
      } else {
        counts.put(sample, countForSample + 1);
      }
    }
    return counts;
  }
}