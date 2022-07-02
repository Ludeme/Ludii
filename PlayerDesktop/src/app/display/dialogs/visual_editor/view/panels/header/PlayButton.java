package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;
import game.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import static app.display.dialogs.visual_editor.view.DesignPalette.*;

public class PlayButton extends JButton
{

    private boolean compilable = true;
    public PlayButton()
    {
        super("Play");
        setIcon(COMPILABLE_ICON);
        setFont(new Font("Roboto Bold", Font.PLAIN, 12));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setFocusPainted(false);
        setOpaque(true);
        //setContentAreaFilled(false);
        setBorderPainted(false);
        setBackground(COMPILABLE_COLOR);
        setForeground(new Color(69,69,69));
        addActionListener(actionListener);
    }

    private final ActionListener actionListener = e ->
    {
        if(!compilable)
        {
            Handler.markUncompilable();
            java.util.List<String> errors = (java.util.List<String>) Handler.lastCompile[1];
            String errorMessage = "";
            if (errors.isEmpty())
                errorMessage = "Could not create \"game\" ludeme from description.";
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
                java.util.List<String> errors = (java.util.List<String>) output[1];
                String errorMessage = "";
                if (errors.isEmpty())
                    errorMessage = "Could not create \"game\" ludeme from description.";
                else
                {
                    errorMessage = errors.toString();
                    errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                }
                JOptionPane.showMessageDialog(this, errorMessage, "Couldn't compile", JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    public void setNotCompilable()
    {
        compilable = false;
        setIcon(NOT_COMPILABLE_ICON);
        setBackground(NOT_COMPILABLE_COLOR);
    }

    public void setCompilable()
    {
        compilable = true;
        setIcon(COMPILABLE_ICON);
        setBackground(COMPILABLE_COLOR);
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
