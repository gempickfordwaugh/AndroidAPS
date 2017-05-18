package info.nightscout.androidaps.realmDb;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by mike on 17.05.2017.
 */

public class RealmDBHelper {

    static public void resetDatabases() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    static public void delete(Class clazz) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(clazz);
        realm.commitTransaction();
    }

    // ----------- Bg ------------

    static public List<Bg> getBgDataFromTime(long mills, boolean ascending) {
        try {
            Realm realm = Realm.getDefaultInstance();
            return realm.where(Bg.class).greaterThanOrEqualTo("date", mills).findAllSorted("date", ascending ? Sort.ASCENDING : Sort.DESCENDING);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Nullable
    static public Bg getLastBg() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Bg>  realmResults = realm.where(Bg.class).findAllSorted("date");
        if (realmResults.size() > 0)
            return realmResults.last();
        return null;
    }

    static public void add(Bg bg) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(bg);
        realm.commitTransaction();
    }

    static public void addOrUpdate(Bg bg) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(bg);
        realm.commitTransaction();
    }

    static public void update(Bg bg) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(bg);
        realm.commitTransaction();
    }

    static public void delete(Bg bg) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(Bg.class).equalTo("date", bg.date).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    // ----------- DatabaseRequest ------------

    static public List<DatabaseRequest> getDatabaseRequests() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(DatabaseRequest.class).findAll();
    }

    static public long getDatabaseRequestsCount() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(DatabaseRequest.class).count();
    }

    static public void add(DatabaseRequest dbr) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(dbr);
        realm.commitTransaction();
    }

    static public void delete(DatabaseRequest dbr) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(DatabaseRequest.class).equalTo("nsClientID", dbr.nsClientID).findFirst().deleteFromRealm();
        realm.commitTransaction();
    }

    static public long deleteDatabaseRequest(String nsClientID) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<DatabaseRequest> all = realm.where(DatabaseRequest.class).findAll();
        RealmResults<DatabaseRequest> realmResults = realm.where(DatabaseRequest.class).equalTo("nsClientID", nsClientID).findAll();
        realmResults.deleteAllFromRealm();
        realm.commitTransaction();
        return realmResults.size();
    }

    static public long deleteDatabaseRequest(String action, String _id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<DatabaseRequest> realmResults = realm.where(DatabaseRequest.class).equalTo("_id", _id).equalTo("action", action).findAll();
        realmResults.deleteAllFromRealm();
        realm.commitTransaction();
        return realmResults.size();
    }

}
