package ltg.commons;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * This is an XMPP client that is ready to use. It handles only one connection which can be 
 * either point-to-point or a group chat. There are methods to actively (using 
 * <code>PacketCollector</code> and passively (using <code>PacketListener</code>) listen for
 * packets. There are send methods to send messages to the group chat or to individual users.
 * 
 * @author tebemis
 *
 */
public class SimpleXMPPClient {

	// Connection
	protected XMPPConnection connection = null;
	// Group chat (can be null)
	protected MultiUserChat groupChat = null;
	// Packet collector (can be null)
	protected PacketCollector packetCollector = null; 

	
	/**
	 * Creates a simple client and connects it to the XMPP server 
	 * 
	 * @param username
	 * @param password
	 */
	public SimpleXMPPClient(String username, String password) {
		// Parse username and hostname
		String[] sa = username.split("@", 2);
		String uname = null;
		String hostname = null;
		try {
			uname = sa[0];
			hostname = sa[1];
		} catch (Exception e) {
			System.err.println(username + " is not a valid JID, impossible to CONNECT to the XMPP server, terminating");
			System.exit(-1);
		}
		// Connect
		try {
			connection = new XMPPConnection(hostname);
			connection.connect();
		} catch (XMPPException e) {
			System.err.println("Impossible to CONNECT to the XMPP server, terminating");
			System.exit(-1);
		}
		// Authenticate
		try {
			connection.login(uname, password);
		} catch (XMPPException e) {
			System.err.println("Impossible to LOGIN to the XMPP server, terminating");
			System.exit(-1);
		} catch (IllegalArgumentException e) {
			// This needs to be here because the MultiUserChat implementation
			// in smackx is crappy. They throw exceptions if the username is "".
			System.err.println("Impossible to LOGIN to the XMPP server, terminating");
			System.exit(-1);
		}
	}

	
	/**
	 * Creates a simple client, connects it to the server and joins a chat room.
	 * 
	 * @param username
	 * @param password
	 * @param chatRoom
	 */
	public SimpleXMPPClient(String username, String password, String chatRoom) {
		// Connect and authenticate
		this(username, password);
		if (connection.isAuthenticated() && chatRoom!=null) {
			// Initialize and join chatRoom
			groupChat = new MultiUserChat(connection, chatRoom);
			try {
				groupChat.join(connection.getUser());
			} catch (XMPPException e) {
				System.err.println("Impossible to join GROUPCHAT, terminating");
				System.exit(-1);
			}
		}
	}

	
	/**
	 * Fetches the last message in the queue.
	 * 
	 * @return
	 */
	public Message nextMessage() {
		if (packetCollector==null)
			packetCollector = connection.createPacketCollector(new PacketTypeFilter(Message.class));
		return (Message) packetCollector.nextResult();
	}

	
	/**
	 * Registers an event listener.
	 * 
	 * @param eventListener
	 */
	public void registerEventListener(final MessageListener eventListener) {
		PacketListener pl = new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				eventListener.processMessage((Message) packet);
			}
		};
		connection.addPacketListener(pl, new PacketTypeFilter(Message.class));
	}

	
	/**
	 * Sends a point to point message to another client.
	 * 
	 * @param to
	 * @param message
	 */
	public void sendMessage(String to, String message) {
		if (connection==null || !connection.isAuthenticated()){
			System.err.println("Impossible to send message to " +to + ": we have been disconnected! Terminating");
			System.exit(-1);
		}
		Message m = new Message(to, Message.Type.normal);
		m.setBody(message);
		connection.sendPacket(m);
	}

	
	/**
	 * Sends a message to the whole group chat.
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		if (connection==null || !connection.isAuthenticated() || !groupChat.isJoined()){
			System.err.println("Impossible to send message to groupchat: we have been disconnected! Terminating");
			System.exit(-1);
		}
		Message m = new Message(groupChat.getRoom(), Message.Type.groupchat);
		m.setBody(message);
		connection.sendPacket(m);
	}
	
	
	/**
	 * Returns the username.
	 * 
	 * @return
	 */
	public String getUsername() {
		return connection.getUser();
	}


	/**
	 * Disconnects the client.
	 */
	public void disconnect() {
		if (groupChat!=null && groupChat.isJoined()) {
			groupChat.leave();
			groupChat = null;
		}
		if (connection.isAuthenticated()) {
			connection.disconnect();
			connection = null;
		}
	}

}
