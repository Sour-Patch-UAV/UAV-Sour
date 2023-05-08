package READERS;

// interface to allow objects of Serial type to be able to access some function onSerialResponse()
public interface SerialResponseInitialize {
    void onSetupCommunicationWithTeensy(boolean isTeensyResponded);
};