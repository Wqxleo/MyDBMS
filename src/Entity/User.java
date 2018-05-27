package Entity;

import java.util.List;

/**
 * Created by wangquanxiu at 2018/5/25 20:35
 */
public class User {
    private int userid;//用户id，唯一标识
    private String username;//用户名
    private String password;//用户密码
    private String role;//用户
    private List<String> priviledge;//用户权限

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getPriviledge() {
        return priviledge;
    }

    public void setPriviledge(List<String> priviledge) {
        this.priviledge = priviledge;
    }
}
