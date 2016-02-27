package IHM;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import Client.ClientPOP3;
import Client.Message;

import javax.swing.SwingConstants;
import javax.swing.JPanel;

public class MessagingService {

	private JFrame frame;
	private ClientPOP3 client;
	private JTextField txtLogin;
	private JButton btnLogin;
	private JLabel lblLogin;
	private JLabel lblInformation;
	private JLabel lblPassword;
	private JPasswordField txtPassword;
	private JLabel lblNewMessage;
	private JButton btnSuivant;
	private static int nbMessages;
	private static int numeroMessage;
	private JButton btnPassword;
	private JTextArea jpaCorpsMessage;
	private JTextField txtAdresseServeur;
	private JLabel lblFrom;
	private JLabel lblObject;
	private JLabel lblDate;
	private JButton btnConnexionServeur;
	private JButton btnInfoMessagerie;
	private JButton btnValider;
	private int nbTentativeMdp;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MessagingService window = new MessagingService();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MessagingService() {
		initialize();
		nbTentativeMdp = 0;
		nbMessages = 0;
		numeroMessage = 0;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 566, 503);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(null);

		// Adresse serveur
		JLabel lblAdresseServeur = new JLabel("Adresse du serveur :");
		lblAdresseServeur.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAdresseServeur.setBounds(6, 27, 128, 15);
		frame.getContentPane().add(lblAdresseServeur);

		txtAdresseServeur = new JTextField();
		txtAdresseServeur.setBounds(146, 27, 215, 20);
		frame.getContentPane().add(txtAdresseServeur);
		txtAdresseServeur.setColumns(10);

		btnValider = new JButton("Valider");
		btnValider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					client = new ClientPOP3();
					String response = client.connect(txtAdresseServeur
							.getText());

