package io.github.davepkennedy;

/**
 * Provides an interface for listening for expiring events
 */
public interface ExpiringCounter extends Counter {
    /**
     * Adds a new handler to the listeners
     * @param handler An object which will respond to the expiry event
     */
    void expiryHandler(ExpiryHandler handler);
}
