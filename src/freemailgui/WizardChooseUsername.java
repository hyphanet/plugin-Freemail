package freemailgui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Box;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.util.ResourceBundle;

public class WizardChooseUsername extends JPanel {
	public static final long serialVersionUID = -1;
	private JTextField usernametext;
	private JTextField passwordtext;
	private JTextField passwordconfirmtext;

	public WizardChooseUsername(ResourceBundle bundle) {
		super();
		
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		this.setLayout(gb);
		
		JLabel infolabel = new JLabel(bundle.getString("choose_a_username_and_password"), SwingConstants.CENTER);
		infolabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gb.setConstraints(infolabel, c);
		this.add(infolabel);
		
		Component vspacer = Box.createRigidArea(new Dimension(15, 15));
		gb.setConstraints(vspacer, c);
		this.add(vspacer);
		
		Component lspacer = Box.createRigidArea(new Dimension(15, 15));
		c.gridwidth = 1;
		gb.setConstraints(lspacer, c);
		this.add(lspacer);
		
		JLabel usernamelbl = new JLabel(bundle.getString("username")+": ", SwingConstants.RIGHT);
		usernamelbl.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.anchor = GridBagConstraints.EAST; 
		gb.setConstraints(usernamelbl, c);
		this.add(usernamelbl);
		
		this.usernametext = new JTextField();
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = GridBagConstraints.RELATIVE;
		gb.setConstraints(this.usernametext, c);
		this.add(this.usernametext);
		
		Component rspacer = Box.createRigidArea(new Dimension(15, 15));
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		gb.setConstraints(rspacer, c);
		this.add(rspacer);
		 
		JLabel passwordlbl = new JLabel(bundle.getString("password")+": ", SwingConstants.RIGHT);
		passwordlbl.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		c.gridwidth = 1;
		c.gridx = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		gb.setConstraints(passwordlbl, c);
		this.add(passwordlbl);
		
		this.passwordtext = new JPasswordField();
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = GridBagConstraints.RELATIVE;
		gb.setConstraints(this.passwordtext, c);
		this.add(this.passwordtext);
		
		Component rspacer1 = Box.createRigidArea(new Dimension(15, 15));
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		gb.setConstraints(rspacer1, c);
		this.add(rspacer1);
		
		JLabel passwordconfirmlbl = new JLabel(bundle.getString("confirm_password")+": ", SwingConstants.RIGHT);
		passwordconfirmlbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		c.gridwidth = 1;
		c.gridx = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		gb.setConstraints(passwordconfirmlbl, c);
		this.add(passwordconfirmlbl);
		
		this.passwordconfirmtext = new JPasswordField();
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = GridBagConstraints.RELATIVE;
		gb.setConstraints(this.passwordconfirmtext, c);
		this.add(this.passwordconfirmtext);
	}
}
