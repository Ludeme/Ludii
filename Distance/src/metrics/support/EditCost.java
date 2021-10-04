package metrics.support;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * 
 * @author markus
 */
public class EditCost
{

	int HIT_VALUE = 5;
	int MISS_VALUE = -5;
	int GAP_PENALTY = -1;

	/**
	 * 
	 * @param hitValue    should be positive
	 * @param missValue   should be negative
	 * @param gapPenality should be negative
	 */
	public EditCost(
			final int hitValue, final int missValue, final int gapPenality
	)
	{
		this.HIT_VALUE = hitValue;
		this.MISS_VALUE = missValue;
		this.GAP_PENALTY = gapPenality;
	}

	public EditCost()
	{
	}

	public int gapPenalty()
	{
		return GAP_PENALTY;
	}

	public int hit()
	{
		return HIT_VALUE;
	}

	public int miss()
	{

		return MISS_VALUE;
	}

	public static EditCost showUserSelectionDialog()
	{

		int hit = 5;
		int miss = -5;
		int gap = -1;

		final JTextField hitField = new JTextField();
		hitField.setText("" + hit);
		final JTextField missField = new JTextField();
		missField.setText("" + miss);
		final JTextField gapField = new JTextField();
		gapField.setText("" + gap);
		
		final Object[] message = {
				"hit value:", hitField,
				"miss value:", missField,
				"gap value:", gapField
		};

		final int option = JOptionPane.showConfirmDialog(null, message,
				"Edit cost settings", JOptionPane.PLAIN_MESSAGE);

		if (option == JOptionPane.OK_OPTION)
		{
			try
			{
				hit = Integer
						.parseInt(hitField.getText());
			} catch (final Exception e2)
			{
				System.out.println(
						"Not a number, using hit = 5 instead");
			}
			try
			{
				miss = Integer
						.parseInt(missField.getText());
			} catch (final Exception e2)
			{
				System.out.println(
						"Not a number, using miss = -3 instead");
			}
			try
			{
				gap = Integer
						.parseInt(gapField.getText());
			} catch (final Exception e2)
			{
				System.out.println(
						"Not a number, using gap = -1 instead");
			}

		}
		return new EditCost(hit, miss, gap);

	}

	public int maxValue()
	{
		int first = Math.max(Math.abs(HIT_VALUE), Math.abs(GAP_PENALTY));
		int second = Math.max(first, Math.abs(this.MISS_VALUE));
		return second;
		
	}
}
