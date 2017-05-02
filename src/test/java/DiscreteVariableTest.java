import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;

public class DiscreteVariableTest {
  private DiscreteVariable x = new DiscreteVariable(2, "X");

  @Test
  public void equals() throws Exception {
    assertThat(x).isNotEqualTo(new DiscreteVariable(3, "X"));
    assertThat(x).isNotEqualTo(new DiscreteVariable(2, "Y"));
    assertThat(x).isEqualTo(new DiscreteVariable(2, "X"));
  }

}