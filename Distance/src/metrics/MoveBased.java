package metrics;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import metrics.suffix_tree.Letteriser;

public interface MoveBased
{
	public class Inits
	{

		public Inits(final int playouts, final int maxMoves, final Letteriser selectedItem)
		{
			this.numPlayouts = playouts;
			this.numMaxMoves = maxMoves;
			this.letteriser = selectedItem;
		}
		public Letteriser letteriser;
		public int numPlayouts;
		public int numMaxMoves;

	}

	/**
	 * Many distance metrices can have multiple settings.
	 * This returns a suggestion. (if the metric does not 
	 * have multiple settings it just returns the instance).    
	 * 
	 * @return a userSelected instance of this distance metric
	 */
	public DistanceMetric showUserSelectionDialog();


	public static Inits showUserPlayoutAndMaxMovesSettings()
	{
		int playouts = 40;
		int maxMoves = 80;
		final JTextField playoutField = new JTextField();
		playoutField.setText("" + playouts);
		final JTextField maxMovesField = new JTextField();
		maxMovesField.setText("" + maxMoves);
		 final JComboBox<Letteriser> combo = new JComboBox<>(Letteriser.possibleLetteriser);
		final Object[] message = {
				"Num Playouts:", playoutField,
				"Max Moves:", maxMovesField,
				"Letteriser:", combo
		};
		JOptionPane.showConfirmDialog(null, message,
				"Edit cost settings", JOptionPane.DEFAULT_OPTION);

		
			try
			{
				playouts = Integer
						.parseInt(playoutField.getText());
			} catch (final Exception e2)
			{
				System.out.println(
						"Not a number, using playouts = 40 instead");
			}
			try
			{
				maxMoves = Integer
						.parseInt(maxMovesField.getText());
			} catch (final Exception e2)
			{
				System.out.println(
						"Not a number, using maxMoves = 80 instead");
			}
		
		return new Inits(playouts,maxMoves,(Letteriser)combo.getSelectedItem());
	}
	
	
}
