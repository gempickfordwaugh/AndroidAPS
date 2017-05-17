package info.nightscout.androidaps.interfaces;

import java.util.Date;

import info.nightscout.androidaps.data.Iob;
import info.nightscout.androidaps.db.Treatment;
import info.nightscout.androidaps.realmDb.Bolus;

/**
 * Created by mike on 17.04.2017.
 */

public interface InsulinInterface {
    int FASTACTINGINSULIN = 0;
    int FASTACTINGINSULINPROLONGED = 1;

    int getId();
    String getFriendlyName();
    String getComment();
    double getDia();
    Iob iobCalcForTreatment(Treatment treatment, long time, Double dia);
    Iob iobCalcForTreatment(Bolus bolus, long time, Double dia);
}
