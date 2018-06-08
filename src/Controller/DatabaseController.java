package Controller;

import Common.*;
import Common.Error;
import net.sf.json.JSON;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wangquanxiu at 2018/5/25 20:34
 */
public class DatabaseController {
    public static String sql = null;

    //创建数据库
    public static void createDb(String arrs[]) {
        sql = Util.arrayToString(arrs);
        //检查命令的格式是否正确
        if(!arrs[2].matches("[a-z_][a-z0-9_]{0,99}") || arrs.length != 3){
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }

        //检查数据库名是否已经存在
        if(Constant.DICTIONARY.has(arrs[2])){
            Util.showInTextArea(sql,Error.COMMAND_ERROR);
            return;
        }

        //检查用户是否有权限，root用户才能修改数据库结构
        if(!PermissionControl.checkChangeStructurePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }

        try {
            JSONObject tem = new JSONObject();
            tem.put("table",new JSONObject());
            tem.put("view",new JSONObject());

            //获取所有的用户名，初始化用户对数据库的操作权限
            String allUser = Util.readData(Constant.PATH_USERS);
            List<String> userName = Util.getUserJsonKeyByString(allUser);
            //迭代器配合List
            Iterator<String> iterator = userName.iterator();
            while(iterator.hasNext()){
                JSONObject permission = new JSONObject();
                permission.put("select", "1");
                permission.put("insert","1");
                permission.put("delete","1");
                permission.put("update","1");
                tem.put((String)iterator.next(),permission);
            }
            Constant.DICTIONARY.put(arrs[2],tem);
            //文件存放的位置可以优化
            Util.writeData(Constant.PATH_DICTIONARY,Constant.DICTIONARY.toString());
            //为数据库在根目录下新建一个文件夹，文件夹下可以创建table，view，index
            File temFile = new File(Constant.PATH_ROOT,arrs[2]);
            if(!temFile.exists()){
                temFile.mkdir();
            }
            Util.showInTextArea(sql, Prompt.CREATE_DATABASE_SUCCESS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //展示数据库
    public static void showDatabase(String arrs[]){
        sql = Util.arrayToString(arrs);

        //检查用户权限
        if(!PermissionControl.checkChangeStructurePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }
        try {
            Iterator iterator = Constant.DICTIONARY.keys();
            String dbStr = "";
            int total = 0;
            //迭代器的使用
            while (iterator.hasNext()){
                String key = (String)iterator.next();
                dbStr += key+"\n";
                total += 1;
            }
            Util.showInTextArea(sql,dbStr+"total : " + total);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 输出所有数据表、视图和索引的信息，同时显示其对象类型
     */
    public static void helpDatabase(String arrs[]){

        // 检查是否选中数据库
        if (Constant.currentDatabase == null) {
            Util.showInTextArea(sql, Error.NO_DATABASE_SELECT);
            return;
        }
        //检查权限
        if(!PermissionControl.checkChangeStructurePermission()) {
            Util.showInTextArea(sql, Error.ACCESS_DENIED);
            return;
        }

        try {
            String content;
            Iterator<String> iterator = Constant.currentDatabase.getJSONObject("view").keys();
            content = "all view : \n";
            int total = 0;
            while(iterator.hasNext()){
                content += iterator.next().toString()+ "\n";
                total++;
            }
            content += "total: " + total;

            content += "all table: \n";
            total = 0;
            iterator = Constant.currentDatabase.getJSONObject("table").keys();
            while(iterator.hasNext()){
                content += iterator.next().toString() + "\n";
                total++;
            }

            content += "total: "+ total;


            Util.showInTextArea(sql,content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
