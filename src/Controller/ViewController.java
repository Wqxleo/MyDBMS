package Controller;

import Common.Constant;
import Common.Error;
import Common.PermissionControl;
import Common.Util;
import Entity.Table;
import Service.Select;
import net.sf.json.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangquanxiu at 2018/5/25 20:34
 */
public class ViewController {
    public static String view = null;
    public static String view_content = null;
    public static String sql = null;

    //创建视图
    public static void createView(String arrs[]){
        sql = Util.arrayToString(arrs);
        if(!checkCreateViewGram()){
            Util.showInTextArea(sql,Error.COMMAND_ERROR);
            return;
        }
        //检查数据库是否在使用
        if(Constant.currentDatabase == null){
            Util.showInTextArea(sql,Error.NO_DATABASE_SELECT);
            return;
        }

        //检查用户权限
        if(!PermissionControl.checkChangeStructurePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }

        //检查视图名是不是已经存在
        try {
            if(Constant.currentDatabase.getJSONObject("view").has(view)){
                Util.showInTextArea(sql, Error.VIEW_EXSIT);
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject tempView = new JSONObject();
            tempView.put("content",view_content);
            JSONArray array = new JSONArray();
            String natures[] = TableController.natures.split(",");
            JSONArray tablenatures = Constant.currentDatabase.getJSONObject("table").getJSONObject(TableController.tableName).getJSONArray("items");
            for(int i = 0; i < tablenatures.length(); i++){
                for(int j = 0; j < natures.length; j++){
                    if(tablenatures.getJSONObject(i).getString("nature").equals(natures[j].trim())){
                        array.put(tablenatures.getJSONObject(i));
                    }
                }
            }
            JSONObject views = new JSONObject();
            views.put("content",view_content);
            views.put("items",array);
            views.put("table",TableController.tableName);
            Constant.currentDatabase.getJSONObject("view").put(view,views);
            Util.writeData(Constant.PATH_DICTIONARY, Constant.DICTIONARY.toString());
            Util.showInTextArea(sql,"ok, a view : " + view + " is created success!");
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public static void showView(String arrs[]){

    }

    //help view
    public static void helpView(String arrs[]){
        sql = Util.arrayToString(arrs);
        // 语法错误
        if (arrs.length != 3) {
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }
        // 检查是否选中数据库
        if (Constant.currentDatabaseName == null) {
            Util.showInTextArea(sql, Error.NO_DATABASE_SELECT);
            return;
        }
        // 检查权限
        if(!PermissionControl.checkChangeStructurePermission()) {
            Util.showInTextArea(sql, Error.ACCESS_DENIED);
            return;
        }

        try {
            // 视图是否存在
            if (!Constant.currentDatabase.getJSONObject("view").has(arrs[2])) {
                Util.showInTextArea(sql, Error.VIEW_NOT_EXSIT + " ： " + arrs[2]);
                return;
            }
            String content = Constant.currentDatabase.getJSONObject("view").getJSONObject(arrs[2]).getString("content") + "\n";
            JSONArray natures = Constant.currentDatabase.getJSONObject("view").getJSONObject(arrs[2]).getJSONArray("items");
            content += "nature\t\t" + "type\t\t" + "limit" + "\n";
            for(int i = 0; i < natures.length(); i++){
                JSONObject temp = natures.getJSONObject(i);
                content += temp.getString("nature") + "\t\t" + temp.getString("type");
                if(temp.has("limit")){
                    content  += "\t\t" + temp.getString("limit");
                }
                content += "\n";
            }
            Util.showInTextArea(sql, "content" + " : " + content.substring(0, content.length() - 1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查创建视图语法
     */
    public static boolean checkCreateViewGram() {
        final String matchCreateViewGram = "create view ([a-z_][a-z0-9_]{0,99}) as (.+)";
        Pattern pattern = Pattern.compile(matchCreateViewGram);
        Matcher matcher = pattern.matcher(sql);
        if(matcher.matches()) {
            //捕获组
            view = matcher.group(1);
            view_content = matcher.group(2);
            // 检查select中的语法
            if (!TableController.checkSelectSqlGram(view_content)) {
                Util.showInTextArea(sql, Error.COMMAND_ERROR);
                return false;
            }
            // 获得所有的属性
            String[] natures_array = TableController.natures.split(",");
            for (int i = 0; i < natures_array.length; i++)
                natures_array[i] = natures_array[i].trim();
            List<String> natures_list = Util.getNaturesList(TableController.tableName, false);
            // 检查所有的属性是否存在
            String result = Util.checkAllNatureExsit(natures_list, natures_array);
            if (result != null) {
                Util.showInTextArea(sql, Error.ATTR_NOT_EXIST + " : " + result);
                return false;
            }
            return true;
        }
        return false;
    }


}
