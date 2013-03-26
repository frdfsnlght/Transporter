package com.frdfsnlght.transporter.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.frdfsnlght.transporter.Server;

public class APIMessageReceivedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private String message;
    private Server remoteServer;
    private boolean cancelled;
    
    public APIMessageReceivedEvent(Server remoteServer, String message) {
    	this.remoteServer = remoteServer;
    	this.message = message;
    }
    
    /**
     * Returns the list of event handlers for this event.
     *
     * @return the list of event handlers for this event
     */
    @Override
	public HandlerList getHandlers() {
        return handlers;
	}
    
    /**
     * Returns the list of event handlers for this event.
     *
     * @return the list of event handlers for this event
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
	public String getMessage() {
		return message;
	}

	public Server getRemoteServer() {
		return remoteServer;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
