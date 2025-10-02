package app.boardMaker.display.components.sliders;

import javax.swing.*;

public class RotationSlider extends JSlider {

    public RotationSlider() {
        super(-180,180,0);

        setMajorTickSpacing(90);
        setMinorTickSpacing(15);
        setPaintTicks(true);
        setPaintLabels(true);
    }
}
