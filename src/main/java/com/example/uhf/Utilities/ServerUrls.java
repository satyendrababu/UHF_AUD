package com.example.uhf.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import io.paperdb.Paper;

public class ServerUrls {


    Common common = new Common();


    //private static String ContextUrl = "http://192.168.0.197:8080/ats/";
    //private static String ContextUrl = "http://45.117.152.206:8080/ats/";
    public String ContextUrl = "http://"+common.readFile()+"/ats/";


    public String URL_APP_CONNECTION = ContextUrl+"AppConnection";
    public String URL_LOGIN = ContextUrl+"Login";
    public String URL_ALL_TAG_LIST = ContextUrl+"allTaggingMapList";
    public String URL_AUDIT_LOCATION = ContextUrl+"getAuditLocation";
    public String URL_ASSET_SUBITEM = ContextUrl+"AssetSubItemList";
    public String URL_UPDATE_INV_LOCATION = ContextUrl+"PostUpdateServlet";
    public String URL_AUDITABLE_EQPT = ContextUrl+"AuditableEqpt";
    public String URL_MISSING_EQPT = ContextUrl+"MissingEqpt";
    public String URL_MISLOCATED_EQPT = ContextUrl+"MislocatedEqpt";
    public String URL_PHY_UPDATE_EQPT = ContextUrl+"physicallyUpdateEQPT";
    public String URL_AUDITABLE_SUB_ITEM = ContextUrl+"getAuditableTagList";
    public String URL_AUDITED_EQPT = ContextUrl+"GetAuditableEqpt";
    public String URL_ALL_TAGGED_ASSET = ContextUrl+"TaggedAsset";
    public String URL_DELETE_TAGGED_ASSET = ContextUrl+"DeleteTaggedAsset";
    public String URL_IMAGE_BY_ASSET_ID = ContextUrl+"ImageAction.do?action=showPainingSmallImage&asset_id=";
    public String URL_VALIDATE_RFID_TAG = ContextUrl+"ValidateRFIDTag";
    public String URL_ALL_ASSET_LOCATION = ContextUrl+"getAuditLocation";
    public String URL_ALL_UNTAGGED_ASSET_BY_LOCATION = ContextUrl+"AllUnTaggedAssetByAssetLocation";
    public String URL_ASSET_DETAILS_BY_ASSET_ID = ContextUrl+"AssetDetailsByAssetId";
    public String URL_TAG_NEW_ASSET = ContextUrl+"TagAsset";



    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String name = "";
        name = preferences.getString(key, null);
        Log.d("IPAddress", "IPAddress"+name);
        String ContextUrl1 = "http://";
        String ContextUrl2 = "/Inventory/";
        //ContextUrl = ContextUrl1+name+ContextUrl2;
        return name;
    }
}
