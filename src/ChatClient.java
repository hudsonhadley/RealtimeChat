import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


/**
 * A Client that can connect to a certain server. ChatClients will use the ChatMessage class as a format for messages
 */
public class ChatClient {
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private final String HOST;
    private final int PORT;
    private final String SELF_HOST;
    private String name;
    public ChatClient(String host, int port) throws IOException {
        HOST = host;
        PORT = port;

        socket = new Socket(HOST, PORT);
        SELF_HOST = socket.getLocalAddress().getHostAddress();
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
    }

    /**
     * Sends a message to the server
     * @param message the String we want to send
     * @throws IOException if an error occurs during sending
     */
    public void sendMessage(String message) throws IOException {
        ChatMessage messagePackage = new ChatMessage(name, message);
        for (byte b : messagePackage.flatten()) {
            output.writeByte(b);
        }
        output.flush();
    }

    /**
     * Begins the thread that will read input from the console and send it
     */
    public void startConsoleThread() {
        Scanner inScanner = new Scanner(System.in);
        Thread consoleThread = new Thread(() -> {
            while (true) {
                System.out.print(name + "> ");
                String message = inScanner.nextLine();
                try {
                    sendMessage(message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        consoleThread.start();
    }

    /**
     * Starts the loop that receives data from the socket and prints it out
     * @throws IOException if an error occurs during receiving
     */
    public void socketStart() throws IOException {
        while (receive());
    }

    /**
     * Receives the data and processes the request, whether that is a command from the server, or a message to print
     * @return whether or not to stop receiving
     * @throws IOException if an error occurs during receiving
     */
    public boolean receive() throws IOException {
        ChatMessage received = ChatMessage.getPackage(input);
        System.out.println();
        System.out.println(received.getSender() + "> " + received.getMessage());
        System.out.print(name + "> ");
        return true;
    }

    /**
     * Tests if a certain IP address is valid
     * @param ip the String to test
     * @return true if the String is a valid IP address
     */
    public static boolean validIP(String ip) {
        // Ensure it is #.#.#.#
        if (!ip.matches("[0-9]*.[0-9]*.[0-9]*.[0-9]*")) {
            return false;
        }

        String[] numStrings = ip.split("\\.");
        for (String numString : numStrings) {
            int n = Integer.parseInt(numString);

            if (n < 0 || 255 < n) {
                return false;
            }
        }

        return true;
    }

    public boolean setName(String name) throws IOException {
        this.name = name;
        sendMessage("");

        ChatMessage received = ChatMessage.getPackage(input);
        // Server will send true if the name is okay
        if (received.getMessage().equals("approved")) {
            return true;
        } else {
            this.name = null;
            return false;
        }

    }

    public static void main(String[] args) throws IOException {
        Scanner inScanner = new Scanner(System.in);

        // Get host and port info
        System.out.print("Enter server ip> ");
        String serverIP = inScanner.nextLine();

        while (!validIP(serverIP)) {
            System.out.println("Invalid IP");
            System.out.print("Enter server ip> ");
            serverIP = inScanner.nextLine();
        }

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

        ChatClient client = new ChatClient(serverIP, port);
        boolean approvedName = false;
        while (!approvedName) {
            System.out.print("Enter name> ");
            String name = inScanner.nextLine();

            if (client.setName(name)) {
                approvedName = true;
            }
        }
        client.startConsoleThread();
        client.socketStart();
    }
}
