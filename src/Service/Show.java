package Service;

import Common.Error;
import Common.Util;
import Controller.DatabaseController;
import Controller.TableController;
import Controller.ViewController;

/**
 * Created by wangquanxiu at 2018/5/27 20:27
 */
public class Show {
    public static void handleSql(String arrs[]){
        String sql = Util.arrayToString(arrs);
        if(arrs.length == 2){
            switch(arrs[1]){
                case "database":
                    DatabaseController.showDatabase(arrs);
                    break;
                case "table":
                    TableController.showTable(arrs);
                    break;
                case "view":
                    ViewController.showView(arrs);
                    break;
                    default:
                        Util.showInTextArea(sql, Error.COMMAND_ERROR);
                        break;
            }
        }else {
            Util.showInTextArea(sql,Error.COMMAND_ERROR);
            return;
        }

    }
}
