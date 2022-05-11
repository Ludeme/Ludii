package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;

import app.display.dialogs.visual_editor.model.grammar.Constructor;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LHeader extends JComponent {

    private LIngoingConnectionComponent ingoingConnectionComponent;
    private LudemeNodeComponent LNC;
    private JPanel connectionAndTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));

    public JLabel title;

    public LHeader(LudemeNodeComponent ludemeNodeComponent) {
        LNC = ludemeNodeComponent;

        setLayout(new BorderLayout());

        title = new JLabel(ludemeNodeComponent.getLudemeNode().getLudeme().getName());

        title.setFont(DesignPalette.LUDEME_TITLE_FONT);
        title.setForeground(DesignPalette.FONT_LUDEME_TITLE_COLOR);
        title.setSize(title.getPreferredSize());

        ingoingConnectionComponent = new LIngoingConnectionComponent(this, title.getHeight(), ((int)(title.getHeight()*0.4)), false);

        connectionAndTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionAndTitle.add(ingoingConnectionComponent);
        connectionAndTitle.add(Box.createHorizontalStrut(5));
        connectionAndTitle.add(title);
        connectionAndTitle.setOpaque(false);

        add(connectionAndTitle, BorderLayout.LINE_START);

        if(ludemeNodeComponent.getLudemeNode().getLudeme().getConstructors().size() > 1) {


            JComboBox<Constructor> constructorPicker = new JComboBox<>();
            for (Constructor c : ludemeNodeComponent.getLudemeNode().getLudeme().getConstructors()) {
                constructorPicker.addItem(c);
            }

            System.out.println(ludemeNodeComponent.getLudemeNode().getCurrentConstructor());
            constructorPicker.setSelectedItem(ludemeNodeComponent.getLudemeNode().getCurrentConstructor());

            constructorPicker.addActionListener(e -> {
                ludemeNodeComponent.changeConstructor((Constructor) constructorPicker.getSelectedItem());
                repaint();
            });
            //add(constructorPicker, BorderLayout.SOUTH);
        }


        //int width = title.getPreferredSize().width + ingoingConnectionComponent.getPreferredSize().width;
        //int height = title.getPreferredSize().height;

        // TODO: maybe do this somehwere else?
        setBorder(new EmptyBorder(DesignPalette.HEADER_PADDING_TOP,0,DesignPalette.HEADER_PADDING_BOTTOM,0)); // just space between this and input area and top of LNC

        setSize(getPreferredSize());

        setOpaque(false);

        revalidate();
        repaint();
        setVisible(true);
    }

    public void updatePosition(){
        ingoingConnectionComponent.updatePosition();
    }

    public LIngoingConnectionComponent getIngoingConnectionComponent() {
        return ingoingConnectionComponent;
    }

    public LudemeNodeComponent getLudemeNodeComponent() {
        return LNC;
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        title.setFont(DesignPalette.LUDEME_TITLE_FONT);
        title.setForeground(DesignPalette.FONT_LUDEME_TITLE_COLOR);
        title.setSize(title.getPreferredSize());

        setSize(getPreferredSize());
        setBorder(DesignPalette.HEADER_PADDING_BORDER);

        //ingoingConnectionComponent = new LIngoingConnectionComponent(this, title.getHeight(), ((int)(title.getHeight()*0.4)), false);
/*
        remove(connectionAndTitle);

        connectionAndTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionAndTitle.add(ingoingConnectionComponent);
        connectionAndTitle.add(Box.createHorizontalStrut(5));
        connectionAndTitle.add(title);
        connectionAndTitle.setOpaque(true);

        add(connectionAndTitle, BorderLayout.LINE_START);


*/

    }

}
