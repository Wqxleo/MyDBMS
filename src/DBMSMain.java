import Common.Constant;
import Common.Error;
import Common.Util;
import UI.MainFrame;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by wangquanxiu at 2018/5/20 20:08
 */
public class DBMSMain {

    public static void main(String[] args) throws JSONException {
        if(!init()){
            //初始化返回错误信息
            System.out.println(Error.INIT_FAILED);
            return;
        }

    }

    //初始化
    public static boolean init() throws JSONException {

        //注意顺序不能更改
        createRoot();
        createConfig();
        createDic();
        createUserDic();
        new MainFrame();
        //转到当前用户的操作
        Constant.USERS = new JSONObject(Util.readData(Constant.PATH_USERS));
        Constant.DICTIONARY = new JSONObject(Util.readData(Constant.PATH_DICTIONARY));
        return true;
    }

    //创建根目录
    public static void createRoot(){
        File rootFile = new File(Constant.PATH_ROOT);
        if(!rootFile.exists()){
            try {
                rootFile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //创建config文件
    public static void createConfig(){
        File configFile = new File(Constant.PATH_CONFIG);
        if(!configFile.exists()){
            try {
                configFile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //创建数据字典
    public static void createDic(){
        File dicFile = new File(Constant.PATH_DICTIONARY);
        if(!dicFile.exists()){
            try {
                dicFile.createNewFile();
                //更新
                Util.writeData(Constant.PATH_DICTIONARY,new JSONObject().toString());

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    //创建用户字典
    public static void createUserDic(){
        File userFile = new File(Constant.PATH_USERS);
        if(!userFile.exists()){
            try {
                userFile.createNewFile();

                //创建root用户
                JSONObject root = new JSONObject();
                root.put("type","root_user");
                root.put("root","root");
                root.put("password","root");

                //添加到用户文件中
                JSONObject user = new JSONObject();
                user.put("root", root);
                Util.writeData(Constant.PATH_USERS, user.toString());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
