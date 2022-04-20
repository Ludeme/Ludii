package app.display.dialogs.visual_editor.view.components.ludemenode.block;


import app.display.dialogs.visual_editor.model.grammar.Constructor;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenode.interfaces.ILudemeNodeTitle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class LudemeBlockHeaderComponent extends JPanel implements ILudemeNodeTitle {

    private LudemeConnectionComponent ingoingConnectionComponent;

    public LudemeBlockHeaderComponent(LudemeBlock ludemeBlock){
        setPreferredSize(new Dimension(ludemeBlock.getWidth(), ludemeBlock.HEIGHT_HEADER_COMPONENT));
        setSize(getPreferredSize());
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(DesignPalette.BACKGROUND_LUDEME_TITLE);
        setOpaque(false); // TODO: ?

        int padding_left_center = 5;

        JLabel ludemeNameLabel = new JLabel(ludemeBlock.getLudeme().getName());
        ludemeNameLabel.setPreferredSize(new Dimension(ludemeBlock.WIDTH_CENTER - padding_left_center, getPreferredSize().height));
        ludemeNameLabel.setSize(getPreferredSize());
        ludemeNameLabel.setFont(DesignPalette.LUDEME_TITLE_FONT);
        ludemeNameLabel.setForeground(DesignPalette.FONT_LUDEME_TITLE_COLOR);

        // LEFT SIDE: Ingoing Connection
        // TODO: Center vertically and horizontally
        ingoingConnectionComponent = new LudemeConnectionComponent(ludemeBlock,null, ludemeBlock.WIDTH_SIDE,ludemeBlock.HEIGHT_HEADER_COMPONENT, ludemeBlock.HEIGHT_HEADER_COMPONENT/3,false);
        add(ingoingConnectionComponent);
        //add(new PlaceholderComponent(ludemeBlock.WIDTH_SIDE, (int)ludemeNameLabel.getPreferredSize().getHeight(), Color.RED));
        // PADDING BETWEEN LEFT & CENTER
        add(Box.createRigidArea(new Dimension(padding_left_center,1)));
        // CENTER: Ludeme Name
        add(ludemeNameLabel);
        // RIGHT SIDE: Empty
        JComponent emptyComponent = (JComponent) Box.createRigidArea(new Dimension(ludemeBlock.WIDTH_SIDE,5));
        add(emptyComponent);


        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                ArrayList<Constructor> constructors = (ArrayList<Constructor>) ludemeBlock.getLudeme().getConstructors();
                int currentIndex = constructors.indexOf(ludemeBlock.getCurrentConstructor());
                if(currentIndex == constructors.size()-1) ludemeBlock.setCurrentConstructor(constructors.get(0));
                else ludemeBlock.setCurrentConstructor(constructors.get(currentIndex+1));
            }
        });

        setVisible(true);
        revalidate();
        repaint();
    }

    public LudemeConnectionComponent getIngoingConnectionComponent(){
        return ingoingConnectionComponent;
    }

}
