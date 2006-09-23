package freemailgui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;

import java.util.ResourceBundle;

public class WizardAskGenKeys extends JPanel {
	private final JTextField fcphosttext;
	private final JTextField fcpporttext;

	public WizardAskGenKeys(ResourceBundle bundle) {
		super();
		
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(gb);
		
		// put the label in HTML mode to make in word wrap
		// this doesn't work in GIJ/GCJ, but then neither does
		// the previous page since classpath's layout is broken.
		JLabel explain = new JLabel("<html><div style=\"text-align: center; \">"+bundle.getString("will_generate_keys")+"</div></html>");
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		gb.setConstraints(explain, c);
		this.add(explain);
		
		Component vspacer = Box.createRigidArea(new Dimension(15, 15));
		gb.setConstraints(vspacer, c);
		this.add(vspacer);
		
		JLabel nodeprompt = new JLabel("<html><div style=\"text-align: center; \">"+bundle.getString("node_address_prompt")+"</div></html>", SwingConstants.CENTER);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gb.setConstraints(nodeprompt, c);
		this.add(nodeprompt);
		
		Component vspacer1 = Box.createRigidArea(new Dimension(15, 15));
		gb.setConstraints(vspacer1, c);
		this.add(vspacer1);
		
		Component lspacer = Box.createRigidArea(new Dimension(15, 15));
		c.gridwidth = 1;
		c.weightx = 0;
		gb.setConstraints(lspacer, c);
		this.add(lspacer);
		
		JLabel fcphostlbl = new JLabel(bundle.getString("fcp_host")+": ");
		c.anchor = GridBagConstraints.EAST;
		gb.setConstraints(fcphostlbl, c);
		this.add(fcphostlbl);
		
		this.fcphosttext = new JTextField("blurdyboop");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = 1;
		gb.setConstraints(this.fcphosttext, c);
		this.add(this.fcphosttext);
		
		Component rspacer = Box.createRigidArea(new Dimension(15, 15));
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		gb.setConstraints(rspacer, c);
		this.add(rspacer);
		
		
		JLabel fcpportlbl = new JLabel(bundle.getString("fcp_port")+": ");
		c.weightx = 0;
		c.gridx = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		gb.setConstraints(fcpportlbl, c);
		this.add(fcpportlbl);
		
		this.fcpporttext = new JTextField("numeric blurdyboop");
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridwidth = 1;
		gb.setConstraints(this.fcpporttext, c);
		this.add(this.fcpporttext);
		
		Component rspacer1 = Box.createRigidArea(new Dimension(15, 15));
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		gb.setConstraints(rspacer1, c);
		this.add(rspacer1);
	}
}
