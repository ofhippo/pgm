import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

public class DiscreteVariableTest {
  private DiscreteVariable x = new DiscreteVariable(2, "X");
  private DiscreteVariable y = new DiscreteVariable(3, "Y");

  @Test
  public void equals() throws Exception {
    assertThat(x).isNotEqualTo(y);
    assertThat(x).isNotEqualTo(new DiscreteVariable(3, "X"));
    assertThat(x).isNotEqualTo(new DiscreteVariable(2, "Y"));
    assertThat(x).isEqualTo(new DiscreteVariable(2, "X"));
  }

  @Test
  public void allAssignments() {
    assertThat(x.allAssignments()).isEqualTo(ImmutableSet.of(
        new Assignment(x, 0),
        new Assignment(x, 1)));

    assertThat(y.allAssignments()).isEqualTo(ImmutableSet.of(
        new Assignment(y, 0),
        new Assignment(y, 2),
        new Assignment(y, 1)));
  }

}