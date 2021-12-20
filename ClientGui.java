package mafia;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;


public class ClientGui extends JFrame implements ActionListener, Runnable{

	JFrame frame = new JFrame();
	Container cont = getContentPane();
	JPanel pan = new JPanel();
	JTextArea chatBox = new JTextArea();
	JScrollPane scrollPane = new JScrollPane(chatBox);
	JTextField text = new JTextField();
	Image image = new Image();
	
	Socket socket;
	BufferedReader input;
	static PrintWriter output;
	String str;
	
	public ClientGui(String ip, int port) {
        //frame
        setTitle("Find Mafia");
        setSize(1000, 700);
        setLocation(50, 50);
        init();
        start();
        setLocationRelativeTo(null);
        setVisible(true);

        //network initialization
        initNet(ip, port);
        System.out.println("ip : " + ip);
    }
	
	public ClientGui() {}
	
	//network initialization
	private void initNet(String ip, int port) {
		try {
			socket = new Socket(ip, port);
			
			//make input and output for communication
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);	
		} 
		catch (UnknownHostException e) {
			System.out.println("Different IP address");
		} 
		catch (IOException e) {
			System.out.println("Connect failed");
		}

		Thread thread = new Thread(this);
		thread.start();	//start run
	}
	 

	class Image extends JPanel {
		public void paintComponent(Graphics g) {
			Dimension d = getSize();
			ImageIcon image = new ImageIcon("C:\\Users\\jjy02\\eclipse-workspace2\\Mafia\\resources\\mafia3.jpg");	//add image
			g.drawImage(image.getImage(), 0, 0, 1000, 100, null);
			setSize(1000,100);
		}
	}
	
	
	//set layout
	private void init() {		
		cont.setLayout(new BorderLayout(10,90));
		cont.setBackground(Color.black);
		
//		scrollPane.setPreferredSize(new Dimension(700, 500));
//		text.setPreferredSize(new Dimension(700, 30));
		
		JLabel label = new JLabel("ют╥б");
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setOpaque(true); 
		label.setBackground(Color.black);
		label.setForeground(Color.white); 
		
		pan.setLayout(null);
		scrollPane.setBounds(140, 5, 700, 500);
		label.setBounds(140, 509, 50, 25);
		text.setBounds(190, 509, 650, 25);
		
		pan.add(scrollPane);
		pan.add(label);
		pan.add(text);
		pan.setBackground(Color.black);

		
		cont.add("Center", pan); //chat box
//		cont.add("South", pan1); // text field
		cont.add("North", image); //image on the top
	}
	
	private void start() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		text.addActionListener(this);
	}
	
	//Read the string sent from the server in response and output it to the chatBox
	@Override
	public void run() {
	      while(true) {
	         try {
	            str = input.readLine();	//read string
	            chatBox.append(str + "\n");
	            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
	            if(str.equalsIgnoreCase("Game Over")) {
	               
	               try {
	                  Thread.sleep(10 * 1000);
	               } catch (InterruptedException e) {
	               }
	               System.exit(0);	//end game
	            }
	               
	         } catch (IOException e) {
	            e.printStackTrace();
	         }
	      }
	   }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		str = text.getText();	//read string
		output.println(str);	//send to server
		text.setText("");
	}
}