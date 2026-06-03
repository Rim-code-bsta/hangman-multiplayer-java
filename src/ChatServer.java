
/**
 *
 * @author Rim
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

    private ServerSocket server = null;
    
    //Hangman Game Global Variables
    private static ClientHandler word_setter = null;
    private static ClientHandler word_guesser = null;
    private static String word_toBe_guessed = null;
    private static char[] array_word;
    private static int attemptsLeft = 6; // In the Hangman game,players are allowed
    //only 6 incorrect guesses (head, body, 2 arms,and 2 legs)
    private static boolean Start = false;
    
    private static List<Character> Guessed_already = new ArrayList<>();
    
    //Since we are dealing with multi-threaded coding, we will use here a thread safe list of clients
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public ChatServer(int port) {
        try {  
            // Here will go the succesful case but that may cause exceptions that
            //will be handled in the catch part of the code

            server = new ServerSocket(port);
            System.out.println("Server started on the following port " + port);
            System.out.println("Waiting for the players ...");

            while(true){
                Socket socket = server.accept(); //Once a client connects, a socket object is created,
                // so that communication can start
                System.out.println("New Client accepted and connected: "+ socket.getInetAddress());
                // In line 35, the socket.getInetAddress() will retrieve the remote IP address to which the socket the server is currently connected.

                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                
                assignRole(client);
                
                new Thread(client).start();
            }
        } catch (IOException e) {
            System.out.println("Server Error here " + e.getMessage());
        }
    }

    //Assign Player Roles: either a Setter, a Guesser or just a spectator.
    public static void assignRole(ClientHandler client){
         if(word_setter == null){
             word_setter = client;
             client.setRole("SETTER");
             client.sendMessage("Dear Player, you are the word Setter. Use: Set your word. ");
         }else if(word_guesser == null){
             word_guesser = client;
             client.setRole("GUESSER");
             client.sendMessage("You are the GUESSER. Wait for the word to be set.");
             if(Start){
                client.sendMessage("Game Started!");
                client.sendMessage("Word: " + new String(array_word));
                client.sendMessage("Attempts left:" + attemptsLeft);
             }
        }else{
             client.setRole("SPECTATOR");
             client.sendMessage("The Game is already full (only 2 players can play at a time). You are a spectator.");
         
         }
    
    }
    
    //After the setter sets the word, we can now start the game.
    public static void startGame(String word){
           word_toBe_guessed = word.toLowerCase();
           array_word = new char[word_toBe_guessed.length()];
           Arrays.fill(array_word,'_');
           attemptsLeft = 6;
           Start = true;
           broadcast("Game Started!");
           broadcast("Word: " + new String(array_word));

           broadcast("Attempts left:" + attemptsLeft);
    }
    
    //Process the guess
    public static void process_Guess(char the_guess){
        
        boolean correct = false;
        
        //Handling the case if the Guesser guesses the same letter twice.
        
        if(Guessed_already.contains(the_guess)){
           word_guesser.sendMessage("You already guessed '"+ the_guess +"'! Try another guess please. ");
           return;
        }
        Guessed_already.add(the_guess);
        
        for(int i = 0; i < word_toBe_guessed.length(); i++ ){
             if(word_toBe_guessed.charAt(i)== the_guess){
                 array_word[i] = the_guess;
                 correct = true;
             
             }
        
        }
        if( !correct){ //If the guess is wrong, the attempts left get decremented by one.
             attemptsLeft--;
             broadcast("Your guess is wrong :( !");
        
        }else{
             broadcast("Your guess is correct :) !");
        
        }
        
        broadcast("Word: " + new String(array_word));

        broadcast("Attempts left:" + attemptsLeft);
        
        checkGameState();
    
    }
    
    private static void checkGameState(){
        
        if(String.valueOf(array_word).equals(word_toBe_guessed)){
            broadcast("The Guesser Wins. The word set was : "+ word_toBe_guessed );
            resetGame();
        }
        
        if(attemptsLeft == 0){
            broadcast("The Setter Wins. The word set was : "+ word_toBe_guessed );
            resetGame();
        }
        
        
        
    
    }
    
    //Now we need to implement the following function becausee what if the players want to replay another
    //round after the game ends, so the variables should not hold the old values anymore.
    public static void resetGame(){
          word_toBe_guessed = null;
          array_word = null;
          attemptsLeft = 6;
          Start = false;
          Guessed_already.clear();
    }
   
 
    //Let us now write the method for broadcasting the messaging to all clients
    public static void broadcast(String message){
        synchronized(clients){
            for(ClientHandler i : clients){
                
                    i.sendMessage(message);
   
            }
        }
    }
    
    
    public static boolean isGameStarted(){
           return Start;
    }
    
    public static ClientHandler  getWordSetter(){
           return word_setter;
    }

    
   public static ClientHandler  getGuesser(){
           return word_guesser;
    }
    
    
    //Creating a method that removes disconnected clients
    public static void removeClient(ClientHandler client){
        clients.remove(client);
    }



    public static void main(String[] args){
        if(args.length!= 1){
            System.out.println("Usage: java ChatServer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new ChatServer(port);
    }
}

/*
Now we need to handle the communication part for one client as an independant class
*/

