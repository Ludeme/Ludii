package app.display.dialogs.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
  
/**
 * Class for creating a JComboBox with check boxes on the options.
 * @author Matthew.Stephenson
 *
 */
@SuppressWarnings({ "rawtypes" })
public class JComboCheckBox extends JComboBox
{
	private static final long serialVersionUID = 1L;

public JComboCheckBox() {
      init();
   }
    
   @SuppressWarnings("unchecked")
public JComboCheckBox(final JCheckBox[] items) {
      super(items);
      init();
   }
    
   @SuppressWarnings("unchecked")
public JComboCheckBox(final Vector items) {
      super(items);
      init();
   }
    
   @SuppressWarnings("unchecked")
public JComboCheckBox(final ComboBoxModel aModel) {
      super(aModel);
      init();
   }
    
   @SuppressWarnings("unchecked")
private void init() {
      setRenderer(new ComboBoxRenderer());
      addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(final ActionEvent ae) {
            itemSelected();
         }
      });
   }
  
	void itemSelected()
	{
      if (getSelectedItem() instanceof JCheckBox) {
         final JCheckBox jcb = (JCheckBox)getSelectedItem();
         jcb.setSelected(!jcb.isSelected());
      }
   }
  
   class ComboBoxRenderer implements ListCellRenderer {
      private JLabel label;
       
      public ComboBoxRenderer() {
         setOpaque(true);
      }
       
      @Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
                                                    final boolean isSelected, final boolean cellHasFocus) {
         if (value instanceof Component) {
            final Component c = (Component)value;
            if (isSelected) {
               c.setBackground(list.getSelectionBackground());
               c.setForeground(list.getSelectionForeground());
            } else {
               c.setBackground(list.getBackground());
               c.setForeground(list.getForeground());
            }
              
            return c;
         } else {
            if (label ==null) {
               label = new JLabel(value.toString());
            }
            else {
               label.setText(value.toString());
            }
                
            return label;
         }
      }
   }
}