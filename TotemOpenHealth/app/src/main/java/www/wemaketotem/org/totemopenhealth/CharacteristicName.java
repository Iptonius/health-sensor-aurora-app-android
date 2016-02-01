package www.wemaketotem.org.totemopenhealth;

/**
 * Public enum for the available characteristics on the device.
 */
public enum CharacteristicName{
    WRITECHAR("WRITECHAR","6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    private final String mName, mUUID;

    CharacteristicName(final String name, final String uuid){
        mName = name;
        mUUID = uuid;
    }

    @Override
    public String toString() {
            return mName;
        }

    /**
     * Getter for the UUID address as String format.
     * @return retrieves UUID of the chosen characteristic
     */
    public String getUUID() {
        return mUUID;
    }
}




