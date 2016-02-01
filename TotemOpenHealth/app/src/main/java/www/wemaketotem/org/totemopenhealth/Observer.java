package www.wemaketotem.org.totemopenhealth;

/**
 * Observer interface for a device connected.
 */
public interface Observer {
    /**
     * Called when a connection between a device has been established.
     */
    void deviceConnected();

    /**
     * Called when the connected device has been disconnected.
     */
    void deviceDisconnected();
}
