package Common;

/**
 * Created by wangquanxiu at 2018/5/27 21:52
 */
public class PermissionControl {

    /**
     * 检查用户有无更改数据库结构权限，有则返回true,无则返回false
     * @return
     */
    public static boolean checkChangeStructurePermission() {
        if(Constant.userType.equals("root_user")) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 检查用户有无select权限，有则返回true，无则返回false
     * @return
     */
    public static boolean checkSelectPermission() {
        if(Constant.selectPermission.equals("1")) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 检查用户有无insert权限，有则返回true，无则返回false
     * @return
     */
    public static boolean checkInsertPermission() {
        if(Constant.insertPermission.equals("1")) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 检查用户有无delete权限，有则返回true，无则返回false
     * @return
     */
    public static boolean checkDeletePermission() {
        if(Constant.deletePermission.equals("1")) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 检查用户有无update权限，有则返回true，无则返回false
     * @return
     */
    public static boolean checkUpdatePermission() {
        if(Constant.updatePermission.equals("1")) {
            return true;
        } else {
            return false;
        }
    }

}
