package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import org.omg.CORBA.portable.UnknownException;

import Domaine.Command;
import Domaine.Constantes;
import Domaine.State;

public class ClientPOP3 {

	private Socket socketClient;
	private boolean debug;
	private String hostname;
	private int port;
	private BufferedReader reader;
	private BufferedWriter writer;
	private State etat;
	private Command command;
	private static int nbMessages;

	/**
	 * Méthode de connexion au serveur
	 * 
	 * @param host
	 * @return
	 * @throws IOException
	 */
	public String connect(String host) throws Exception, IOException {

		String response = connect(host, Constantes.DEFAULT_PORT_SERVER);

		return response;
	}

	/**
	 * Méthode de connexion au serveur
	 * 
	 * @param hostname
	 * @param port
	 * @return
	 * @throws IOException
	 */
	public String connect(String hostname, int port) throws Exception, IOException {
		try{
		this.debug = true;
		this.etat = State.WaitForWelcome;
		this.hostname = hostname;
		this.port = port;
		socketClient = new Socket();
		socketClient.connect(new InetSocketAddress(this.hostname, this.port));
		reader = new BufferedReader(new InputStreamReader(
				socketClient.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(
				socketClient.getOutputStream()));

		if (debug)
			System.out.println("Connected to the host : " + this.hostname + ":"
					+ this.port);
		}catch(UnknownHostException e){
			throw new Exception(Constantes.ERROR_UNKNOWN_HOST);
		}
		return readResponseLine();
	}

	/**
	 * Check connection of serveur
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return socketClient != null && socketClient.isConnected();
	}

	/**
	 * Disconnect socket from the server
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		if (!isConnected())
			throw new IllegalStateException(Constantes.ERROR_NOT_CONNECTED);
		socketClient.close();
		reader = null;
		writer = null;
		if (debug)
			System.out.println("Disconnected from the host");
	}

	/**
	 * Read and return line of data sent by the server:
	 * 
	 * @return
	 * @throws IOException
	 */
	protected String readResponseLine() throws IOException {
		String response = "";
		response += reader.readLine() + "\r\n";
		while (reader.ready()) {
			response += reader.readLine() + "\r\n";
			if (debug) {
				System.out.println("DEBUG [in] : " + response);
			}

			if (this.etat != State.WaitForTransaction) {
				this.changeState(!response.startsWith("-ERR"));
			} else {
				this.changeState(this.command == Command.QUIT);
			}

			if (response.startsWith("-ERR")) {
				throw new RuntimeException(MessageFormat.format(Constantes.ERROR_MESSAGE_CLIENT, response.replaceFirst("-ERR ", "")));
			}
		}

		return response;
	}

	/**
	 * Sending data command
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public String sendCommand(String command) throws IOException {
		if (debug) {
			System.out.println("DEBUG [out]: " + command);
		}
		writer.write(command + "\n");
		writer.flush();
		this.command = Command.valueOf(command.substring(0, 4).toUpperCase());
		this.changeState(false);
		return readResponseLine();
	}

	/**
	 * Method Login
	 * 
	 * @param username
	 * @return
	 * @throws IOException
	 */
	public String login(String username) throws IOException {
		return sendCommand(Constantes.COMMAND_USER + username);
	}

	/**
	 * Method Logout
	 * 
	 * @param password
	 * @return
	 * @throws IOException
	 */
	public String password(String password) throws IOException {
		return sendCommand(Constantes.COMMAND_PASS + password);
	}

	/**
	 * Commande List
	 * 
	 * @throws IOException
	 */
	public void list() throws IOException {
		String response = sendCommand(Constantes.COMMAND_LIST);
		String[] values = response.split(" ");
		nbMessages = Integer.parseInt(values[1]);
	}

	/**
	 * Commande Noop
	 * 
	 * @return
	 * @throws IOException
	 */
	public String noop() throws IOException {
		String response = sendCommand(Constantes.COMMAND_NOOP);
		return response;
	}

	/**
	 * Commande Stat
	 * 
	 * @return
	 * @throws IOException
	 */
	public String stat() throws IOException {
		String response = sendCommand(Constantes.COMMAND_STAT);
		return response;
	}

	/**
	 * Commande Quit
	 * 
	 * @throws IOException
	 */
	public void logout() throws IOException {
		sendCommand(Constantes.COMMAND_QUIT);
	}

	/**
	 * Renvoie le nombre de nouveaux messages
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getNumberOfNewMessages() throws IOException {
		return nbMessages;
	}

	/**
	 * Fonction qui renvoie les informations sur le message
	 * 
	 * @param i
	 *            Numéro du message
	 * @return le message
	 * @throws IOException
	 */
	public Message getMessage(int i) throws IOException {
		String response = sendCommand(Constantes.COMMAND_RETR + " " + i);
		return builtMessage(response);
	}

	/**
	 * Etat
	 * 
	 * @param commandeOk
	 * @throws IOException
	 */
	private void changeState(boolean commandeOk) throws IOException {
		switch (this.etat) {
		case WaitForWelcome:
			this.etat = State.Closed;
			break;
		case Closed:
			this.etat = State.WaitForLogin;
			break;
		case AuthorizationUser:
			this.etat = State.WaitForPassword;
			break;
		case Transaction:
			this.etat = State.WaitForTransaction;
			break;
		case WaitForLogin:
			this.etat = commandeOk ? State.AuthorizationUser : State.Closed;
			break;
		case WaitForPassword:
			this.etat = commandeOk ? State.Transaction
					: State.AuthorizationUser;
			break;
		case WaitForTransaction:
			if (commandeOk) {
				this.disconnect();
			} else {
				this.etat = State.Transaction;
			}
		default:
			break;
		}
	}

	/**
	 * Construction du message
	 * 
	 * @param response
	 * @return Le message
	 */
	private Message builtMessage(String response) {
		Message message = new Message();
		String headerValue = null;
		String body = "";
		String[] messageLines = response.split("\r\n");
		boolean header = true;
		for (int j = 1; j < messageLines.length; j++) {
			if (messageLines[j].length() == 0) {
				header = false;
			}
			;
			if (header == true) {
				int position = messageLines[j].indexOf(":");
				String headerName = messageLines[j].substring(0, position);
				switch (headerName) {
				case "Date":
					headerValue = messageLines[j].substring(position + 1,
							messageLines[j].length());
					message.setDate(headerValue);
					break;
				case "Object":
					headerValue = messageLines[j].substring(position + 1,
							messageLines[j].length());
					message.setObject(headerValue);
					break;
				case "To":
					headerValue = messageLines[j].substring(position + 1,
							messageLines[j].length());
					message.setRecipient(headerValue);
					break;
				case "Cc":
					headerValue = messageLines[j].substring(position + 1,
							messageLines[j].length());
					message.setCopy(headerValue);
					break;
				case "From":
					headerValue = messageLines[j].substring(position + 1,
							messageLines[j].length());
					message.setSender(headerValue);
					break;
				default:
					break;
				}
			} else {
				body += messageLines[j] + "\r\n";
			}
		}
		message.setBody(body);

		return message;
	}
}
