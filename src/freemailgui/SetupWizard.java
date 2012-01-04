/*
 * SetupWizard.java
 * This file is part of Freemail, copyright (C) 2006 Dave Baker
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package freemailgui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.ImageIcon;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.Box;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

public class SetupWizard implements ActionListener {
	private final JFrame frame;
	private final JPanel panel;
	private JPanel subpanel;
	private final JButton backbutton;
	private final JButton cancelbutton;
	public final JButton nextbutton;
	private ResourceBundle bundle;
	private final JLabel logolabel;
	private int currentstep;

	public static void main(String args[]) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SetupWizard().show();
			}
		});
	}
	
	public SetupWizard() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		
		try {
			this.bundle = ResourceBundle.getBundle("freemailgui.text.MessageBundle", Locale.getDefault());
		} catch (MissingResourceException mre) {
			this.bundle = ResourceBundle.getBundle("freemailgui.text.MessageBundle", new Locale("en", "GB")); 
		}
		
		this.frame = new JFrame(this.bundle.getString("wizard_title"));
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ImageIcon icon = new ImageIcon(getClass().getResource("images/pigeon_small.png"));
		this.frame.setIconImage(icon.getImage());
		
		this.panel = new JPanel();
		this.panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
		
		ImageIcon logo = new ImageIcon(getClass().getResource("images/logo_and_text.png"));
		this.logolabel = new JLabel();
		logolabel.setIcon(logo);
		logolabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		
		this.backbutton = new JButton("<< "+bundle.getString("back"));
		this.backbutton.addActionListener(this);
		
		this.cancelbutton = new JButton(bundle.getString("cancel"));
		this.cancelbutton.addActionListener(this);
		
		this.nextbutton = new JButton(bundle.getString("next")+" >>");
		this.nextbutton.addActionListener(this);
		
		this.currentstep = 0;
		this.makeGUI();
		this.frame.add(this.panel);
	}
	
	public void show() {
		// make the window a fixed size - it's a wizard
		this.frame.setSize(500, 400);
		// center
		this.frame.setLocationRelativeTo(null);
		this.frame.validate();
		this.frame.setVisible(true);
	}
	
	private void makeGUI() {
		this.panel.removeAll();
		
		this.panel.add(logolabel, 0);
		this.panel.add(Box.createVerticalGlue());
		
		switch (this.currentstep) {
			case 0:
				this.subpanel = new WizardWelcome(this.bundle);
				break;
			case 1:
				this.subpanel = new WizardChooseUsername(this.bundle);
				break;
			case 2:
				this.subpanel = new WizardAskGenKeys(this.bundle);
				break;
		}
		
		if (this.currentstep == 0) {
			this.backbutton.setEnabled(false);
		} else {
			this.backbutton.setEnabled(true);
		}
		
		this.panel.add(this.subpanel);
		
		this.panel.add(Box.createVerticalGlue());
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(this.backbutton);
		buttons.add(Box.createHorizontalGlue());
		buttons.add(this.cancelbutton);
		buttons.add(Box.createHorizontalGlue());
		buttons.add(this.nextbutton);
		
		this.panel.add(buttons);
		this.frame.validate();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.cancelbutton) {
			System.exit(0);
		} else if (e.getSource() == this.nextbutton) {
			this.currentstep++;
			this.makeGUI();
			this.panel.repaint();
		} else if (e.getSource() == this.backbutton) {
			this.currentstep--;
			this.makeGUI();
			this.panel.repaint();
		}
	}
}
