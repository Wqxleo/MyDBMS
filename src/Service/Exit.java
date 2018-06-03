package Service;

import Common.Constant;
import Common.Error;
import Common.Prompt;
import Common.Util;

/**
 * Created by wangquanxiu at 2018/6/3 19:44
 */
public class Exit {

    //处理退出请求
    public static void handleExit(String arr[]) {
        //检查语法是否正确
        if(arr.length != 1) {
            Util.showInTextArea(Util.arrayToString(arr), Error.COMMAND_ERROR);
            return;
        }
        PreHandle.isLogin = false;
        Constant.currentUser = null;
        Constant.currentDatabase = null;
        Constant.currentUserName = null;
        Constant.currentDatabaseName = null;
        Util.showInTextArea(Util.arrayToString(arr), Prompt.EXIT_SYSTEM);
    }
}
