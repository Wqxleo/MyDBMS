package Controller;

import Common.*;
import Common.Error;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangquanxiu at 2018/5/25 20:35
 */
public class UserController {
    public static void createdUser(String arrs[]){
        String sql;
        sql = Util.arrayToString(arrs);
        //检查语句是否合法
        if(!sql.matches("create user [a-z_][a-z0-9_]{0,99} \\w{1,100} ([a-z_]+)") || arrs.length != 5){
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }
        //检查用户权限，只有root用户才能新建用户
        if(!PermissionControl.checkChangeStructurePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }
        //检查当前用户字典中是否已经有此用户名
        if(Constant.USERS.has(arrs[2])){
            Util.showInTextArea(sql,Error.USER_EXIST);
            return;
        }

            try {
                JSONObject user = new JSONObject();
                user.put("password",arrs[3]);
                if(arrs[4].equals("root_user") || arrs[4].equals("common_user")){
                    user.put("type",arrs[4]);
                }
                else {
                    Util.showInTextArea(sql,Error.COMMAND_ERROR);
                    return;
                }
                Constant.USERS.put(arrs[2],user);
                Util.writeData(Constant.PATH_USERS,Constant.USERS.toString());
                Util.showInTextArea(sql, Prompt.CREATE_USER_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }







    }
    public static void deleteUser(String arrs[]){

    }

}
