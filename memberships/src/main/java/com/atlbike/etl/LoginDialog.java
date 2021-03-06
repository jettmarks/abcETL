package com.atlbike.etl;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.atlbike.etl.util.ETLProperties;

public class LoginDialog {
	public LoginDialog() {
		String[] optionNames = { "Login", "Cancel" };
		JPanel userPanel = new JPanel();
		userPanel.setPreferredSize(new Dimension(280, 50));
		userPanel.setLayout(new GridLayout(2, 2));
		JLabel userEmailLbl = new JLabel("Email Address:");
		JLabel passwordLbl = new JLabel("Password:");
		JTextField userEmail = new JTextField();
		JPasswordField passwordFld = new JPasswordField();
		userPanel.add(userEmailLbl);
		userPanel.add(userEmail);
		userPanel.add(passwordLbl);
		userPanel.add(passwordFld);
		userEmail.requestFocusInWindow();
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);

		JOptionPane.showOptionDialog(null, userPanel,
				"Enter NationBuilder Credentials",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
				null, optionNames, null);

		ETLProperties etlProps = ETLProperties.getInstance();
		etlProps.setLoginEmail(userEmail.getText());
		etlProps.setLoginPwd(passwordFld.getText());

		try {
			etlProps.store();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
