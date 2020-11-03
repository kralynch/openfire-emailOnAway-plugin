package com.tempstop;

import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.util.EmailService;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.io.File;
import java.io.*;

public class emailOnAway implements Plugin, PacketInterceptor {

    private static final Logger Log = LoggerFactory.getLogger(emailOnAway.class);
    
    private InterceptorManager interceptorManager;
    private UserManager userManager;
    private PresenceManager presenceManager;
    private EmailService emailService;
    
    public emailOnAway() {
        interceptorManager = InterceptorManager.getInstance();
	    emailService = EmailService.getInstance();
	    presenceManager = XMPPServer.getInstance().getPresenceManager();
	    userManager = XMPPServer.getInstance().getUserManager();
    }

    public void initializePlugin(PluginManager pManager, File pluginDirectory) {
        // register with interceptor manager
        interceptorManager.addInterceptor(this);
    }

    public void destroyPlugin() {
        // unregister with interceptor manager
        interceptorManager.removeInterceptor(this);
    }

    public void interceptPacket(Packet packet, Session session, boolean read,
            boolean processed) throws PacketRejectedException {
    
    if((!processed) && 
        (packet instanceof Message) && 
        (packet.getTo() != null)) 
    { 
        Message msg = (Message) packet;
        
        if(msg.getType() == Message.Type.chat) {
        try 
        {
            User userTo = userManager.getUser(packet.getTo().getNode());
            //if online strinf is not found in users presence
            if(presenceManager.getPresence(userTo) == null) 
            {
	            //if body is not empty
	            if((msg.getBody() != null)&&(msg.getBody().trim().length() > 0)) 
	            {
	                // The From email address
	                User userFrom = userManager.getUser(packet.getFrom().getNode());
	              
	                //only send if the user has an email specified
	                if( userTo.getEmail() != null && userTo.getEmail().length() > 0 )
	                {
	                	emailService.sendMessage(
	                			userTo.getName(), 				//To Name in email header
		                		userTo.getEmail(), 				//Destination email address
		                		userFrom.getUsername(), 		//From Display Name in email header
		                		emailService.getUsername(),		//From address in email header
		                		"Offline Message",				//Email Subject
		                		msg.getBody(), 					//Email Body
		                		null);							//Email Body Html
	                }
	            }
            }
        } catch (UserNotFoundException e) {
        }
        }
    }
    }
}
