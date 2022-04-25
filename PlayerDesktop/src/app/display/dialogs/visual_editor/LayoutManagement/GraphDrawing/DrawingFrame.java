package app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing;


import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;

import javax.swing.*;
import java.awt.event.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Main frame of DrawingApp
 * @author nic0gin
 */

public class DrawingFrame extends JFrame {

    private static int WIDTH = 800;
    private static int HEIGHT = 700;

    private GraphPanel graphPanel;
    private Timer updateTimer;

    public DrawingFrame()
    {
        setTitle("Graph drawing");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        updateTimer = new Timer(20, new LayoutUpdate());

        graphPanel = new GraphPanel(updateTimer);
        graphPanel.addMouseListener(new ClickListener());
        graphPanel.addMouseMotionListener(new DragDropListener());
        add(graphPanel);

        setVisible(true);
    }

    public static int getHEIGHT() {
        return HEIGHT;
    }

    public static int getWIDTH() {
        return WIDTH;
    }

    public static Vector2D getRandomScreenPos()
    {
        double x = ThreadLocalRandom.current().nextInt(-WIDTH/2, WIDTH/2);
        double y = ThreadLocalRandom.current().nextInt(-HEIGHT/2, HEIGHT/2);
        return new Vector2D(x, y);
    }

    private class LayoutUpdate implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {

            graphPanel.getLayoutManager().executeLayout();

            graphPanel.repaint();
            graphPanel.revalidate();
            //updateTimer.stop();
        }
    }

    public class ClickListener implements MouseListener
    {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                Vector2D click = new Vector2D(e.getX(), e.getY());
                graphPanel.getExpGraph().removeNode(new Vector2D(click.getDefTransX(), click.getDefTransY()));
                graphPanel.repaint();
                graphPanel.revalidate();
                System.out.println("hey");
            }
            //System.out.println("hey");
        }

        @Override
        public void mousePressed(MouseEvent e) {
            //System.out.println("hey");
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //System.out.println("hey");
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public class DragDropListener implements MouseMotionListener
    {

        boolean clicked = false;

        @Override
        public void mouseDragged(MouseEvent e) {
            Vector2D click = new Vector2D(e.getX(), e.getY());
            graphPanel.getExpGraph().updateNodePos(click.getDefTrans());
            graphPanel.repaint();
            graphPanel.revalidate();
            // TODO: increment temperature when dragged
            // graphPanel.getLayoutManager().incrementT(0.1);
            System.out.println("Dragged");
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }

}
