package gameDistance.metrics;

import game.Game;
import gameDistance.datasets.Dataset;
import java.util.Map;

/**
 * Interface for distance metric classes.
 *
 * @author Matthew.Stephenson
 */
public interface DistanceMetric {
  /**
   * @return Estimated distance between two games.
   */
  double distance(
    final Dataset dataset,
    final Map<String, Double> vocabulary,
    final Game gameA,
    final Game gameB
  );
}
