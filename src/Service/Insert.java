package Service;

import Common.Constant;
import Common.Error;
import Common.PermissionControl;
import Common.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangquanxiu at 2018/5/27 20:25
 */
public class Insert {
    public static void handleSql(String arrs[]){
        String sql = Util.arrayToString(arrs);
        if(Constant.currentDatabase == null){
            Util.showInTextArea(sql, Error.ACCESS_DENIED);
            return;
        }
        if(!PermissionControl.checkInsertPermission()) {
            Util.showInTextArea(sql, Error.ACCESS_DENIED);
            return;
        }
        /**
         * 语句匹配
         */
        Pattern pattern = Pattern.compile("insert into (.{1,}) values ?\\((.{1,})\\)");
        Matcher m = pattern.matcher(sql);
        if(m.matches()){

        }


    }
}
