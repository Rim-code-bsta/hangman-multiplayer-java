
/**
 *
 * @author Rim
 */
import java.net.*;
import java.io.*;

public class ChatClient {

    private Socket socket = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;
    private HangmanChatGUI gui;
    private String username; // store username to filter it locally

    public ChatClient(String address, int port, HangmanChatGUI gui, String username) {
        this.gui = gui;
        this.username = username;

        try {
            socket = new Socket(address, port);
            System.out.println("Connected to server");

            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            //Using threads to be able to receive messages from the server
            Thread thread_received = new Thread(new ReceiveMessages());
            thread_received.start();

        } catch (IOException e) {
            System.out.println("Connection related error " + e.getMessage());
        }
    }

    //Once the user will press the send button, here is the method that the GUI will call  
    public void sendMessage(String message){
        try{
            out.writeUTF(message);
            out.flush();

        }catch(IOException e){
            System.out.println("An error occured while sending the message.");        
        }
    }

    //This method handles the cleaning and disconnection
    public void disconnect(){
        try{
            if(out != null) out.close();
            if(in != null) in.close();
            if(socket != null) socket.close();
            System.out.println("Disconnected from server :(");

        }catch(IOException e){
            System.out.println("An error occured while disconnecting and closing the ressources.");
        }
    }

    //I implemented this class to be able to access both in and gui
    class ReceiveMessages implements Runnable {

        @Override
        public void run(){
            try{
                String message;

                while(socket != null && !socket.isClosed()){
                    message = in.readUTF();
                    
                    gui.displayMessage(message); //Since I am implementing a GUI I will not just print the message
                    // using System.out.println(message) so that the core here would talk to the GUI.
                }

            }catch(IOException ex){
                gui.displayMessage("Unfortunately we lost the connection to the server !");
            }
        }
    }
}

//Because I am choosing to do the GUI, then the ChatClient class will no main because it only
//works as a networking engine this time so only the ChatServer and the HangmanChatGUI will have the main method.



