package info.nightscout.androidaps.plugins.NSClientInternal;

import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.plugins.NSClientInternal.services.NSClientService;
import info.nightscout.androidaps.realmDb.DatabaseRequest;
import info.nightscout.androidaps.realmDb.RealmDBHelper;

/**
 * Created by mike on 21.02.2016.
 */
public class UploadQueue {
    private static Logger log = LoggerFactory.getLogger(UploadQueue.class);

    public static String status() {
        return "QUEUE: " + size();
    }

    public static long size() {
        return RealmDBHelper.getDatabaseRequestsCount();
    }

    private static void startService() {
        if (NSClientService.handler == null) {
            Context context = MainApp.instance();
            context.startService(new Intent(context, NSClientService.class));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void add(final DatabaseRequest dbr) {
        startService();
        if (NSClientService.handler != null) {
            NSClientService.handler.post(new Runnable() {
                @Override
                public void run() {
                    log.debug("QUEUE adding: " + dbr.data);
                    RealmDBHelper.add(dbr);
                    NSClientInternalPlugin plugin = (NSClientInternalPlugin) MainApp.getSpecificPlugin(NSClientInternalPlugin.class);
                    if (plugin != null) {
                        plugin.resend("newdata");
                    }
                }
            });
        }
    }

    public static void clearQueue() {
        startService();
        if (NSClientService.handler != null) {
            NSClientService.handler.post(new Runnable() {
                @Override
                public void run() {
                    log.debug("QUEUE ClearQueue");
                    RealmDBHelper.delete(DatabaseRequest.class);
                    log.debug(status());
                }
            });
        }
    }

    public static void removeID(final JSONObject record) {
        startService();
        if (NSClientService.handler != null) {
            NSClientService.handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        String id;
                        if (record.has("NSCLIENT_ID")) {
                            id = record.getString("NSCLIENT_ID");
                        } else {
                            return;
                        }
                        if (RealmDBHelper.deleteDatabaseRequest(id) >= 1)
                            log.debug("Removed item from UploadQueue. " + UploadQueue.status());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void removeID(final String action, final String _id) {
        startService();
        if (NSClientService.handler != null) {
            NSClientService.handler.post(new Runnable() {
                @Override
                public void run() {
                    RealmDBHelper.deleteDatabaseRequest(action, _id);
                }
            });
        }
    }

    public String textList() {
        String result = "";
        List<DatabaseRequest> list = RealmDBHelper.getDatabaseRequests();
        for (DatabaseRequest dbr : list) {
            result += "<br>";
            result += dbr.action.toUpperCase() + " ";
            result += dbr.collection + ": ";
            result += dbr.data;
        }
        return result;
    }

}
