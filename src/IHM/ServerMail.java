package IHM;

import java.io.IOException;

import Serveur.SocketServer;

public class ServerMail {

	public static void main(String[] args) {
		try {
			new SocketServer().start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
