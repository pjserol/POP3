package Serveur;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.text.MessageFormat;

import Domaine.Command;
import Domaine.Constantes;
import Domaine.State;

public class ServerPOP3 implements Runnable {
	private Socket socketClient;
	private DataOutputStream out;
	private State state;
	private State previousState;
	private String username;
	private Command command;
	private File mailboxFolder;
	private int nbEssaiLogin;
	private int nbEssaiPassword;
	
	public ServerPOP3(Socket socketClient) throws IOException {
		this.nbEssaiLogin = 3;
		this.nbEssaiPassword = 3;
		this.socketClient = socketClient;
		this.changeState(State.Closed);
	}
	
	public void run() {
		try {
			this.initDataOutputStream();
			this.doAction(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initDataOutputStream() throws IOException {
		this.out = new DataOutputStream(this.socketClient.getOutputStream());
	}
	
	private void doAction(Object parameter) throws IOException {
		switch (this.state) {
			case Closed :
				this.changeState(State.AuthorizationUser);
				this.sendMessage(MessageFormat.format(Constantes.WELCOME_MESSAGE_SERVER, Constantes.RESPONSE_NOOP_OK, InetAddress.getLocalHost().getHostName()));
				break;
			case AuthorizationUser :
			case AuthorizationPassword :
			case Transaction :
			case ErrorCommand :
				this.sendMessage(this.executeCommand(parameter));
				break;
			case Update :
			case TimeOut :
				this.socketClient.close();
				this.out.close();
				break;
		}
	}
	
	private void sendMessage(String message) throws IOException {
		this.out.write(message!= null ? message.getBytes() : "".getBytes());
		this.out.flush();
		if(this.state != State.Update && this.state != State.TimeOut) {
			this.receiveMessage();
		}
	}
	
	private void receiveMessage() throws IOException {
		Object parameter = null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(this.socketClient.getInputStream()));
		//while(!reader.ready()) {
			parameter = this.recognizeCommand(reader.readLine().toUpperCase());
			this.doAction(parameter);
		//}
	}
	
	private Boolean checkUser(String username) throws IOException {
		if(username.length() <= 0) {
			return false;
		}
		
		String serverName = InetAddress.getLocalHost().getHostName();
		Boolean isMailAddr = username.matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}");
		this.username = isMailAddr ? username.substring(0, username.lastIndexOf("@")) : username;
		
		if(!isMailAddr) {
			username += serverName.toUpperCase() + ".FR";
		}
		
		this.mailboxFolder = new File(MessageFormat.format(Constantes.FOLDER_MAIL, this.username));
		//TODO GCA : Aller chercher domaine au lieu de le construire
		return this.mailboxFolder.exists() && username.endsWith(serverName.toUpperCase() + ".FR");
	}
	
	private Boolean checkPassword(String password) throws IOException {
		if(password.length() <= 0) {
			return false;
		}
		
		String line = "";
		Boolean isGoodPassword = false;
		BufferedReader reader = new BufferedReader(new FileReader(Constantes.PASS_FILE));
		
		while (reader.ready()) {
			line = reader.readLine();
			if(line.startsWith(this.username.toLowerCase())) {
				isGoodPassword = line.endsWith(password.toLowerCase());
			}
		}
		
		reader.close();
		return isGoodPassword;
	}
	
	private int countMessageInMailbox() {
		return this.mailboxFolder.list().length;
	}
	
	private int countSizeOfMailbox() {
		int size = 0;
		for (File mail : this.mailboxFolder.listFiles()) {
			size += mail.length();
		}
		return size;
	}
	
	private String listMailbox() {
		String response = "";
		int i = 1;
		
		for (File mail : this.mailboxFolder.listFiles()) {
			response += MessageFormat.format("{0} {1}\r\n", i, mail.length());
			i++;
		}
		
		return response;
	}
	
	private Object recognizeCommand(String in) {
		Boolean requestHasParameter = true;
		Object parameter = null;
		
		try {
			if(in.startsWith("RETR")) {
				this.command = Command.RETR;
				parameter = Integer.valueOf(in.substring(5));
			} else if (in.startsWith("LIST")) {
				this.command = Command.LIST;
				if(in.length() > 5) {
					parameter = Integer.valueOf(in.substring(5));
				}
			} else if(in.contains("USER")) {
				this.command = Command.USER;
				parameter = in.substring(in.indexOf("USER ") + 5);
			} else if(in.startsWith("PASS")) {
				this.command = Command.PASS;
				parameter = in.substring(5);
			} else {
				this.command = Command.valueOf(in.substring(0, 4));
				requestHasParameter = false;
			}
		} catch(Exception ex) {
			this.stateError();
		}
		
		return requestHasParameter ? parameter : 0;
	}
	
	private String readMail(int mailNumber) throws FileNotFoundException, IOException {
		String response = "";
		String line;
		
		BufferedReader reader = new BufferedReader(new FileReader(this.mailboxFolder.listFiles()[mailNumber - 1]));
		
		while((line = reader.readLine()) != null) {
			response += MessageFormat.format("{0}\r\n",line);
		}
		
		reader.close();
		
		return response;
	}
	
	private long getSizeOfMail(int mailNumber) {
		return this.mailboxFolder.listFiles()[mailNumber - 1].length();
	}
	
	private String executeCommand(Object parameter) throws IOException {
		String response = null;
		
		if(this.nbEssaiLogin == 0 || this.nbEssaiPassword == 0) {
			this.command = Command.QUIT;
		}
		
		try {
			switch(this.command) {
				case USER :
					if(this.state == State.AuthorizationUser) {
						if(this.checkUser((String)parameter)) {
							this.changeState(State.AuthorizationPassword);
							response = MessageFormat.format(Constantes.MESSAGE_OK_USER, Constantes.RESPONSE_NOOP_OK, this.username);
						} else {
							this.nbEssaiLogin --;
							response = MessageFormat.format(Constantes.MESSAGE_ERREUR, Constantes.ERROR_UNKNOWN_USER);
						}
					} else {
						response = MessageFormat.format(Constantes.MESSAGE_ERREUR, Constantes.ERROR_COMMAND);
					}
					break;
				case PASS : 
					if(this.state == State.AuthorizationPassword) {
						if(this.checkPassword((String)parameter)) {
							this.changeState(State.Transaction);
							int numberOfMessage = this.countMessageInMailbox();
							int sizeOfMailbox = this.countSizeOfMailbox();
							response = MessageFormat.format(Constantes.MESSAGE_RSET_OK, Constantes.RESPONSE_NOOP_OK, this.username, numberOfMessage, sizeOfMailbox);
						} else {
							this.nbEssaiPassword --;
							response = MessageFormat.format(Constantes.MESSAGE_ERREUR, Constantes.ERROR_WRONG_PASSWORD);
						}
					} else {
						response = MessageFormat.format(Constantes.MESSAGE_ERREUR, Constantes.ERROR_COMMAND);
					}
					break;
				case STAT :
					if(this.state == State.Transaction) {
						response = MessageFormat.format(Constantes.RESPONSE_STAT_OK, Constantes.RESPONSE_NOOP_OK, this.countMessageInMailbox(), this.countSizeOfMailbox());
					} else {
						response = MessageFormat.format(Constantes.MESSAGE_ERREUR, Constantes.ERROR_COMMAND);
					}
					break;
				case LIST :
					if(this.state == State.Transaction) {
						int numberMailInMailbox = this.countMessageInMailbox();
						if(numberMailInMailbox > 0) {
							if(parameter != null) {
								response = MessageFormat.format(Constantes.RESPONSE_STAT_OK, Constantes.RESPONSE_NOOP_OK, (int)parameter, this.getSizeOfMail((int)parameter));
							} else {
								response = MessageFormat.format(Constantes.RESPONSE_LIST_OK, Constantes.RESPONSE_NOOP_OK, numberMailInMailbox, this.countSizeOfMailbox(), this.listMailbox());
							}
						} else {
							response = MessageFormat.format(Constantes.RESPONSE_LIST_OK_EMPTY, Constantes.RESPONSE_NOOP_OK);
						}
					} else {
						response = MessageFormat.format(Constantes.MESSAGE_ERREUR, Constantes.ERROR_COMMAND);
					}
					break;
				case RETR :
					if(this.state == State.Transaction) {
						response = MessageFormat.format(Constantes.RESPONSE_RETR_OK, Constantes.RESPONSE_NOOP_OK, this.getSizeOfMail((int)parameter), this.readMail((int)parameter));
					} else {
						response = MessageFormat.format(Constantes.MESSAGE_ERREUR, Constantes.ERROR_COMMAND);
					}
					break;
				case NOOP :
					if(this.state == State.Transaction) {
						response = MessageFormat.format("{0}\r\n",Constantes.RESPONSE_NOOP_OK);
					} else {
						response = MessageFormat.format(Constantes.MESSAGE_ERREUR, Constantes.ERROR_COMMAND);
					}
					break;
				case QUIT :
					this.changeState(this.state == State.Transaction ? State.Update : State.TimeOut);
					if(this.state == State.Update) {
						int numberOfMessage = this.countMessageInMailbox();
						String numberOfMailInMailbox = numberOfMessage > 0 ? String.valueOf(numberOfMessage) : Constantes.MESSAGE_EMPTY_MAILDROP;
						response = MessageFormat.format(Constantes.RESPONSE_QUIT_OK, Constantes.RESPONSE_NOOP_OK, InetAddress.getLocalHost().getHostName(), numberOfMailInMailbox);
					} else {
						response = MessageFormat.format(Constantes.RESPONSE_QUIT_NOK, Constantes.RESPONSE_NOOP_OK, InetAddress.getLocalHost().getHostName());
					}
					break;
				default :
					this.state = this.previousState;
					response = MessageFormat.format(Constantes.MESSAGE_ERREUR, Constantes.ERROR_COMMAND);
					break;
			}
		} catch(ArrayIndexOutOfBoundsException ex) {
			response = MessageFormat.format(Constantes.MESSAGE_ERREUR, MessageFormat.format(Constantes.ERROR_PARAM_RETR, this.countMessageInMailbox()));
		}
		return response;
	}
	
	private void changeState(State newState) {
		this.state = newState;
	}
	
	private void stateError() {
		this.previousState = this.state; 
		this.state = State.ErrorCommand;
		this.command = Command.ERR;
	}
}
