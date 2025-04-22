import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A ChatServer manages a collection of Clients. The main structure is that the server receives a message from a
 * particular client. The server then broadcasts that message to all the other clients.
 */
public class ChatServer {
    private ServerSocket serverSocket;
    public final int PORT;
    public final String HOST;
    ConcurrentHashMap<String, Socket> activeClients;

    public ChatServer(int port) throws IOException {
        PORT = port;
        serverSocket = new ServerSocket(PORT);
        HOST = InetAddress.getLocalHost().getHostAddress();
        activeClients = new ConcurrentHashMap<>();
    }

    /**
     * Starts the server. This method will loop infinitely until the program is ended. The basic structure is that in
     * each loop, the server will accept a connection, ask for a name, then start that particular client thread.
     * @throws IOException if an error occurs in the server
     */
    public void start() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            String name = ""; // Will get initialized
            // Get their name
            boolean approved = false;
            while (!approved) {
                ChatMessage namePackage = ChatMessage.getPackage(input);

                name = namePackage.getSender();

                if (validName(name)) {
                    activeClients.put(name, clientSocket);
                    // Give approval
                    ChatMessage approval = new ChatMessage("server", "approved");
                    for (byte b : approval.flatten()) {
                        output.writeByte(b);
                    }
                    output.flush();
                    approved = true;
                    System.out.println(name + " connected");
                }
            }
            startClientThread(name);
        }

    }

    private boolean validName(String name) {
        if (name.contains(">") || activeClients.containsKey(name)) {
            return false;
        }

        return true;
    }

    /**
     * Broadcasts a message to every client (including the one that sent it)
     * @param originName the client that sent the message
     * @param message the message the client sent
     * @throws IOException if an error occurs during sending
     */
    private void broadcast(String originName, String message) throws IOException {
        broadcast(new ChatMessage(originName, message));
    }

    /**
     * Broadcasts a message to every client (including the one that sent it)
     * @param messagePacket the ChatMessage to broadcast
     */
    private void broadcast(ChatMessage messagePacket) throws IOException {
        for (String client : activeClients.keySet()) {
            DataOutputStream output = new DataOutputStream(activeClients.get(client).getOutputStream());

            for (byte b : messagePacket.flatten()) {
                output.writeByte(b);
            }
            output.flush();
        }
    }

    /**
     * Begins the client thread for a particular client. This thread will wait for input from the client, and then
     * call the broadcast method.
     * @param name
     */
    private void startClientThread(String name) {
        Thread clientThread = new Thread(() -> {
            try {
                DataInputStream input = new DataInputStream(activeClients.get(name).getInputStream());
                while (true) {
                    ChatMessage receivedPackage = ChatMessage.getPackage(input);
                    System.out.println(receivedPackage.getSender() + "> " + receivedPackage.getMessage());
                    broadcast(receivedPackage);
                }

            } catch (IOException e) {
                System.out.println(name + " disconnected");
                activeClients.remove(name);
            }
        });
        clientThread.start();
    }


    public static void main(String[] args) throws IOException {
        Scanner inScanner = new Scanner(System.in);
        System.out.print("Enter port> ");
        int port;
        while (true) {
            try {
                port = Integer.parseInt(inScanner.nextLine());

                if (port >= 0) {
                    break;
                } else {
                    System.out.println("Invalid port");
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid port");
            }
        }

        ChatServer server = new ChatServer(port);
        System.out.println("Starting server:");
        System.out.printf("Host: %s\n", server.HOST);
        System.out.printf("Port: %d\n", server.PORT);
        server.start();
    }
}
