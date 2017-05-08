import java.util.Set;

public class ClusterGraph {
  private final Set<Cluster> clusters;
  private final Set<ClusterEdge> edges;

  public ClusterGraph(Set<Cluster> clusters, Set<ClusterEdge> edges) {
    this.clusters = clusters;
    this.edges = edges;
  }
}
