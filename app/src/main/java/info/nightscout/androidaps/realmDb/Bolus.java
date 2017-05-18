package info.nightscout.androidaps.realmDb;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.data.Iob;
import info.nightscout.androidaps.interfaces.InsulinInterface;
import info.nightscout.androidaps.plugins.ConfigBuilder.ConfigBuilderPlugin;
import info.nightscout.androidaps.plugins.NSClientInternal.data.NSProfile;
import info.nightscout.androidaps.plugins.Overview.graphExtensions.DataPointWithLabelInterface;
import info.nightscout.utils.DateUtil;
import info.nightscout.utils.DecimalFormatter;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mike on 16.05.2017.
 */

public class Bolus extends RealmObject implements DataPointWithLabelInterface {

    @PrimaryKey
    @Index
    public long date = 0;

    @Index
    public boolean isValid = true;

    public int source = Source.NONE;
    public String _id = null; // NS _id

    public double insulinAmount = 0d;
    public double carbsAmount = 0d;
    public boolean mealBolus = false;

    public int insulinInterfaceID = InsulinInterface.FASTACTINGINSULIN;
    public double dia = Constants.defaultDIA;

    public Bolus() {
        super();
    }

    public Bolus(InsulinInterface insulinInterface) {
        super();
        insulinInterfaceID = insulinInterface.getId();
    }

    public String log() {
        return "Bolus{" +
                "date= " + date +
                ", date= " + DateUtil.dateAndTimeString(date) +
                ", isValid=" + isValid +
                ", _id= " + _id +
                ", insulinAmount= " + insulinAmount +
                ", carbsAmount= " + carbsAmount +
                ", mealBolus= " + mealBolus +
                "}";
    }

    //  ----------------- DataPointInterface --------------------
    @Override
    public double getX() {
        return date;
    }

    // default when no sgv around available
    @Ignore
    private double yValue = 0;

    @Override
    public double getY() {
        return yValue;
    }

    @Override
    public String getLabel() {
        String label = "";
        if (insulinAmount > 0) label += DecimalFormatter.to2Decimal(insulinAmount) + "U";
        if (carbsAmount > 0)
            label += (label.equals("") ? "" : " ") + DecimalFormatter.to0Decimal(carbsAmount) + "g";
        return label;
    }

    public void setYValue(List<Bg> bgReadingsArray) {
        NSProfile profile = MainApp.getConfigBuilder().getActiveProfile().getProfile();
        if (profile == null) return;
        for (int r = bgReadingsArray.size() - 1; r >= 0; r--) {
            Bg reading = bgReadingsArray.get(r);
            if (reading.date > date) continue;
            yValue = NSProfile.fromMgdlToUnits(reading.value, profile.getUnits());
            break;
        }
    }

    //  ----------------- DataPointInterface end --------------------

    public void sendToNSClient() {
        JSONObject data = new JSONObject();
        try {
            if (mealBolus)
                data.put("eventType", "Meal Bolus");
            else
                data.put("eventType", "Correction Bolus");
            if (insulinAmount != 0d) data.put("insulin", insulinAmount);
            if (carbsAmount != 0d) data.put("carbs", new Double(carbsAmount).intValue());
            data.put("created_at", DateUtil.toISOString(date));
            data.put("timeIndex", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ConfigBuilderPlugin.uploadCareportalEntryToNS(data);
    }

    public Iob iobCalc(long time, double dia) {
        InsulinInterface insulinInterface = MainApp.getInsulinIterfaceById(insulinInterfaceID);
        if (insulinInterface == null)
            insulinInterface = ConfigBuilderPlugin.getActiveInsulin();

        return insulinInterface.iobCalcForTreatment(this, time, dia);
    }
}
