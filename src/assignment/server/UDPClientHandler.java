package assignment.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import assignment.client.CommunicationCode;

/**
 * @author Ted Vu - S3678491
 * 
 *         This class is responsible for handling ping between client and server
 */
public class UDPClientHandler extends Thread {
	private static final Logger logger = Logger.getLogger(Server.class.getName());

	private int clientPort;
	private String clientName;

	private boolean serverRunning = true;
	private DatagramSocket socket;

	public UDPClientHandler(int clientPort, String clientName) {

		this.clientPort = clientPort;
		this.clientName = clientName;
	}

	@Override
	public void run() {

		try {
			socket = new DatagramSocket();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		try {
			// after 10 seconds if no packet receive then terminate this thread
			socket.setSoTimeout(10000);

		} catch (SocketException e1) {
			System.out.println(e1.getMessage());
		}

		InetAddress ip;
		try {

			ip = InetAddress.getByName("localhost"); // where the client locates (i.e netprog1.csit.rmit.edu.au)
			while (serverRunning) {
				DatagramPacket serverupCodePacket = new DatagramPacket(CommunicationCode.SERVERUP.toString().getBytes(),
						CommunicationCode.SERVERUP.toString().length(), ip, clientPort);

				socket.send(serverupCodePacket);
				logger.log(Level.INFO,
						"Sending ping to " + clientName + " at " + serverupCodePacket.getSocketAddress());
				byte[] buf = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(buf, 1024);
				socket.receive(receivePacket);
				logger.log(Level.INFO,
						"Receiving ping from " + clientName + " at " + serverupCodePacket.getSocketAddress());
				Thread.sleep(5000);

			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			logger.log(Level.INFO, "UDP Socket at " + socket.getLocalSocketAddress() + " closes");
			serverRunning = false;

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}

	}

}
