/*
 * WizardWelcome.java
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
