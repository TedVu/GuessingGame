package assignment.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import assignment.server.KEEP_ALIVE_CODE;

public class ClientPingThread extends Thread {

	private Socket clientSocket;
	private Client client;
	private BufferedWriter outSocket;
	private static final int KEEP_ALIVE_INTERVAL = 1000;

	public ClientPingThread(Client client) {
		this.client = client;
		clientSocket = client.getConnection();
	}

	@Override
	public void run() {
		Socket pingSocket;

		try {
			while (!clientSocket.isClosed()) {
				pingSocket = new Socket("localhost", 9090);// your allocated port number on school server
				outSocket = new BufferedWriter(new OutputStreamWriter(pingSocket.getOutputStream()));
				outSocket.write(KEEP_ALIVE_CODE.PING.toString());
				outSocket.write("\n");
				outSocket.flush();
				Thread.sleep(KEEP_ALIVE_INTERVAL);
			}

		} catch (IOException e) {
			client.setServerDown();
			System.out.println("\nNo data can be sent please enter anything to continue: ");

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
