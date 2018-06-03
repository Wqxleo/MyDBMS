package Controller;

import Common.Constant;
import Common.Error;
import Common.PermissionControl;
import Common.Util;

/**
 * Created by wangquanxiu at 2018/5/25 20:35
 */
public class UserController {
    public static void createdUser(String arrs[]){
        String sql;
        sql = Util.arrayToString(arrs);
        //检查语句是否合法
        if(!sql.matches("create user [a-z_][a-z0-9_]{0,99} \\w{1,100} ([a-z_]+)")){
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
        }
        //检查用户权限，只有root用户才能新建用户
        if(!PermissionControl.checkChangeStructurePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
        }
        //检查当前用户字典中是否已经有此用户名
        if(Constant.USERS.has(arrs[2])){
            Util.showInTextArea(sql,Error.USER_EXIST);
        }





    }
    public static void deleteUser(String arrs[]){

    }

}
