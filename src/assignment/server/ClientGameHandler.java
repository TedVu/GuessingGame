package assignment.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

import assignment.client.Status;

public class ClientGameHandler extends Thread {
	private Socket connection;
	private BufferedWriter out;
	private BufferedReader in;
	private int randomNum;
	private String clientName;
	private int numClientGuess = 1;
	boolean playAgain = false;
	private boolean exitGuess = false;

	public ClientGameHandler(Socket connection) {
		this.connection = connection;
	}

	public ClientGameHandler(Socket connection, String clientName) {
		this.connection = connection;
		this.clientName = clientName;
	}

	/*
	 * (non-Javadoc) To be implement some logic for guessing game here
	 */
	@Override
	public void run() {
		int numGuess = 4;
		boolean guessSuccess = false;
		try {

			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			out.write("Welcome to guessing game\n");

			do {
				out.write("Please specify your guess number here:\n");
				out.flush();
				String inClient = in.readLine();
				if(this.isInterrupted()) {break;}
				if (inClient.equalsIgnoreCase("e")) {
					numClientGuess--;
					exitGuess = true;

					out.write("You exited the game - Please wait for others to finish the game...\n");
					out.flush();
					break;
				}
				try {
					int guessNum = Integer.parseInt(inClient);
					if (guessNum == randomNum) {
						guessSuccess = true;
						break;
					} else if (guessNum > randomNum) {
						out.write("Your guess is larger than the generated number\n");
						out.flush();
						++numClientGuess;
					} else {
						out.write("Your guess is smaller than the generated number\n");
						out.flush();
						++numClientGuess;
					}
					out.flush();
					--numGuess;

				} catch (NumberFormatException e) {
					--numGuess;
					out.write("Invalid input\n");
					out.flush();
				}
			} while (numGuess >= 1 && !this.isInterrupted());

			if (!exitGuess && !this.isInterrupted()) {
				if (guessSuccess) {
					out.write(Status.SUCCESS.toString());
					out.write("\n");
					out.write("Congratulations\n");
					out.flush();
				} else if (this.isAlive()) {
					out.write(Status.FAIL.toString());
					out.write("\n");
					out.write("Game Over - The correct number is: " + randomNum + "\n");
					out.flush();
				}
			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block

		} catch (IOException e) {
		}
	}

	public void setRandomNum(int randomNum) {
		this.randomNum = randomNum;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public Socket getConnection() {
		return connection;
	}

	public String getClientName() {
		return this.clientName;
	}

	public int getNumGuessClient() {
		return numClientGuess;
	}

	public boolean getExitGuess() {
		return exitGuess;
	}
}
