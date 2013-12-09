package ltg.commons;

import java.util.ArrayList;
import java.util.List;

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
	protected List <MultiUserChat> chatRooms = null;
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
		String[] sa;
		String uname = null;
		String hostname = null;
		try {
			sa = splitUserAndHost(username);
			uname = sa[0];
			hostname = sa[1];
		} catch (XMPPException e1) {
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
	 * Creates a simple client, connects to the server and joins a chat room.
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
			chatRooms = new ArrayList<MultiUserChat>();
			chatRooms.add(new MultiUserChat(connection, chatRoom));
			try {
				chatRooms.get(0).join(connection.getUser());
			} catch (XMPPException e) {
				System.err.println("Impossible to join GROUPCHAT, terminating");
				System.exit(-1);
			}
		}
	}
	
	
	/**
	 * Creates a simple client, connects to the server and joins a list of chat rooms.
	 * 
	 * @param username
	 * @param password
	 * @param chatRooms
	 */
	public SimpleXMPPClient(String username, String password, List<String> chatRooms) {
		// Connect and authenticate
		this(username, password);
		if (connection.isAuthenticated() && chatRooms!=null && !chatRooms.isEmpty()) {
			// Initialize and join chatRoom
			this.chatRooms = new ArrayList<MultiUserChat>();
			for(String cr : chatRooms)
				this.chatRooms.add(new MultiUserChat(connection, cr));
			try {
				for (MultiUserChat cr : this.chatRooms)
					cr.join(connection.getUser());
			} catch (XMPPException e) {
				System.err.println("Impossible to join one or more GROUPCHAT, terminating");
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
	 * This method is used only when the client is connected
	 * to one and only one group chat.
	 * 
	 * @param message
	 */
	public void sendMUCMessage(String message) {
		if (connection==null || !connection.isAuthenticated() ) {
			System.err.println("Impossible to send message to groupchat: we have been disconnected! Terminating");
			System.exit(-1);
		}
		if ( !onlyOneRoomJoined() ) {
			System.err.println("Can't use this method when multiple group chats have been joined. "
					+ "How do I know to which one of them I should send your message to?");
			return;
		}
		Message m = new Message(chatRooms.get(0).getRoom(), Message.Type.groupchat);
		m.setBody(message);
		connection.sendPacket(m);
	}
	
	
	/**
	 * Sends a message to a specific group chat.
	 * This method is used when there are multiple group chats
	 * the client is connected to and we want to send a message 
	 * only to a specific one of them.
	 * 
	 * @param chatroom
	 * @param message
	 */
	public void sendMUCMessage(String chatroom, String message) {
		if (connection==null || !connection.isAuthenticated() ) {
			System.err.println("Impossible to send message to groupchat: we have been disconnected! Terminating");
			System.exit(-1);
		}
		if ( !joinedSpecificRoom(chatroom) ) {
			System.err.println("Impossible to send message to groupchat: " + chatroom + ". "
					+ "Make sure the groupchat you are trying to send the message to exists and you joined it.");
		}
		Message m = new Message(chatroom, Message.Type.groupchat);
		m.setBody(message);
		connection.sendPacket(m);
	}


	/**
	 * Returns the username without the <code>@hostname/resource</code>.
	 * 
	 * @return
	 */
	public String getUsername() {
		return connection.getUser().split("@")[0];
	}


	/**
	 * Disconnects the client.
	 */
	public void disconnect() {
		for (MultiUserChat cr: chatRooms)	
			if (cr!=null && cr.isJoined()) {
				cr.leave();
			}
		chatRooms = null;
		if (connection.isAuthenticated()) {
			connection.disconnect();
			connection = null;
		}
	}


	/**
	 * Splits a JID in the form username@domain into the two components.
	 * If the user name can't be properly parsed, an exception is generated.
	 * 
	 * @param fullJID the JID that needs to be parsed
	 * @return an array with two elements containing the the username and domain
	 * @throws XMPPException 
	 */
	public static String[] splitUserAndHost(String fullJID) throws XMPPException {
		if (fullJID==null || fullJID.isEmpty())
			throw new XMPPException(fullJID + " is not a valid JID");
		String[] sa = fullJID.split("@", 2);
		if (sa.length!=2 || sa[0]==null || sa[1]==null || sa[0].isEmpty() || sa[1].isEmpty() ) 
			throw new XMPPException(fullJID + " is not a valid JID");
		return sa;
	}
	
	
	private boolean onlyOneRoomJoined() {
		if (chatRooms.size()==1 && chatRooms.get(0).isJoined())
			return true;
		return false;
	}
	
	
	private boolean joinedSpecificRoom(String chatroom) {
		if ( chatroom!=null && chatRoomExists(chatroom) && chatRoomIsJoined(chatroom) )
			return true;
		return false;
	}


	private boolean chatRoomIsJoined(String chatroom) {
		for (MultiUserChat muc : chatRooms) 
			if (muc.getRoom().equals(chatroom) && muc.isJoined())
				return true;
		return false;
	}


	private boolean chatRoomExists(String chatroom) {
		for (MultiUserChat muc : chatRooms) 
			if (muc.getRoom().equals(chatroom))
				return true;
		return false;
		
	}

}
