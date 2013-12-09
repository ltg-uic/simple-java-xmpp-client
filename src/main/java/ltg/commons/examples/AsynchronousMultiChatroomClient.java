package ltg.commons.examples;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.Message;

import ltg.commons.MessageListener;
import ltg.commons.SimpleXMPPClient;

/**
 * This example demonstrates the use of SimpleXMPPClient class.
 * The class is used to asynchronously process packets.
 * This is useful whenever the main thread is already busy doing 
 * stuff and we want to interrupt.
 * 
 * @author tebemis
 *
 */
public class AsynchronousMultiChatroomClient {

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
		
		// ... so let's go ahead and make ourself busy... but before we do that... 
		 
		// ... let's register the packet handler with our XMPP client...
		sc.registerEventListener(new MessageListener() {
			
			@Override
			public void processMessage(Message m) {
				System.out.println(m.getFrom() + " " + m.getBody());
			}
		});
		
		// ... and now we can make ourselves busy
		int i = 0;
		while(true) {
			try {
				sc.sendMUCMessage("test-room-1@conference.ltg.evl.uic.edu", "Message " + i);
				Thread.sleep(5000);
				i++;
				sc.sendMUCMessage("test-room-2@conference.ltg.evl.uic.edu", "Message " + i);
				Thread.sleep(5000);
				i++;
			} catch (InterruptedException e) {
				sc.disconnect();
			}
		}
		
		// NOTE: unless the main thread is suspended we will never get to this point 
	}

}
