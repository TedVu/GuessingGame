package assignment.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class KeepAliveThreadClient extends Thread {

	private Client client;

	public KeepAliveThreadClient(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		try {
			BufferedReader inSocket = new BufferedReader(
					new InputStreamReader(client.getConnection().getInputStream()));
			while (true && !client.getConnection().isClosed()) {
				if (inSocket.ready() && client.getGuessString() == null) {
					String keepAlive = inSocket.readLine();
					System.out.print("\n" + keepAlive + " ");
				} else if (client.getGuessString() != null) {
					break;
				}
			}
		} catch (IOException e) {
		}

	}

}
