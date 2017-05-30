import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;

public class StudentExample {
  static Variable d = new Variable(2, "D");
  static Variable i = new Variable(2, "I");
  static Variable g = new Variable(4, "G"); //lazy 1-index
  static Variable s = new Variable(2, "S");
  static Variable l = new Variable(2, "L");

  // Koller page 53
  static TableFactor difficulty = new TableFactor(
      ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(d, 0)), 0.6)
      .put(ImmutableSet.of(new Assignment(d, 1)), 0.4)
      .build()
    );

  static TableFactor intelligence = new TableFactor(
      ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(i, 0)), 0.7)
      .put(ImmutableSet.of(new Assignment(i, 1)), 0.3)
      .build()
    );

  static TableFactor grade = new TableFactor(
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

  static TableFactor letter = new TableFactor(
      ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(l, 0), new Assignment(g, 1)), 0.1)
      .put(ImmutableSet.of(new Assignment(l, 0), new Assignment(g, 2)), 0.4)
      .put(ImmutableSet.of(new Assignment(l, 0), new Assignment(g, 3)), 0.99)
      .put(ImmutableSet.of(new Assignment(l, 1), new Assignment(g, 1)), 0.9)
      .put(ImmutableSet.of(new Assignment(l, 1), new Assignment(g, 2)), 0.6)
      .put(ImmutableSet.of(new Assignment(l, 1), new Assignment(g, 3)), 0.01)
      .build()
    );

  static TableFactor sat = new TableFactor(
      ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(s, 0)), 0.95)
      .put(ImmutableSet.of(new Assignment(i, 0), new Assignment(s, 1)), 0.05)
      .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(s, 0)), 0.2)
      .put(ImmutableSet.of(new Assignment(i, 1), new Assignment(s, 1)), 0.8)
      .build()
    );

  static Set<TableFactor> allFactors() {
    return ImmutableSet.of(difficulty, grade, intelligence, sat, letter);
  }

  public static List<Variable> allVariables() {
    return ImmutableList.of(d, g, i, s, l);
  }
}
