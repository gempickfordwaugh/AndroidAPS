package info.nightscout.androidaps.realmDb;

import java.util.Date;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.data.Iob;
import info.nightscout.androidaps.data.IobTotal;
import info.nightscout.androidaps.interfaces.InsulinInterface;
import info.nightscout.androidaps.plugins.ConfigBuilder.ConfigBuilderPlugin;
import info.nightscout.androidaps.plugins.NSClientInternal.data.NSProfile;
import info.nightscout.utils.DateUtil;
import info.nightscout.utils.DecimalFormatter;
import info.nightscout.utils.Round;
import io.realm.RealmObject;
import info.nightscout.androidaps.interfaces.Interval;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mike on 17.05.2017.
 */

public class ExtendedBolus extends RealmObject implements Interval {

    @PrimaryKey
    @Index
    public long startDate = 0;

    @Index
    public boolean isValid = true;

    public int source = Source.NONE;
    public String _id = null; // NS _id

    public double insulinAmount = 0d;
    public int durationInMinutes = 0; // duration == 0 means end of temp basal

    public int insulinInterfaceID = InsulinInterface.FASTACTINGINSULIN;
    public double dia = Constants.defaultDIA;

    public ExtendedBolus() {
        super();
    }

    public ExtendedBolus(InsulinInterface insulinInterface) {
        super();
        insulinInterfaceID = insulinInterface.getId();
    }

    public String log() {
        return "Bolus{" +
                "startDate= " + startDate +
                ", date= " + DateUtil.dateAndTimeString(startDate) +
                ", isValid=" + isValid +
                ", _id= " + _id +
                ", insulinAmount= " + insulinAmount +
                ", durationInMinutes= " + durationInMinutes +
                "}";
    }

    double absoluteRate() {
        return Round.roundTo(insulinAmount / durationInMinutes * 60, 0.01);
    }

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

        if (realDuration > 0) {
            Double dia_ago = time - profile.getDia() * 60 * 60 * 1000;
            int aboutFiveMinIntervals = (int) Math.ceil(realDuration / 5d);
            double spacing = realDuration / aboutFiveMinIntervals;

            for (Long j = 0L; j < aboutFiveMinIntervals; j++) {
                // find middle of the interval
                Long date = (long) (startDate + j * spacing * 60 * 1000 + 0.5d * spacing * 60 * 1000);

                if (date > dia_ago && date <= time) {
                    double tempBolusSize = absoluteRate() * spacing / 60d;

                    Bolus tempBolusPart = new Bolus(insulinInterface);
                    tempBolusPart.insulinAmount = tempBolusSize;
                    tempBolusPart.date = date;

                    Iob aIOB = insulinInterface.iobCalcForTreatment(tempBolusPart, time, profile.getDia());
                    result.iob += aIOB.iobContrib;
                    result.activity += aIOB.activityContrib;
                }
            }
        }
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

    public String toString() {
        return "E " + DecimalFormatter.to2Decimal(absoluteRate()) + "U/h @" +
                DateUtil.timeString(startDate) +
                " " + getRealDuration() + "/" + durationInMinutes + "min";
    }

    public String toStringShort() {
        return "E " + DecimalFormatter.to2Decimal(absoluteRate()) + "U/h ";
    }

    public String toStringMedium() {
        return "E " + DecimalFormatter.to2Decimal(absoluteRate()) + "U/h ("
                + getRealDuration() + "/" + durationInMinutes + ") ";
    }
}
