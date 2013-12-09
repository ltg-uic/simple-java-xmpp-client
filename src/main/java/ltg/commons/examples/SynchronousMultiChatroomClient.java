package ltg.commons.examples;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.Message;

import ltg.commons.SimpleXMPPClient;

/**
 * This example demonstrates the use of SimpleXMPPClient class.
 * The class is used to synchronously wait for packets and then process them,
 * a typical scenario when designing simple agents.
 * 
 * @author tebemis
 *
 */
public class SynchronousMultiChatroomClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> chatrooms = new ArrayList<String>();
		chatrooms.add("test-room-1@conference.ltg.evl.uic.edu");
		chatrooms.add("test-room-2@conference.ltg.evl.uic.edu");
		SimpleXMPPClient sc = new SimpleXMPPClient("test-bot@ltg.evl.uic.edu", "test-bot", chatrooms);
		
		// We are now connected and in the group chat room. If we don't do something
		// the main will terminate... 
		
		// ... so let's go ahead and wait for a message to arrive...
		while (!Thread.currentThread().isInterrupted()) {
			// ... and process it ...
			Message m = sc.nextMessage();
			System.out.println(m.getFrom() + " " + m.getBody());
		}
		// ... and finally disconnect.
		sc.disconnect();
	}

}
