package freemailgui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.Box;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

import java.util.ResourceBundle;

public class WizardChooseUsername extends JPanel {
	public static final long serialVersionUID = -1;
	private JTextField usernametext;
	private JTextField passwordtext;
	private JTextField passwordconfirmtext;

	public WizardChooseUsername(ResourceBundle bundle) {
		super();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.add(Box.createVerticalGlue());
		
		JLabel welcomelabel = new JLabel(bundle.getString("choose_a_username_and_password"), SwingConstants.CENTER);
		welcomelabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		this.add(welcomelabel);
		
		JPanel usernamebox = new JPanel();
		usernamebox.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		usernamebox.setLayout(gb);
		
		JLabel usernamelbl = new JLabel(bundle.getString("username")+": ", SwingConstants.RIGHT);
		usernamelbl.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		gb.setConstraints(usernamelbl, c);
		usernamebox.add(usernamelbl);
		
		this.usernametext = new JTextField();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		gb.setConstraints(this.usernametext, c);
		usernamebox.add(this.usernametext);
		 
		
		JLabel passwordlbl = new JLabel(bundle.getString("password")+": ", SwingConstants.RIGHT);
		passwordlbl.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		gb.setConstraints(passwordlbl, c);
		usernamebox.add(passwordlbl);
		
		this.passwordtext = new JPasswordField();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		gb.setConstraints(this.passwordtext, c);
		usernamebox.add(this.passwordtext);
		
		JLabel passwordconfirmlbl = new JLabel(bundle.getString("confirm_password")+": ", SwingConstants.RIGHT);
		passwordconfirmlbl.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		gb.setConstraints(passwordconfirmlbl, c);
		usernamebox.add(passwordconfirmlbl);
		
		this.passwordconfirmtext = new JPasswordField();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		gb.setConstraints(this.passwordconfirmtext, c);
		usernamebox.add(this.passwordconfirmtext);
		
		this.add(usernamebox);
		this.add(Box.createVerticalGlue());
	}
}
