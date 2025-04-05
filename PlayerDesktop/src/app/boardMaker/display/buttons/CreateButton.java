package app.boardMaker.display.buttons;

import java.awt.event.ActionListener;

import javax.swing.JButton;

public class CreateButton extends JButton
{
	public CreateButton(ActionListener al) {
		setText("Create");
		addActionListener(al);
	}
}
