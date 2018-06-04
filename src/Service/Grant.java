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
public class Grant {
    public static String sql = null;
    public static String[] arr = null;
    public static String permission = null;
    public static String database = null;
    public static String user = null;

    public static void handleSql(String arrs[]){
        sql = Util.arrayToString(arrs);
        arr = arrs;

        //判断语句是否合法
        if(!checkGrantGram()){
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }
        //判断用户的权限
        if(!PermissionControl.checkChangeStructurePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }

        //判断数据库是否存在
        if(!Constant.DICTIONARY.has(database)){
            Util.showInTextArea(sql,Error.DATABASE_NOT_EXIST);
            return;
        }

        //判断用户是否存在
        if(!Constant.USERS.has(user)){
            Util.showInTextArea(sql,Error.USER_NOT_EXIST);
            return;
        }

        try {
            //获取当前的数据库JSON对象
            JSONObject databaseJSONObject = Constant.DICTIONARY.getJSONObject(database);
            //获取当前要授权的用户对该数据库的操作权限
            String select = databaseJSONObject.getJSONObject(user).getString("select");
            String insert = databaseJSONObject.getJSONObject(user).getString("insert");
            String delete = databaseJSONObject.getJSONObject(user).getString("delete");
            String update = databaseJSONObject.getJSONObject(user).getString("update");
            //分别授权
            switch(permission) {
                case "select" :
                    select = "1";
                    break;
                case "insert" :
                    insert = "1";
                    break;
                case "delete" :
                    delete = "1";
                    break;
                case "update" :
                    update = "1";
                    break;
                default :
                    Util.showInTextArea(sql, Error.COMMAND_ERROR);
                    return;
            }
            //临时保存用户权限
            JSONObject temp = new JSONObject();
            temp.put("select", select);
            temp.put("insert", insert);
            temp.put("delete", delete);
            temp.put("update", update);
            //将原有用户权限重写
            databaseJSONObject.remove(user);
            databaseJSONObject.put(user, temp);
            //数据库字典重写
            Constant.DICTIONARY.remove(database);
            Constant.DICTIONARY.put(database, databaseJSONObject);
            Util.writeData(Constant.PATH_DICTIONARY, Constant.DICTIONARY.toString());
            Constant.selectPermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("select");
            Constant.insertPermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("insert");
            Constant.deletePermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("delete");
            Constant.updatePermission = Constant.currentDatabase.getJSONObject(Constant.currentUserName).getString("update");
            Util.showInTextArea(sql, Prompt.GRANT_SUCCESS);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public static boolean checkGrantGram() {
        Pattern pattern = Pattern.compile("grant (.{1,}) on (.{1,}) to (.{1,})");
        Matcher matcher = pattern.matcher(sql);
        if(matcher.matches()) {
            permission = matcher.group(1).trim();
            database = matcher.group(2).trim();
            user = matcher.group(3).trim();
            return true;
        } else {
            return false;
        }
    }
}