					String rep = response.substring(0, 3);
					if (rep.equals("+OK")) {
						lblInformation.setText(response);
						lblLogin.setVisible(true);
						txtLogin.setVisible(true);
						btnLogin.setVisible(true);
						txtAdresseServeur.setEnabled(false);
						btnValider.setEnabled(false);
					}

				} catch (Exception e) {
					lblInformation.setText(e.getMessage());
				}
			}
		});
		btnValider.setBounds(379, 26, 124, 23);
		frame.getContentPane().add(btnValider);

		// Information
		lblInformation = new JLabel("");
		lblInformation.setForeground(new Color(30, 144, 255));
		lblInformation.setBounds(6, 434, 383, 23);
		frame.getContentPane().add(lblInformation);

		// Utilisateur
		lblLogin = new JLabel("Utilisateur :");
		lblLogin.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLogin.setBounds(49, 68, 85, 15);
		frame.getContentPane().add(lblLogin);

		txtLogin = new JTextField();
		txtLogin.setToolTipText("");
		txtLogin.setBounds(146, 65, 215, 20);
		frame.getContentPane().add(txtLogin);

		btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String response = client.login(txtLogin.getText());
					lblInformation.setText(response);

					String rep = response.substring(0, 3);
					if (rep.equals("+OK")) {
						lblPassword.setVisible(true);
						txtPassword.setVisible(true);
						btnPassword.setVisible(true);
						txtLogin.setEnabled(false);
						btnLogin.setEnabled(false);
					}
				} catch (IOException e1) {
					lblInformation.setText(e1.getMessage());
				}
			}
		});
		btnLogin.setBounds(379, 64, 124, 23);
		frame.getContentPane().add(btnLogin);

		// Mot de passe
		lblPassword = new JLabel("Mot de passe :");
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassword.setBounds(49, 105, 85, 15);
		frame.getContentPane().add(lblPassword);

		txtPassword = new JPasswordField();
		txtPassword.setBounds(146, 102, 215, 20);
		frame.getContentPane().add(txtPassword);
		txtPassword.setColumns(10);

		btnPassword = new JButton("Password");
		btnPassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					nbTentativeMdp++;
					String response = client.password(txtPassword.getText());
					lblInformation.setText(response);

					String rep = response.substring(0, 3);
					if (rep.equals("+OK") && nbTentativeMdp <= 3) {
						client.list();
						nbMessages = client.getNumberOfNewMessages();
						lblNewMessage.setText("Vous avez " + nbMessages
								+ " nouveaux messages.");

						txtPassword.setEnabled(false);
						btnPassword.setEnabled(false);
						btnSuivant.setVisible(true);
					}
				} catch (IOException e1) {
					lblInformation.setText(e1.getMessage());
				}
			}
		});
		btnPassword.setBounds(379, 101, 124, 23);
		frame.getContentPane().add(btnPassword);

		// Nombre de nouveaux messages
		lblNewMessage = new JLabel("");
		lblNewMessage.setBounds(12, 157, 303, 16);

		// Bouton suivant pour afficher message
		frame.getContentPane().add(lblNewMessage);

		btnSuivant = new JButton("Suivant");
		btnSuivant.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					numeroMessage++;

					if (numeroMessage <= nbMessages) {
						Message message = client.getMessage(numeroMessage);

						lblDate.setVisible(true);
						lblDate.setText(message.getDate());

						lblFrom.setVisible(true);
						lblFrom.setText("De : " + message.getSender());

						lblObject.setVisible(true);
						lblObject.setText("Objet : " + message.getObject());

						jpaCorpsMessage.setVisible(true);
						jpaCorpsMessage.setText(message.getBody());
					} else {
						lblInformation.setText("Tous les messages sont lus.");
						jpaCorpsMessage.setEnabled(false);
						btnSuivant.setEnabled(false);
						btnConnexionServeur.setVisible(true);
						btnInfoMessagerie.setVisible(true);
					}
				} catch (IOException e1) {
					lblInformation.setText(e1.getMessage());
				}
			}
		});
		btnSuivant.setBounds(379, 150, 124, 23);
		frame.getContentPane().add(btnSuivant);

		jpaCorpsMessage = new JTextArea();
		jpaCorpsMessage.setBounds(10, 225, 351, 184);
		jpaCorpsMessage.setLineWrap(true);
		frame.getContentPane().add(jpaCorpsMessage);

		lblFrom = new JLabel("De :");
		lblFrom.setBounds(6, 186, 200, 16);
		frame.getContentPane().add(lblFrom);

		lblObject = new JLabel("Sujet :");
		lblObject.setBounds(6, 206, 200, 16);
		frame.getContentPane().add(lblObject);

		lblDate = new JLabel("Date");
		lblDate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDate.setBounds(286, 206, 73, 16);
		frame.getContentPane().add(lblDate);

		btnConnexionServeur = new JButton("Connexion serveur");
		btnConnexionServeur.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String response = client.noop();
					lblInformation.setText(response);
				} catch (IOException e) {
					lblInformation.setText(e.getMessage());
				}
			}
		});
		btnConnexionServeur.setBounds(379, 350, 159, 23);
		frame.getContentPane().add(btnConnexionServeur);

		btnInfoMessagerie = new JButton("Information bo\u00EEte mail");
		btnInfoMessagerie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String response = client.stat();
					lblInformation.setText(response);
				} catch (IOException e) {
					lblInformation.setText(e.getMessage());
				}
			}
		});
		btnInfoMessagerie.setBounds(379, 386, 159, 23);
		frame.getContentPane().add(btnInfoMessagerie);
		
		// Fermeture de la fenêtre
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				try {
					deconnection();
				} catch (Exception e) {
					lblInformation.setText(e.getMessage());
				}
				System.exit(0);
			}
		});

		// On cache tout les élèments
		lblLogin.setVisible(false);
		txtLogin.setVisible(false);
		btnLogin.setVisible(false);
		lblPassword.setVisible(false);
		txtPassword.setVisible(false);
		btnPassword.setVisible(false);
		btnSuivant.setVisible(false);
		lblDate.setVisible(false);
		lblFrom.setVisible(false);
		lblObject.setVisible(false);
		jpaCorpsMessage.setVisible(false);
		btnConnexionServeur.setVisible(false);
		btnInfoMessagerie.setVisible(false);
	}

	public void deconnection() {
		try {
			client.logout();
			client.disconnect();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
