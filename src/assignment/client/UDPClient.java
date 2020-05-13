package assignment.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Ted Vu - S3678491
 * 
 *         This class is responsible for handling ping between client-server
 *
 */
public class UDPClient extends Thread {

	private boolean endGame = false;
	private DatagramSocket UDPClient;

	public UDPClient() {
		try {
			UDPClient = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String requestCon = CommunicationCode.REQUESTCONNECT.toString();
		InetAddress ip;
		try {
			ip = InetAddress.getByName("localhost"); // where the server locates (i.e: netprog2.csit.rmit.edu.au)
			DatagramPacket requestConPacket = new DatagramPacket(requestCon.getBytes(), requestCon.length(), ip, 9090); // port
																														// number
																														// of
																														// server

			UDPClient.send(requestConPacket);

			while (!endGame) {
				byte[] buf = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(buf, 1024);
				UDPClient.receive(receivePacket); // client will be blocked here wait for packet to act correspondingly

				String serverReply = new String(receivePacket.getData(), 0, receivePacket.getLength());

				if (serverReply.equalsIgnoreCase(CommunicationCode.SERVERUP.toString())) {
					DatagramPacket replyPacket = new DatagramPacket(requestCon.getBytes(), requestCon.length(), ip,
							receivePacket.getPort());
					UDPClient.send(replyPacket);

				}

			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setEndGame() {
		endGame = true;
	}
}
