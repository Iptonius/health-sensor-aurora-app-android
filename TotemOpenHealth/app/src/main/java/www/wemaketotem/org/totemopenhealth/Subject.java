package www.wemaketotem.org.totemopenhealth;

/**
 * Subject class for the observer.
 */
public interface Subject {
    /**
     * Registers the observer.
     * @param observer observer to start listening to the subject.
     */
    void registerObserver(Observer observer);

    /**
     * Should be called when a connection with a device has been established.
     */
    void notifyConnected();

    /**
     * Should be called when device has been disconnected.
     */
    void notifyDisconnected();
}
