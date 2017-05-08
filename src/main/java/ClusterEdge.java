import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Set;

public class ClusterEdge {
  private final Cluster clusterA;
  private final Cluster clusterB;
  private final Set<Factor> sepset;

  public ClusterEdge(Cluster clusterA, Cluster clusterB, Set<Factor> sepset) {
    Preconditions.checkArgument(
        Sets.intersection(clusterA.getFactors(),
            clusterB.getFactors()).containsAll(sepset));
    this.clusterA = clusterA;
    this.clusterB = clusterB;
    this.sepset = sepset;
  }
}
