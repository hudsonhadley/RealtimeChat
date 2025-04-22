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
    }

    /**
     * Broadcasts a message to every client (including the one that sent it)
     * @param originName the client that sent the message
     * @param message the message the client sent
     * @throws IOException if an error occurs during sending
     */
    private void broadcast(String originName, String message) throws IOException {
    }

    /**
     * Begins the client thread for a particular client. This thread will wait for input from the client, and then
     * call the broadcast method.
     * @param name
     */
    private void startClientThread(String name) {

    }


    public static void main(String[] args) throws IOException {

    }
}
