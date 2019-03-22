package frc.time.sync;

import edu.wpi.first.networktables.NetworkTableEntry;

public class NtpClient {

    private final NetworkTableEntry mClientEntry;
    private final NetworkTableEntry mServerRecTimeEntry;
    private final NetworkTableEntry mServerSendTimeEntry;
    private final NtpClock mClock;

    public NtpClient(NetworkTableEntry clientEntry, NetworkTableEntry serverRecTimeEntry, NetworkTableEntry serverSendTimeEntry, NtpClock clock) {
        mClientEntry = clientEntry;
        mServerRecTimeEntry = serverRecTimeEntry;
        mServerSendTimeEntry = serverSendTimeEntry;
        mClock = clock;
    }

    public void sync() {
        NtpSyncher syncher = new NtpSyncher(mClientEntry, mServerRecTimeEntry, mServerSendTimeEntry, mClock);
        syncher.startSync();
    }
}
