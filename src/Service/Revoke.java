package Service;

import Common.*;
import Common.Error;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangquanxiu at 2018/5/27 20:26
 */
public class Revoke {

    public static String sql = null;
    public static String[] arr = null;
    public static String permission = null;
    public static String database = null;
    public static String user = null;

    public static void handleSql(String arrs[]) {
        sql = Util.arrayToString(arrs);
        arr = arrs;

        if(!checkRevokeGram()) {
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }

        if(!PermissionControl.checkChangeStructurePermission()) {
            Util.showInTextArea(sql, Error.ACCESS_DENIED);
            return;
        }

        if(!Constant.DICTIONARY.has(database)) {
            Util.showInTextArea(sql, Error.DATABASE_NOT_EXIST);
            return;
        }

        if(!Constant.USERS.has(user)) {
            Util.showInTextArea(sql, Error.USER_NOT_EXIST);
            return;
        }

        try {
        JSONObject databaseJSONObject = Constant.DICTIONARY.getJSONObject(database);
        String select = databaseJSONObject.getJSONObject(user).getString("select");
        String insert = databaseJSONObject.getJSONObject(user).getString("insert");
        String delete = databaseJSONObject.getJSONObject(user).getString("delete");
        String update = databaseJSONObject.getJSONObject(user).getString("update");
        switch(permission) {
            case "select" :
                select = "0";
                break;
            case "insert" :
                insert = "0";
                break;
            case "delete" :
                delete = "0";
                break;
            case "update" :
                update = "0";
                break;
            default :
                Util.showInTextArea(sql, Error.COMMAND_ERROR);
                return;
        }

            JSONObject temp = new JSONObject();
            temp.put("select", select);
            temp.put("insert", insert);
            temp.put("delete", delete);
            temp.put("update", update);
            databaseJSONObject.remove(user);
            databaseJSONObject.put(user, temp);
            Constant.DICTIONARY.remove(database);
            Constant.DICTIONARY.put(database, databaseJSONObject);
            Util.writeData(Constant.PATH_DICTIONARY, Constant.DICTIONARY.toString());
            Constant.selectPermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("select");
            Constant.insertPermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("insert");
            Constant.deletePermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("delete");
            Constant.updatePermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("update");
            Util.showInTextArea(sql, Prompt.REVOKE_SUCCESS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static boolean checkRevokeGram() {
        Pattern pattern = Pattern.compile("revoke (.{1,}) on (.{1,}) from (.{1,})");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.matches()) {
            permission = matcher.group(1).trim();
            database = matcher.group(2).trim();
            user = matcher.group(3).trim();
            return true;
        } else {
            return false;
        }
    }
}
