package Service;


import Common.Util;
import Controller.TableController;
import org.json.JSONException;

/**
 * Created by wangquanxiu at 2018/5/27 20:24
 */
public class Select {
    public static void handleSql(String arrs[]) throws JSONException {
        TableController.selectTable(arrs);

    }
}
