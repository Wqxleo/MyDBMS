package Service;

import Common.Constant;
import Common.Error;
import Common.PermissionControl;
import Common.Util;
import Controller.TableController;
import org.json.JSONException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangquanxiu at 2018/5/27 20:25
 */
public class Insert {
    public static void handleSql(String arrs[]) throws JSONException {
        String sql = Util.arrayToString(arrs);
        if(Constant.currentDatabase == null){
            Util.showInTextArea(sql, Error.ACCESS_DENIED);
            return;
        }
        if(!PermissionControl.checkInsertPermission()) {
            Util.showInTextArea(sql, Error.ACCESS_DENIED);
            return;
        }
        if(!Constant.currentDatabase.getJSONObject("table").has(arrs[2])){
            Util.showInTextArea(sql,Error.TABLE_NOT_EXIST);
            return;
        }
        else {
            TableController.insertTable(arrs);
        }


    }
}
