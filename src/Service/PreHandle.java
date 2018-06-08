package Service;


import Common.Constant;
import Common.Error;
import Common.Prompt;
import Common.Util;
import Service.Create;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangquanxiu at 2018/5/27 20:09
 */
public class PreHandle {


    public static boolean isLogin = false;
    private static String sql;
    private static String[] arr;
    //对输入的sql语句预处理
    public static void preHandleSql(String preSql) throws JSONException {
        //正则 +号匹配前面的换行符一次或多次
        sql = preSql.replaceAll("[\\n\\r\\t]+", " ").trim().toLowerCase().replaceAll(" +", " ");
        //以空格符分割成字符串数组
        arr = sql.split(" ");
        if(arr[0].equals("login")) {
            if(isLogin) {
                Util.showInTextArea(sql, Error.LOGIN_AGANIN);
            } else {
                Login.handleLogin(arr);
            }
        } else {
            if(isLogin) {
                //已经登录，执行其他操作
                switch(arr[0]) {
                    case "exit" : //退出
                        //重置
                        Exit.handleExit(arr);
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


}
