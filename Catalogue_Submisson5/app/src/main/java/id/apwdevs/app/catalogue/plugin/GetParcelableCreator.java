package id.apwdevs.app.catalogue.plugin;

import android.os.Parcelable;
import id.apwdevs.app.catalogue.manager.PendingAlarmRunJob;

import static id.apwdevs.app.catalogue.manager.PendingAlarmRunJob.CREATOR;

public class GetParcelableCreator {
    @SuppressWarnings("unchecked")
    public static Parcelable.Creator<PendingAlarmRunJob> getPendingCreator() {
        return CREATOR;
    }
}
