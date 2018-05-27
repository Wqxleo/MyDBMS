package Service;


import Common.Constant;
import Common.Error;
import Common.Prompt;
import Common.Util;
import Service.Create;
import org.json.JSONObject;

/**
 * Created by wangquanxiu at 2018/5/27 20:09
 */
public class PreHandle {


    private static boolean isLogin = false;
    private static String sql;
    private static String[] arr;
    //对输入的sql语句预处理
    public static void preHandleSql(String preSql) {
        //正则 +号匹配前面的换行符一次或多次
        sql = preSql.replaceAll("[\\n\\r\\t]+", " ").trim().toLowerCase().replaceAll(" +", " ");
        //以空格符分割成字符串数组
        arr = sql.split(" ");
        if(arr[0].equals("login")) {
            if(isLogin) {
                Util.showInTextArea(sql, Error.LOGIN_AGANIN);
            } else {
                handleLogin();
            }
        } else {
            if(isLogin) {
                //已经登录，执行其他操作
                switch(arr[0]) {
                    case "exit" : //退出
                        handleExit();
                        break;
                    case "create" :  //创建
                        Create.handleSql(arr);
                        break;
                    case "select" :
                        Select.handleSql(arr);
                        break;
                    case "insert" :
                        Insert.handleSql(arr);
                        break;
                    case "delete" :
                        Delete.handleSql(arr);
                        break;
                    case "update" :
                        Update.handleSql(arr);
                        break;
                    case "grant" :
                        Grant.handleSql(arr);
                        break;
                    case "revoke" :
                        Revoke.handleSql(arr);
                        break;
                    case "help" :
                        Help.handleSql(arr);
                        break;
                    case "show" :
                        Show.handleSql(arr);
                        break;
                    case "use" :
                        Use.handleSql(arr);
                        break;
                    default :
                        Util.showInTextArea(sql, Error.COMMAND_ERROR);
                        break;
                }
            } else {
                Util.showInTextArea(sql, Error.LOGIN_FIRST);
            }
        }
    }
    //处理登录请求
    public static void handleLogin() {
        //检查语法是否正确
        if(arr.length != 3) {
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }
        //检查用户是否存在
        if(!Constant.USERS.has(arr[1])) {
            Util.showInTextArea(sql, Error.USER_NOT_EXIST);
            return;
        }
        //检查密码是否正确
        net.sf.json.JSONObject user = Constant.USERS.getJSONObject(arr[1]);
        if(!user.get("password").equals(arr[2])) {
            Util.showInTextArea(sql, Error.PASSWORD_WRONG);
            return;
        }
        //全部验证通过
        Constant.currentUser = Constant.USERS.getJSONObject(arr[1]);
        Constant.currentUserName = arr[1];
        Constant.userType = Constant.currentUser.getString("type");
        isLogin = true;
        Util.showInTextArea(sql, Prompt.LOGIN_SUCCESS);
    }
    //处理退出请求
    public static void handleExit() {
        //检查语法是否正确
        if(arr.length != 1) {
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }
        isLogin = false;
        Constant.currentUser = null;
        Constant.currentDatabase = null;
        Constant.currentUserName = null;
        Constant.currentDatabaseName = null;
        Util.showInTextArea(sql, Prompt.EXIT_SYSTEM);
    }

}
