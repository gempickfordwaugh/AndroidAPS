package info.nightscout.androidaps.realmDb;

import java.util.Date;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.interfaces.Interval;
import info.nightscout.utils.DateUtil;
import info.nightscout.utils.DecimalFormatter;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mike on 17.05.2017.
 */

public class TemporaryTarget extends RealmObject implements Interval {
    @PrimaryKey
    @Index
    public long startDate = 0;

    @Index
    public boolean isValid = true;

    public int source = Source.NONE;
    public String _id = null; // NS _id

    public int durationInMinutes = 0; // duration == 0 means end of temp basal
    public double low; // in mgdl
    public double high; // in mgdl
    public String reason;

    // -------- Interval interface ---------

    @Ignore
    Long cuttedEnd = null;

    public long durationInMsec() {
        return durationInMinutes * 60 * 1000L;
    }

    public long start() {
        return startDate;
    }

    // planned end time at time of creation
    public long originalEnd() {
        return startDate + durationInMinutes * 60 * 1000L;
    }

    // end time after cut
    public long end() {
        if (cuttedEnd != null)
            return cuttedEnd;
        return originalEnd();
    }

    public void cutEndTo(long end) {
        cuttedEnd = end;
    }

    public boolean match(long time) {
        if (start() <= time && end() >= time)
            return true;
        return false;
    }

    public boolean before(long time) {
        if (end() < time)
            return true;
        return false;
    }

    public boolean after(long time) {
        if (start() > time)
            return true;
        return false;
    }

    // -------- Interval interface end ---------

    public String lowValueToUnitsToString(String units) {
        if (units.equals(Constants.MGDL)) return DecimalFormatter.to0Decimal(low);
        else return DecimalFormatter.to1Decimal(low * Constants.MGDL_TO_MMOLL);
    }

    public String highValueToUnitsToString(String units) {
        if (units.equals(Constants.MGDL)) return DecimalFormatter.to0Decimal(high);
        else return DecimalFormatter.to1Decimal(low * Constants.MGDL_TO_MMOLL);
    }

    public boolean isInProgress() {
        return match(new Date().getTime());
    }

    public String log() {
        return "TemporaryTarget{" +
                "startDate=" + startDate +
                "startDate=" + DateUtil.dateAndTimeString(startDate) +
                ", isValid=" + isValid +
                ", duration=" + durationInMinutes +
                ", reason=" + reason +
                ", low=" + low +
                ", high=" + high +
                '}';
    }

}
