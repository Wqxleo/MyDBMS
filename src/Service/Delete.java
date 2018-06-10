package Service;

import Controller.TableController;
import org.json.JSONException;

/**
 * Created by wangquanxiu at 2018/5/27 20:24
 */
public class Delete {
    public static void handleSql(String arrs[]) throws JSONException {
        TableController.deleteInTable(arrs);
    }
}
