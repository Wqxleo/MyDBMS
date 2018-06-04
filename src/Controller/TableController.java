package Controller;

import Common.*;
import Common.Error;
import org.apache.commons.lang.ObjectUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangquanxiu at 2018/5/25 20:33
 */
public class TableController {

    public static String tableName = null;

    //新建表
    public static void createTable(String arrs[]) throws JSONException {

        //正则表达式匹配sql语句
        boolean tem;
        String sql = Util.arrayToString(arrs);
        final String matchCreateTab = "create table [a-z_][a-z0-9_]{0,99} ?\\( ?( ?[a-z_][a-z0-9_]{0,99} (( ?int ?)|( ?do"
                + "uble ?)|( ?varchar ?))(( ?primary key ?)|( ?not null ?)|( unique ?))?)( ?, ?[a-z_][a-z0-9_]{0,99} (( ?i"
                + "nt ?)|( ?double ?)|( ?varchar ?))(( ?primary key ?)|( ?not null ?)|( unique ?))?){0,} ?\\)";

        Pattern pattern = Pattern.compile(matchCreateTab);
        Matcher matcher = pattern.matcher(sql);
        if(matcher.matches()){
            tableName = sql.substring(13,sql.indexOf("(")).trim();
            tem = true;
        }
        else {
            tem = false;
        }
        if(tem == false){
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }

        //检查用户权限
        if(!PermissionControl.checkChangeStructurePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }

        //检查当前的数据库是否在使用
        if(Constant.currentDatabase == null){
            Util.showInTextArea(sql,Error.NO_DATABASE_SELECT);
            return;
        }
        //检查表名是否已经存在
        if(Constant.currentDatabase.getJSONObject("table").has(tableName)){
            Util.showInTextArea(sql,Error.TABLE_EXIST);
            return;
        }

        try {
            String strTabNature = sql.substring(sql.indexOf("(")+1, sql.indexOf(")")).trim();
            String tabItem[] = strTabNature.split(",");
            JSONArray items = new JSONArray();
            for(int i = 0; i < tabItem.length; i++){
                String elem[] = tabItem[i].trim().split(" ");
                JSONObject temp = new JSONObject();
                temp.put("nature",elem[0].trim());//表的属性
                temp.put("type",elem[1].trim());//表的属性的类型
                if(elem.length == 3){
                    temp.put("limit",elem[2].trim());
                }
                else if(elem.length == 4){
                    temp.put("limit",elem[2].trim()+ " "+elem[3].trim());
                }
                items.put(temp);

            }
            JSONObject table = new JSONObject();
            table.put("size","0");
            table.put("items",items);
            //在当前使用的数据库中加入当前的新建的表
            Constant.currentDatabase.getJSONObject("table").put(tableName,table);
            //更新数据字典
            Util.writeData(Constant.PATH_DICTIONARY,Constant.DICTIONARY.toString());
            Util.createFile(Constant.PATH_ROOT+Constant.currentDatabaseName,tableName+".sql");
            Util.showInTextArea(sql, Prompt.CREATE_TABLE_SUCCESS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
