package info.nightscout.androidaps.realmDb;

import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by mike on 17.05.2017.
 */

public class RealmDBHelper {
    static Realm realm = Realm.getDefaultInstance();

    static public void resetDatabases() {
        realm.deleteAll();
    }

    static public void delete(Class clazz) {
        realm.delete(clazz);
    }

    // ----------- Bg ------------

    static public List<Bg> getBgDataFromTime(long mills, boolean ascending) {
        return realm.where(Bg.class).greaterThanOrEqualTo("date", mills).findAllSorted("date", ascending ? Sort.ASCENDING : Sort.DESCENDING);
    }

    static public void add(Bg bg) {
        realm.beginTransaction();
        realm.copyToRealm(bg);
        realm.commitTransaction();
    }

    static public void update(Bg bg) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(bg);
        realm.commitTransaction();
    }

    static public void delete(Bg bg) {
        realm.beginTransaction();
        realm.where(Bg.class).equalTo("date", bg.date).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    // ----------- DatabaseRequest ------------

    public void add(DatabaseRequest dbr) {
        realm.beginTransaction();
        realm.copyToRealm(dbr);
        realm.commitTransaction();
    }

    public void delete(DatabaseRequest dbr) {
        realm.beginTransaction();
        realm.where(Bg.class).equalTo("date", dbr.date).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    public void deleteDatabaseRequest(String _id) {
        realm.beginTransaction();
        realm.where(Bg.class).equalTo("_id", _id).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    public void deleteDatabaseRequest(String action, String _id) {
        realm.beginTransaction();
        realm.where(Bg.class).equalTo("_id", _id).equalTo("action", action).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

}
