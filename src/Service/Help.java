package Service;

import Common.Error;
import Common.Util;
import Controller.DatabaseController;
import Controller.IndexController;
import Controller.TableController;
import Controller.ViewController;
import Entity.Table;

/**
 * Created by wangquanxiu at 2018/5/27 20:27
 */
public class Help {
    public static void handleSql(String arrs[]){
        String sql = Util.arrayToString(arrs);
        if(arrs.length >= 2){
            switch (arrs[1]){
                case "database":
                    DatabaseController.helpDatabase(arrs);
                    break;
                case "table":
                    TableController.helpTable(arrs);
                    break;
                case "view":
                    ViewController.helpView(arrs);
                    break;
                case "index":
                    IndexController.helpIndex(arrs);
                    break;
                    default:
                        Util.showInTextArea(sql, Error.COMMAND_ERROR);
                        break;
            }
        }
    }
}
