package freemailgui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import javax.swing.Box;
import java.awt.Dimension;

import java.util.ResourceBundle;

public class WizardWelcome extends JPanel {
	public static final long serialVersionUID = -1;

	public WizardWelcome(ResourceBundle bundle) {
		super();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JLabel welcomelabel = new JLabel(bundle.getString("welcome"), SwingConstants.CENTER);
		welcomelabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		this.add(welcomelabel);
		
		this.add(Box.createRigidArea(new Dimension(0,15)));
		
		JLabel introlabel = new JLabel(bundle.getString("intro"));
		introlabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		this.add(introlabel);
	}
}
