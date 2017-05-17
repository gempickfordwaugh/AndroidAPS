package info.nightscout.androidaps.realmDb;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mike on 17.05.2017.
 */

public class DatabaseRequest extends RealmObject {
    @PrimaryKey
    @Index
    public String nsClientID = null;

    @Index
    public String _id = null;
    public String action = null;
    public String collection = null;
    public String data = null;

    public DatabaseRequest() {
    }

    // dbAdd
    public DatabaseRequest(String action, String collection, String nsClientID, JSONObject data) {
        this.action = action;
        this.collection = collection;
        this.data = data.toString();
        this.nsClientID = nsClientID;
        this._id = "";
    }

    // dbUpdate, dbUpdateUnset
    public DatabaseRequest(String action, String collection, String nsClientID, String _id, JSONObject data) {
        this.action = action;
        this.collection = collection;
        this.data = data.toString();
        this.nsClientID = nsClientID;
        this._id = _id;
    }

    // dbRemove
    public DatabaseRequest(String action, String collection, String nsClientID, String _id) {
        this.action = action;
        this.collection = collection;
        this.data = new JSONObject().toString();
        this.nsClientID = nsClientID;
        this._id = _id;
    }

    public String hash() {
        return Hashing.sha1().hashString(action + collection + _id + data.toString(), Charsets.UTF_8).toString();
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        try {
            object.put("action", action);
            object.put("collection", collection);
            object.put("data", new JSONObject(data));
            if (_id != null) object.put("_id", _id);
            if (nsClientID != null) object.put("nsClientID", nsClientID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static DatabaseRequest fromJSON(JSONObject jsonObject) {
        DatabaseRequest result = new DatabaseRequest();
        try {
            if (jsonObject.has("action"))
                result.action = jsonObject.getString("action");
            if (jsonObject.has("collection"))
                result.collection = jsonObject.getString("collection");
            if (jsonObject.has("data"))
                result.data = jsonObject.getJSONObject("data").toString();
            if (jsonObject.has("_id"))
                result._id = jsonObject.getString("_id");
            if (jsonObject.has("nsClientID"))
                result.nsClientID = jsonObject.getString("nsClientID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
