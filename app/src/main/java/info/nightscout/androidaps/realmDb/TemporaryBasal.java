package info.nightscout.androidaps.realmDb;

import java.util.Date;

import info.nightscout.androidaps.data.Iob;
import info.nightscout.androidaps.data.IobTotal;
import info.nightscout.androidaps.interfaces.InsulinInterface;
import info.nightscout.androidaps.interfaces.Interval;
import info.nightscout.androidaps.plugins.ConfigBuilder.ConfigBuilderPlugin;
import info.nightscout.androidaps.plugins.NSClientInternal.data.NSProfile;
import info.nightscout.utils.DateUtil;
import info.nightscout.utils.DecimalFormatter;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mike on 16.05.2017.
 */

public class TemporaryBasal extends RealmObject implements Interval {

    @PrimaryKey
    @Index
    long startDate = 0;

    @Index
    boolean isValid = true;

    int source = Source.NONE;
    long _id = 0; // NS _id

    int durationInMinutes = 0; // duration == 0 means end of temp basal
    boolean isAbsolute = false;
    int percentRate = 0;
    double absoluteRate = 0d;


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

    public IobTotal iobCalc(long time) {
        IobTotal result = new IobTotal(time);
        NSProfile profile = ConfigBuilderPlugin.getActiveProfile().getProfile();
        InsulinInterface insulinInterface = ConfigBuilderPlugin.getActiveInsulin();

        if (profile == null)
            return result;

        int realDuration = getDurationToTime(time);
        Double netBasalAmount = 0d;

        if (realDuration > 0) {
            Double netBasalRate = 0d;

            Double dia_ago = time - profile.getDia() * 60 * 60 * 1000;
            int aboutFiveMinIntervals = (int) Math.ceil(realDuration / 5d);
            double tempBolusSpacing = realDuration / aboutFiveMinIntervals;

            for (Long j = 0L; j < aboutFiveMinIntervals; j++) {
                // find middle of the interval
                Long date = (long) (startDate + j * tempBolusSpacing * 60 * 1000 + 0.5d * tempBolusSpacing * 60 * 1000);

                Double basalRate = profile.getBasal(NSProfile.secondsFromMidnight(date));

                if (basalRate == null)
                    continue;
                if (isAbsolute) {
                    netBasalRate = absoluteRate - basalRate;
                } else {
                    netBasalRate = (percentRate - 100) / 100d * basalRate;
                }

                if (date > dia_ago && date <= time) {
                    double tempBolusSize = netBasalRate * tempBolusSpacing / 60d;
                    netBasalAmount += tempBolusSize;

                    Bolus tempBolusPart = new Bolus(insulinInterface);
                    tempBolusPart.insulinAmount = tempBolusSize;
                    tempBolusPart.date = date;

                    Iob aIOB = insulinInterface.iobCalcForTreatment(tempBolusPart, time, profile.getDia());
                    result.basaliob += aIOB.iobContrib;
                    result.activity += aIOB.activityContrib;
                    result.netbasalinsulin += tempBolusPart.insulinAmount;
                    if (tempBolusPart.insulinAmount > 0) {
                        result.hightempinsulin += tempBolusPart.insulinAmount;
                    }
                }
                result.netRatio = netBasalRate; // ratio at the end of interval
            }
        }
        result.netInsulin = netBasalAmount;
        return result;
    }

    public int getRealDuration() {
        return getDurationToTime(new Date().getTime());
    }

    private int getDurationToTime(long time) {
        long endTime = Math.min(time, end());
        long msecs = endTime - startDate;
        return Math.round(msecs / 60f / 1000);
    }

    public int getPlannedRemainingMinutes() {
        float remainingMin = (end() - new Date().getTime()) / 1000f / 60;
        return (remainingMin < 0) ? 0 : Math.round(remainingMin);
    }

    public boolean isInProgress() {
        return match(new Date().getTime());
    }

    public double tempBasalConvertedToAbsolute(Date time) {
        if (isAbsolute) return absoluteRate;
        else {
            NSProfile profile = ConfigBuilderPlugin.getActiveProfile().getProfile();
            return profile.getBasal(NSProfile.secondsFromMidnight(time)) * percentRate / 100;
        }
    }

    public String log() {
        return "TemporaryBasal{" +
                "startDate=" + startDate +
                ", startDate=" + DateUtil.dateAndTimeString(startDate) +
                ", isValid=" + isValid +
                ", _id=" + _id +
                ", percentRate=" + percentRate +
                ", absoluteRate=" + absoluteRate +
                ", durationInMinutes=" + durationInMinutes +
                ", isAbsolute=" + isAbsolute +
                '}';
    }

    public String toString() {
        if (isAbsolute) {
            return DecimalFormatter.to2Decimal(absoluteRate) + "U/h @" +
                    DateUtil.timeString(startDate) +
                    " " + getRealDuration() + "/" + durationInMinutes + "min";
        } else { // percent
            return percentRate + "% @" +
                    DateUtil.timeString(startDate) +
                    " " + getRealDuration() + "/" + durationInMinutes + "min";
        }
    }

    public String toStringShort() {
        if (isAbsolute) {
            return DecimalFormatter.to2Decimal(absoluteRate) + "U/h ";
        } else { // percent
            return percentRate + "% ";
        }
    }

    public String toStringMedium() {
        if (isAbsolute) {
            return DecimalFormatter.to2Decimal(absoluteRate) + "U/h ("
                    + getRealDuration() + "/" + durationInMinutes + ") ";
        } else { // percent
            return percentRate + "% (" + getRealDuration() + "/" + durationInMinutes + ") ";
        }
    }


}