//We will make the class implementing the runnabble interface because java does not support
// multiple inheretance but with this interface we can achieve multitasking and concurrency
//in an efficient way.

class ClientHandler implements Runnable{
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    
    private String role = "UNKNOWN";

    //the constructor
    public ClientHandler(Socket socket ){
        this.socket = socket;
        try{
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        }catch(IOException e){
            System.out.println("Error setting up client streams :" + e.getMessage());
        }
    }
    
     public void setRole(String role){
         
         this.role = role;
     
     }

    //We need to override the behavior run() otherwise we need to declare this class as abstract
    @Override
    public void run(){
        try{

            String message;
            while(true){
                message = in.readUTF().trim();

                //word Setter Command
                if(message.startsWith("SETWORD")){
                    if(!role.equals("SETTER")){
                        sendMessage("You are not the word Setter!");
                        continue;
                        
                        
                    }
                    if(ChatServer.isGameStarted()){
                        sendMessage("Game already started.");
                        continue;
                    }

                    
                    String word = message.substring(8).trim();
                    if(word.isEmpty()){
                       sendMessage("The word is invalid.");
                       continue;
                    }
                    
                    ChatServer.startGame(word);
                }else if(message.startsWith("GUESS")){

                    if(!role.equals("GUESSER")){
                         sendMessage("You are not the guesser!");
                         continue;
                     }

                    if(!ChatServer.isGameStarted()){
                          sendMessage("Game has not started yet.");
                          continue;
                      }

                     String guessString = message.substring(6).trim();
                     
                     //We need to chandle the case if the guesser enters something different than a letter (like a number):
                     if(!Character.isLetter(guessString.charAt(0))){
                         sendMessage("Please try again and guess a letter not a number or a special symbol.");
                         continue;
                     }

                     if(guessString.length() != 1){
                           sendMessage("Please guess ONE letter.");
                           continue;
                       }

                      char guess = Character.toLowerCase(guessString.charAt(0));

                       ChatServer.process_Guess(guess);

                    }else{
                     sendMessage("Unknown command.");
                
                }
            }
              }catch(IOException ex){
                   System.out.println("Client disconnected.");
              }finally{ // this finally block will get excecuted anyways
                    try{
                        ChatServer.removeClient(this);
                        if(in != null) in.close();
                        if(out != null) out.close();
                        if(socket != null) socket.close();
                
            }catch(IOException e){
                System.out.println("Here an error happened when it came to closing client: " + e.getMessage());
            }
        }
    }

    //Now we are still left with defining the method of sendMessage()
    public void sendMessage(String message){
        try{
            out.writeUTF(message);
            out.flush();
        }catch(IOException ex){
            System.out.println("An error happened while sending the message.");
            ChatServer.removeClient(this); // remove client if sending fails
        }
    }

 
}
