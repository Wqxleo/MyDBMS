package Service;

import Common.Constant;
import Common.Error;
import Common.Prompt;
import Common.Util;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangquanxiu at 2018/6/3 19:42
 */
public class Login {
    //处理登录请求
    public static void handleLogin(String arr[]) throws JSONException {
        String sql = Util.arrayToString(arr);
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
        JSONObject user = Constant.USERS.getJSONObject(arr[1]);
        if(!user.get("password").equals(arr[2])) {
            Util.showInTextArea(sql, Error.PASSWORD_WRONG);
            return;
        }
        //全部验证通过
        Constant.currentUser = Constant.USERS.getJSONObject(arr[1]);
        Constant.currentUserName = arr[1];
        Constant.userType = Constant.currentUser.getString("type");
        PreHandle.isLogin = true;
        Util.showInTextArea(sql, Prompt.LOGIN_SUCCESS);
    }
}
