package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {
	private ArrayList<ConnectThread> al;
	private SimpleDateFormat sdf;
	private int port;
	private boolean keepGoing;

	public Server() {
		this(9999);
	}

	public Server(int port) {
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		al = new ArrayList<ConnectThread>();
		keepGoing = true;
		try {
			ServerSocket server = new ServerSocket(port);
			System.out.println(sdf.format(new Date())
					+ " chatroom Server start.");
			while (keepGoing) {
				Socket sock = server.accept();
				if (!keepGoing)
					break;
				ConnectThread ct = new ConnectThread(sock);
				al.add(ct);
				ct.start();
			}
			server.close();
		} catch (IOException e) {
		}
	}

	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		} catch (IOException e) {
		}
	}

	private synchronized void broadcast(String msg) {
		String time = sdf.format(new Date());
		System.out.println(time + " " + msg);
		// send message to everyone
		for (ConnectThread ct : al)
			if (!ct.writeMsg(time + " " + msg))
				al.remove(ct);
	}

	private synchronized void broadcastCommand(String msg) {
		for (ConnectThread ct : al)
			if (!ct.writeMsg(msg))
				al.remove(ct);
	}

	public static void main(String[] args) {
		new Server();
	}

	class ConnectThread extends Thread {
		private Socket sock;
		private BufferedReader in;
		private PrintWriter out;
		private String username;

		public ConnectThread(Socket sock) {
			this.sock = sock;
			try {
				out = new PrintWriter(sock.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(
						sock.getInputStream()));
				username = in.readLine();
				System.out.println(sdf.format(new Date()) + " " + username
						+ " login.");
			} catch (IOException e) {
				return;
			}
		}

		@Override
		public void run() {
			try {
				String msg;
				boolean keepGoing = true;
				refreshUserList();
				while (keepGoing && (msg = in.readLine()) != null) {
					if (msg.equals("/LOGOUT")) {
						keepGoing = false;
						System.out.println(sdf.format(new Date()) + " "
								+ username + " logout.");
					} else
						broadcast(username + ": " + msg);
				}
				if (keepGoing)
					System.out.println(sdf.format(new Date()) + " " + username
							+ " disconnect.");
				al.remove(this);
				close();
				refreshUserList();
			} catch (IOException e) {
				return;
			}
		}

		private void close() {
			if (out != null)
				out.close();
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
			try {
				if (sock != null)
					sock.close();
			} catch (IOException e) {
			}
		}

		private void refreshUserList() {
			broadcastCommand("-");
			for (ConnectThread ct : al)
				broadcastCommand("@" + ct.username + ":"
						+ ct.sock.getPort());
		}

		private boolean writeMsg(String msg) {
			if (!sock.isConnected()) {
				al.remove(this);
				close();
				return false;
			}
			out.println(msg);
			return true;
		}
	}
}
