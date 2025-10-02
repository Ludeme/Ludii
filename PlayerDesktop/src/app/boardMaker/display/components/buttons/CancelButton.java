package app.boardMaker.display.components.buttons;

import java.awt.event.ActionListener;

import javax.swing.JButton;

public class CancelButton extends JButton
{
	public CancelButton(ActionListener al) {
		setText("Cancel");
		addActionListener(al);
	}
}
