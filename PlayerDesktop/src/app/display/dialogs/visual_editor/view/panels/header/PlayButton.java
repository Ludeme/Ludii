package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import game.Game;

import javax.swing.*;
import java.awt.*;

public class PlayButton extends JButton
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2525022058118158573L;
	private boolean compilable = true;
    public PlayButton()
    {
        super("Play");
        setIcon(DesignPalette.COMPILABLE_ICON);
        setFont(new Font("Roboto Bold", Font.PLAIN, 12));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setFocusPainted(false);
        setOpaque(true);
        //setContentAreaFilled(false);
        setBorderPainted(false);
        setBackground(DesignPalette.COMPILABLE_COLOR());
        setForeground(DesignPalette.PLAY_BUTTON_FOREGROUND());
        // try to compile
     
        addActionListener(e -> {
            if (!compilable) 
            {
                Handler.markUncompilable();
                @SuppressWarnings("unchecked")
				java.util.List<String> errors = (java.util.List<String>) Handler.lastCompile[1];
                String errorMessage = "";
                if (errors.isEmpty())
                    {
                	errorMessage = "Could not create \"game\" ludeme from description.";
                    }
                else 
                {
                    errorMessage = errors.toString();
                    errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                }
                JOptionPane.showMessageDialog(null, errorMessage, "Couldn't compile", JOptionPane.ERROR_MESSAGE);
            } 
            else 
            {
                // try to compile
                Object[] output = Handler.compile();
                if (output[0] != null) 
                {
                    setCompilable();
                    setToolTipText(null);
                    Handler.play((Game) output[0]);
                } 
                else 
                {
                    @SuppressWarnings("unchecked")
					java.util.List<String> errors = (java.util.List<String>) output[1];
                    String errorMessage = "";
                    if (errors.isEmpty())
                        {
                    	errorMessage = "Could not create \"game\" ludeme from description.";
                        }
                    else 
                    {
                        errorMessage = errors.toString();
                        errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                    }
                    JOptionPane.showMessageDialog(this, errorMessage, "Couldn't compile", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    @Override
	public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if(compilable)
        {
            if (getBackground() != DesignPalette.COMPILABLE_COLOR())
            {
                setBackground(DesignPalette.COMPILABLE_COLOR());
                setForeground(DesignPalette.PLAY_BUTTON_FOREGROUND());
            }
        }
        else
        {
            if (getBackground() != DesignPalette.NOT_COMPILABLE_COLOR())
            {
                setBackground(DesignPalette.NOT_COMPILABLE_COLOR());
                setForeground(DesignPalette.PLAY_BUTTON_FOREGROUND());
            }
        }

    }

    public void setNotCompilable()
    {
        compilable = false;
        setIcon(DesignPalette.NOT_COMPILABLE_ICON);
        setBackground(DesignPalette.NOT_COMPILABLE_COLOR());
    }

    public void setCompilable()
    {
        compilable = true;
        setIcon(DesignPalette.COMPILABLE_ICON);
        setBackground(DesignPalette.COMPILABLE_COLOR());
    }

    public void updateCompilable(Object[] output)
    {
        if (output[0] != null)
        {
            setCompilable();
            setToolTipText(null);
        }
        else
        {
            @SuppressWarnings("unchecked")
			java.util.List<String> errors = (java.util.List<String>) output[1];
            String errorMessage = "";
            if (errors.isEmpty())
                errorMessage = "Could not create \"game\" ludeme from description.";
            else
            {
                errorMessage = errors.toString();
                errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
            }
            setNotCompilable();
            setToolTipText(errorMessage);
        }
    }

}
