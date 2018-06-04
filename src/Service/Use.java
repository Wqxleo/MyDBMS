package Service;

import Common.*;
import Common.Error;
import org.json.JSONException;

/**
 * Created by wangquanxiu at 2018/5/27 20:27
 */
public class Use {
    public static void handleSql(String arrs[]){
        String sql = Util.arrayToString(arrs);
        if(arrs.length != 2){
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }
        if(!Constant.DICTIONARY.has(arrs[1])){
            Util.showInTextArea(sql, Error.DATABASE_NOT_EXIST);
            return;
        }
        if(!PermissionControl.checkChangeStructurePermission()) {
            Util.showInTextArea(sql, Error.ACCESS_DENIED);
        }
        try {
            Constant.currentDatabase = Constant.DICTIONARY.getJSONObject(arrs[1]);
            Constant.currentDatabaseName = arrs[1];
            Constant.selectPermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("select");
            Constant.insertPermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("insert");
            Constant.deletePermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("delete");
            Constant.updatePermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("update");
            Util.showInTextArea(sql, Prompt.USE_DATABASE_SUCCESS);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
