package chatroom;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ClientGUI extends JFrame {
	private static final long serialVersionUID = 1L;
	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;
	private String host, username;
	private int port;

	private JButton btnLogin;
	private JButton btnLogout;
	private JButton btnSend;
	private JTextArea textArea;
	private JTextArea textArea_1;
	private JTextField txtHost;
	private JTextField txtPort;
	private JTextField txtUserName;
	private JTextField txtMessage;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI frame = new ClientGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ClientGUI() {
		setTitle("Chat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_contentPane = new GridBagLayout();
		contentPane.setLayout(gbl_contentPane);
		setContentPane(contentPane);

		JPanel addressPane = new JPanel();
		addressPane.setBorder(new TitledBorder(null, "Contection Setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_addressPane = new GridBagConstraints();
		gbc_addressPane.gridwidth = 2;
		gbc_addressPane.insets = new Insets(0, 0, 5, 5);
		gbc_addressPane.gridx = 0;
		gbc_addressPane.gridy = 0;
		contentPane.add(addressPane, gbc_addressPane);

		JPanel hostPane = new JPanel();
		hostPane.setBorder(new TitledBorder(null, "Host", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		addressPane.add(hostPane);

		txtHost = new JTextField("localhost", 15);
		hostPane.add(txtHost);

		JPanel portPane = new JPanel();
		portPane.setBorder(new TitledBorder(null, "Port", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		addressPane.add(portPane);

		txtPort = new JTextField("9999", 5);
		portPane.add(txtPort);

		JPanel userNamePane = new JPanel();
		userNamePane.setBorder(new TitledBorder(null, "username",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		addressPane.add(userNamePane);

		txtUserName = new JTextField("Anonymous", 12);
		userNamePane.add(txtUserName);

		btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					host = txtHost.getText();
					port = Integer.parseInt(txtPort.getText());
					username = txtUserName.getText().trim().equals("") ? "Anonymous"
							: txtUserName.getText().trim();
					sock = new Socket(host, port);
					in = new BufferedReader(new InputStreamReader(sock
							.getInputStream()));
					out = new PrintWriter(sock.getOutputStream(), true);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(getRootPane(),
							"Port must be number.", "error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} catch (IOException e) {
					JOptionPane.showMessageDialog(getRootPane(),
							"Connect failed.");
					return;
				}
				new ListenFromServer().start();
				out.println(username);
				btnLogin.setEnabled(false);
				btnLogout.setEnabled(true);
				btnSend.setEnabled(true);
			}
		});
		addressPane.add(btnLogin);

		btnLogout = new JButton("Logout");
		btnLogout.setEnabled(false);
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				out.println("/LOGOUT");
				btnLogin.setEnabled(true);
				btnLogout.setEnabled(false);
				btnSend.setEnabled(false);
				textArea.setText("");
				textArea_1.setText("");
			}
		});
		addressPane.add(btnLogout);

		textArea = new JTextArea(24, 0);
		textArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		contentPane.add(scrollPane, gbc_scrollPane);

		txtMessage = new JTextField(50);
		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}
		});
		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 0, 0, 5);
		gbc_txtMessage.gridy = 2;
		gbc_txtMessage.gridx = 0;
		contentPane.add(txtMessage, gbc_txtMessage);

		btnSend = new JButton("Send");
		btnSend.setEnabled(false);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendMessage();
			}
		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridy = 2;
		gbc_btnSend.gridx = 1;
		contentPane.add(btnSend, gbc_btnSend);

		textArea_1 = new JTextArea(0, 12);
		textArea_1.setEditable(false);

		JScrollPane scrollPane_1 = new JScrollPane(textArea_1);
		scrollPane_1.setBorder(new TitledBorder(null, "Online users",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.VERTICAL;
		gbc_scrollPane_1.gridheight = 3;
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 0;
		contentPane.add(scrollPane_1, gbc_scrollPane_1);

		pack();
	}

	private void sendMessage() {
		String msg = txtMessage.getText().trim();
		if (!msg.equals("")) {
			if (msg.equalsIgnoreCase("/LOGOUT")) {
				out.println("/LOGOUT");
				btnLogin.setEnabled(true);
				btnLogout.setEnabled(false);
				btnSend.setEnabled(false);
				textArea.setText("");
				textArea_1.setText("");
			} else
				out.println(msg);
			txtMessage.setText("");
		}
	}

	class ListenFromServer extends Thread {
		@Override
		public void run() {
			try {
				String msg;
				while ((msg = in.readLine()) != null) {
					if (msg.startsWith("@"))
						textArea_1.append(msg.substring(1) + "\n");
					else if (msg.equals("-"))
						textArea_1.setText("");
					else
						textArea.append(msg + "\n");
				}
			} catch (IOException e) {
			}
		}
	}
}
