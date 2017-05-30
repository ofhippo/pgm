import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.assertj.core.data.Percentage;
import org.junit.Test;

public class GibbsSamplerUtilsTest {
  @Test
  public void runNoEvidence() {
    // P(l=1) = 0.502
    final Set<Assignment> initialAssignments = ImmutableSet.of(
        new Assignment(StudentExample.i, 1),
        new Assignment(StudentExample.d, 1),
        new Assignment(StudentExample.s, 1),
        new Assignment(StudentExample.l, 1),
        new Assignment(StudentExample.g, 1)
    );

    List<Set<Assignment>> samples = GibbsSamplerUtils.run(StudentExample.allFactors(), initialAssignments,
        StudentExample.allVariables(), 3000);

    final Map<Set<Assignment>, Integer> countsFromSamples = getCountsFromSamples(samples);
    int lOneSum = 0;
    for (Entry<Set<Assignment>, Integer> entry : countsFromSamples.entrySet()) {
      if (entry.getKey().contains(new Assignment(StudentExample.l, 1))) {
        lOneSum += entry.getValue();
      }
    }

    final Percentage acceptableDeviation = Percentage.withPercentage(1d);
    assertThat(lOneSum / (double) samples.size()).isCloseTo(0.502, acceptableDeviation);
  }

  @Test
  public void run() {
    // P(i=1|g=3) = 0.079
    // P(d=1|g=3) = 0.629
    final Set<Assignment> initialAssignment = ImmutableSet.of(
        new Assignment(StudentExample.i, 0),
        new Assignment(StudentExample.d, 0),
        new Assignment(StudentExample.s, 0),
        new Assignment(StudentExample.l, 0),
        new Assignment(StudentExample.g, 3)
    );

    final List<Variable> unobservedVariables = ImmutableList.of(StudentExample.i, StudentExample.d, StudentExample.s, StudentExample.l);

    List<Set<Assignment>> samples = GibbsSamplerUtils.run(StudentExample.allFactors(), initialAssignment, unobservedVariables, (int) 1e5);

    final Map<Set<Assignment>, Integer> countsFromSamples = getCountsFromSamples(samples);
    int dOneSum = 0;
    int iOneSum = 0;
    for (Entry<Set<Assignment>, Integer> entry : countsFromSamples.entrySet()) {
      if (entry.getKey().contains(new Assignment(StudentExample.d, 1))) {
        dOneSum += entry.getValue();
      }
      if (entry.getKey().contains(new Assignment(StudentExample.i, 1))) {
        iOneSum += entry.getValue();
      }
    }

    final Percentage acceptableDeviation = Percentage.withPercentage(1d);
    assertThat(dOneSum / (double) samples.size()).isCloseTo(0.629, acceptableDeviation);
    assertThat(iOneSum / (double) samples.size()).isCloseTo(0.079, acceptableDeviation);
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