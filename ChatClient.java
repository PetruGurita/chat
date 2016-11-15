package chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient {

	BufferedReader in;
	PrintWriter out;
	private JFrame frame = new JFrame("Chatter");
	private JTextField textField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 40);
	private JButton send = new JButton("Send"); 

	/**
	 * Constructs the client by laying out the GUI and registering a listener
	 * with the textfield so that pressing Return in the listener sends the
	 * textfield contents to the server. Note however that the textfield is
	 * initially NOT editable, and only becomes editable AFTER the client
	 * receives the NAMEACCEPTED message from the server.
	 */
	public ChatClient() {

		// Layout GUI
		textField.setEditable(false);
		messageArea.setEditable(false);
		frame.getContentPane().add(textField, "North");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		//frame.getContentPane().add(send, "Center");
		frame.pack();

		// Add Listeners
		textField.addActionListener(new ActionListener() {
			/**
			 * Responds to pressing the enter key in the textfield by sending
			 * the contents of the text field to the server. Then clear the text
			 * area in preparation for the next message.
			 */
			public void actionPerformed(ActionEvent e) {
				out.println(textField.getText());
				textField.setText("");
			}
		});
	}

	
	private String showTime() {
		// TO DO : CHANGE THE FONT 
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		int second = now.getSecond();
		String time ="[" + hour + ": " + minute + ": " + second + "]";
		return time;
	}
	/**
	 * Prompt for and return the desired screen name.
	 */
	private String getName() {
		String response =  JOptionPane.showInputDialog(frame, "Hello, choose a nickname:", "",
				JOptionPane.PLAIN_MESSAGE);
		return response;
	}

	/**
	 * Connects to the server then enters the processing loop.
	 */
	private void run() throws IOException {
		
		//local address
		String serverAddress = "localhost"; 
		Socket socket = new Socket(serverAddress, 9001);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		
		// Process all messages from server, according to the protocol.
		while (true) {
			
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				String name = getName();
				if(name != "")
					out.println(name);
				else
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			} else if (line.startsWith("NAMEACCEPTED")) {
				textField.setEditable(true);
			} else if (line.startsWith("MESSAGE")) {
				messageArea.append(showTime() + " " + line.substring(8) + "\n");
			}
		}
	}

	/**
	 * Runs the client as an application with a closeable frame.
	 */
	public static void main(String[] args) throws IOException  {
			ChatClient client = new ChatClient();
			client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			client.frame.setVisible(true);
			client.run();
	}
}