package frc.time.sync;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;

public class NtpSyncher {

    private final NetworkTableEntry mClientEntry;
    private final NetworkTableEntry mServerRecTimeEntry;
    private final NetworkTableEntry mServerSendTimeEntry;
    private final NtpClock mClock;

    private long mClientStartTimestamp;
    private long mClientEndTimestamp;
    private long mServerStartTimestamp;
    private long mServerEndTimestamp;

    public NtpSyncher(NetworkTableEntry clientEntry, NetworkTableEntry serverRecTimeEntry, NetworkTableEntry serverSendTimeEntry, NtpClock clock) {
        mClientEntry = clientEntry;
        mServerRecTimeEntry = serverRecTimeEntry;
        mServerSendTimeEntry = serverSendTimeEntry;
        mClock = clock;

        mClientEntry.addListener(this::onClientRequest, EntryListenerFlags.kUpdate);

        mClientStartTimestamp = mClock.currentTimeMillis();
        mClientEndTimestamp = 0;
        mServerStartTimestamp = 0;
        mServerEndTimestamp = 0;
    }

    public void startSync() {
        mClientEntry.setBoolean(true);
    }

    private void onClientRequest(EntryNotification notification) {
        if (notification.value.getBoolean()) {
            return;
        }

        mClientEndTimestamp = mClock.currentTimeMillis();
        mServerStartTimestamp = (long) mServerRecTimeEntry.getDouble(-1);
        mServerEndTimestamp = (long) mServerSendTimeEntry.getDouble(-1);

        long offset = ((mServerStartTimestamp - mClientStartTimestamp) + (mServerEndTimestamp - mClientEndTimestamp)) / 2;
        mClock.updateOffset(offset);
    }
}
