package Serveur;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Domaine.Constantes;

public class SocketServer {

	private ServerSocket serverSocket;

	public SocketServer() {
	}

	public void start() throws IOException {
		this.serverSocket = new ServerSocket(Constantes.DEFAULT_PORT_SERVER);
		Socket clientSocket = null;

		while (true) {
			clientSocket = serverSocket.accept();
			Thread thread = new Thread(new ServerPOP3(clientSocket));
			thread.start();
		}
	}
}