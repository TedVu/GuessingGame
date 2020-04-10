package testing;// A Java program to demonstrate working of 

// synchronized. 

// A Class used to send a message 
class Sender {
	public void send(String msg) {
		System.out.println("Sending\t" + msg);
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			System.out.println("Thread interrupted.");
		}
		System.out.println("\n" + msg + "Sent");
	}
}

// Class for send a message using Threads
class ThreadedSend extends Thread {
	private String msg;
	Sender sender;

	ThreadedSend(String m, Sender obj) {
		msg = m;
		sender = obj;
	}

	public void run() {

		// lock sender object here
		synchronized (sender) {
			sender.send(msg);
		}
	}
}

// Driver class
class Demo_1 {
	public static void main(String args[]) {
		Sender sender = new Sender();

		// two threads shares the same object
		ThreadedSend S1 = new ThreadedSend(" Hi ", sender);
		ThreadedSend S2 = new ThreadedSend(" Bye ", sender);

		S1.start();
		S2.start();

	}
}
