import java.nio.charset.StandardCharsets;

/**
 * A class that is the standard for messages in this project. Messages consist of a header and a body. The header tells
 * who the sender is. The body is the message they sent.
 */
public class ChatMessage {
    private String sender;
    private String message;

    public ChatMessage(String sender, String message) throws IllegalArgumentException {
        if (message.length() > 255) {
            throw new IllegalArgumentException("Message must be less than or equal to 255 characters");
        } else if (sender.length() > 255) {
            throw new IllegalArgumentException("Sender must be less than or equal to 255 characters");
        }
        this.sender = sender;
        this.message = message;
    }

    public ChatMessage(byte[] rawData) {
        int lengthOfHeader = rawData[0];
        int lengthOfBody = rawData[lengthOfHeader];

        sender = new String(rawData, 0, lengthOfHeader);
        message = new String(rawData, lengthOfHeader+1, lengthOfBody);
    }

    /**
     * Produces a byte array of data based on the sender and message. The byte array's first byte is how many bytes
     * long the header is. Then is the header. Then is how many bytes long the body is. Then is the body.
     * @return The byte array of data to send
     */
    public byte[] flatten() {
        byte[] header = sender.getBytes(StandardCharsets.UTF_8);
        byte[] body = message.getBytes(StandardCharsets.UTF_8);

        // Format is (length of header)header(length of body)body
        byte[] fullMessage = new byte[1 + header.length + 1 + body.length];
        fullMessage[0] = (byte) header.length;

        for (int i = 0; i < header.length; i++) {
            fullMessage[1 + i] = header[i];
        }

        fullMessage[header.length] = (byte) body.length;

        for (int i = 0; i < body.length; i++) {
            fullMessage[header.length+1 + i] = body[i];
        }

        return fullMessage;
    }
}
