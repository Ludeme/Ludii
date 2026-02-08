package app.boardMaker.display.components.buttons;

import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * Creates a default button labelized "Create"
 */
public class CreateButton extends JButton
{
	public CreateButton(ActionListener al) {
		setText("Create");
		addActionListener(al);
	}
}
