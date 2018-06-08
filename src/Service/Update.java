package Service;

import Controller.TableController;
import Entity.Table;

/**
 * Created by wangquanxiu at 2018/5/27 20:25
 */
public class Update {
    public static void handleSql(String arrs[]){
        TableController.updateTable(arrs);

    }
}
