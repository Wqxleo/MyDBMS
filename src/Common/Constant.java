package Common;

import net.sf.json.JSONObject;

/**
 * Created by wangquanxiu at 2018/5/27 21:38
 */
public class Constant {

    //路径
    public static final String PATH_ROOT = "C:/DBMS/";
    public static final String PATH_CONFIG = PATH_ROOT + "./config/";
    public static final String PATH_USERS = PATH_CONFIG + "./users.sql"; //用户文件目录
    public static final String PATH_DICTIONARY = PATH_CONFIG + "./dictionary.sql";  //数据字典目录

    //数据字典
    public static JSONObject USERS = null; //用户字典
    public static JSONObject DICTIONARY = null; //数据字典

    //当前
    public static JSONObject currentUser = null; //当前登录用户
    public static JSONObject currentDatabase = null; //当前所在数据库
    public static String currentUserName = null; //当前登录用户名
    public static String currentDatabaseName = null; //当前所在数据库名

    //当前用户对当前数据库的权限
    public static String selectPermission = null;
    public static String insertPermission = null;
    public static String deletePermission = null;
    public static String updatePermission = null;

    //表中nature之间的分界符
    public static String SPLIT = "=>";// 分隔符

    //当前用户的类型     管理员/普通用户
    public static String userType = null;
}
