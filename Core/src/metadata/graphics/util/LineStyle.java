package metadata.graphics.util;

import game.Game;
import java.util.BitSet;
import metadata.graphics.GraphicsItem;

/**
 * Defines line styles for drawing board elements, e.g. edges for graph games.
 *
 * @author matthew.stephenson and cambolbro
 */
public enum LineStyle implements GraphicsItem {
  /** Thin line. */
  Thin,

  /** Thick line. */
  Thick,

  /** Thin dotted line. */
  ThinDotted,

  /** Thick dotted line. */
  ThickDotted,

  /** Thin dashed line. */
  ThinDashed,

  /** Thick dashed line. */
  ThickDashed,

  /** Line not drawn. */
  Hidden;

  @Override
  public BitSet concepts(final Game game) {
    final BitSet concepts = new BitSet();
    return concepts;
  }

  @Override
  public long gameFlags(final Game game) {
    final long gameFlags = 0l;
    return gameFlags;
  }

  @Override
  public boolean needRedraw() {
    return false;
  }
}
