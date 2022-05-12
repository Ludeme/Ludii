package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;


import app.display.dialogs.visual_editor.view.DesignPalette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class LInputButton extends JButton {

    private final Color ACTIVE_COLOR = DesignPalette.FONT_LUDEME_INPUTS_COLOR;
    private final Color HOVER_COLOR = new Color(127,191,255);

    private final ImageIcon ACTIVE_ICON;
    private final ImageIcon HOVER_ICON;

    public LInputButton(ImageIcon activeIcon, ImageIcon hoverIcon){
        super(activeIcon);
        this.ACTIVE_ICON = activeIcon;
        this.HOVER_ICON = hoverIcon;

        setFont(new Font("Roboto Bold", 0, 12));

        // make transparent
        setBorder(BorderFactory.createEmptyBorder());
        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);

        addMouseListener(hoverMouseListener);

    }

    public void setActive(){
        setForeground(ACTIVE_COLOR);
        setIcon(ACTIVE_ICON);
        repaint();
    }


    public void setHover(){
        setForeground(HOVER_COLOR);
        setIcon(HOVER_ICON);
        repaint();
    }

    MouseListener hoverMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            setHover();
        }
        public void mouseExited(MouseEvent e) {
            setActive();
        }
    };

}
