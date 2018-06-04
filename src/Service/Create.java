package Service;

import Common.Error;
import Common.Util;
import Controller.*;
import org.json.JSONException;

/**
 * Created by wangquanxiu at 2018/5/27 20:24
 */
public class Create {
    public static void handleSql(String arrs[]) throws JSONException {
        if(arrs.length >= 3){
            switch (arrs[1]){
                case "user":
                    UserController.createdUser(arrs);
                    break;
                case "database":
                    DatabaseController.createDb(arrs);
                    break;
                case "table":
                    TableController.createTable(arrs);
                    break;
                case "view":
                    ViewController.createView(arrs);
                    break;
                case "index":
                    IndexController.createIndex(arrs);
                    break;
                default:
                    Util.showInTextArea(Util.arrayToString(arrs), Error.COMMAND_ERROR);
                    break;

            }
        }
        else {
            Util.showInTextArea(Util.arrayToString(arrs),Error.COMMAND_ERROR);
        }
    }
}
