package Service;

import Common.Constant;
import Common.Error;
import Common.Util;
import Entity.ItemCondition;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangquanxiu at 2018/6/5 21:56
 */
public class GetResultWithCheck {
    private static List<String[]> aalldata = null; // alldata取反
    private static List<String[]> alldata = null;
    private static Pattern pattern = null;
    private static Matcher matcher = null;
    private static String match_and = "([a-z0-9_]+ ?(>|<|=|!=) ?.+)( and [a-z0-9_]+ ?(>|<|=|!=) ?.+){1,}";
    private static String match_or = "([a-z0-9_]+ ?(>|<|=|!=) ?.+)( or [a-z0-9_]+ ?(>|<|=|!=) ?.+){1,}";
    private static String match_single = "([a-z0-9_]+ ?(>|<|=|!=) ?.+)";
    public static int flag = 0;

    /**
     * 获得匹配的结果集
     *
     * @param sql
     * @param table
     * @param condition
     * @param delete
     * @return
     */
    public static List<String[]> getAllResult(String sql, String table, String view, String condition, boolean delete,
                                              boolean isview) {
        // 获得where子句的条件
        flag = 0;
        alldata = Util.readDataFromTable(table);
        aalldata = new LinkedList<String[]>();
        List<ItemCondition> list = getMulitCondition(sql, condition, table, view, isview);
        if (list == null) {
            return null;
        }
        if (flag == 1) { //and
            getDataWithAndCondition(list, table);
        } else if (flag == 2) { //and
            getDataWithOrCondition(list, table);
        }
        if (delete)
            return aalldata;
        return alldata;
    }

    /**
     * 获得不确定where条件的内容 即不确定 and or 还是 没有and没有or
     *
     * @param sql
     * @param condition
     * @param table
     * @return
     */
    public static List<ItemCondition> getMulitCondition(String sql, String condition, String table, String view, boolean isview) {
        alldata = Util.readDataFromTable(table);
        aalldata = new LinkedList<String[]>();
        List<ItemCondition> list;
        flag = 0;
        if (((list = getSingleCondition(match_and, "and", condition)) != null)) { //如果不符合and，返回null
            flag = 1;
        } else if ((list = getSingleCondition(match_or, "or", condition)) != null) { //如果不符合or，返回null
            flag = 2;
        } else if ((list = getSingleCondition(match_single, null, condition)) != null) {
            flag = 1;
        }
        // 内容不匹配
        if (flag == 0) {
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return null;
        }
        // 数据类型不匹配
        String tt = table;
        if(isview)
            tt = view;
        if (!checkAllConditions(sql, list, tt, isview)) {
            return null;
        }
        return list;
    }

    /**
     * 根据字符串内容进行匹配并分词获得内容，根据逗号，and， or分词
     *
     * @param match
     *            匹配的表达式
     * @param split
     *            分隔符
     * *@param condition
     *            原字符串
     * @return
     */
    public static List<ItemCondition> getSingleCondition(String match, String split, String content) {
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(content);
        if (matcher.matches()) {
            List<ItemCondition> list = new LinkedList<ItemCondition>();
            String items[] = { content };
            if (split != null) {
                items = content.split(split);
            }
            for (int i = 0; i < items.length; i++) {
                items[i] = items[i].trim();
                pattern = Pattern.compile("([a-z0-9_]+) ?(>|<|=|!=) ?(.+)");
                matcher = pattern.matcher(items[i]);
                if (matcher.matches()) {
                    list.add(new ItemCondition(matcher.group(1).trim(), matcher.group(2).trim(),
                            matcher.group(3).trim()));
                }
            }
            return list;
        }
        return null;
    }

    /**
     * 检查where子句中的每一个条件属性是否正确，类型是否匹配 u8ser = 123 and name = 123
     *
     * @param list
     * @return
     */
    public static boolean checkAllConditions(String sql, List<ItemCondition> list, String table, boolean isview) {
        try {
            JSONArray items;
            if (isview) {
                items = Constant.currentDatabase.getJSONObject("view").getJSONObject(table).getJSONArray("items");
            } else {
                items = Constant.currentDatabase.getJSONObject("table").getJSONObject(table).getJSONArray("items");
            }
            Iterator<ItemCondition> it = list.iterator();
            while (it.hasNext()) {
                ItemCondition temp = it.next();
                boolean match = false;
                for (int j = 0; j < items.length(); j++) {
                    JSONObject item = items.getJSONObject(j);
                    if (temp.getNature().equals(item.getString("nature"))) {// 属性匹配，再匹配数据类型
                        if (item.getString("type").equals("varchar")) {
                            if (!(temp.getOperation().equals("=") || temp.getOperation().equals("!="))) {
                                Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH + " : " + temp.getNature());
                                return false;
                            }
                        } else if (item.getString("type").equals("int")) {
                            try {
                                Integer.valueOf(temp.getValue());
                            } catch (Exception e) {
                                Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH + " : " + temp.getNature());
                                return false;
                            }
                        } else {
                            try {
                                Double.valueOf(temp.getValue());
                            } catch (Exception e) {
                                Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH + " : " + temp.getNature());
                                return false;
                            }
                        }
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    Util.showInTextArea(sql, Error.ATTR_NOT_EXIST + " : " + temp.getNature());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 每一条数据匹配and
     */
    public static void getDataWithAndCondition(List<ItemCondition> list, String table) {
        Iterator<String[]> it = alldata.iterator();
        while (it.hasNext()) {
            String[] arr = it.next();
            if (arr.length > 0) {
                for (int j = 0; j < list.size(); j++) {
                    ItemCondition temp = list.get(j);
                    int pos = Util.getNaturePosition(table, temp.getNature(), false);
                    if (temp.getOperation().equals("=")) {
                        if (!arr[pos].equals(temp.getValue())) {
                            aalldata.add(arr);
                            it.remove();
                            break;
                        }
                    } else if (temp.getOperation().equals("!=")) {
                        if (arr[pos].equals(temp.getValue())) {
                            aalldata.add(arr);
                            it.remove();
                            break;
                        }
                    } else if (temp.getOperation().equals(">")) {
                        if (Double.valueOf(arr[pos]) <= Double.valueOf(temp.getValue())) {
                            aalldata.add(arr);
                            it.remove();
                            break;
                        }
                    } else if (temp.getOperation().equals("<")) {
                        if (Double.valueOf(arr[pos]) >= Double.valueOf(temp.getValue())) {
                            aalldata.add(arr);
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 每一条数据匹配or
     */
    public static void getDataWithOrCondition(List<ItemCondition> list, String table) {
        Iterator<String[]> it = alldata.iterator();
        while (it.hasNext()) {
            String[] arr = it.next();
            if (arr.length > 0) {
                boolean is = false;
                for (int j = 0; j < list.size(); j++) {
                    ItemCondition temp = list.get(j);
                    int pos = Util.getNaturePosition(table, temp.getNature(), false);
                    if (temp.getOperation().equals("=")) {
                        if (arr[pos].equals(temp.getValue())) {
                            is = true;
                            break;
                        }
                    } else if (temp.getOperation().equals("!=")) {
                        if (!arr[pos].equals(temp.getValue())) {
                            is = true;
                            break;
                        }
                    } else if (temp.getOperation().equals(">")) {
                        if (Double.valueOf(arr[pos]) > Double.valueOf(temp.getValue())) {
                            is = true;
                            break;
                        }
                    } else if (temp.getOperation().equals("<")) {
                        if (Double.valueOf(arr[pos]) < Double.valueOf(temp.getValue())) {
                            is = true;
                            break;
                        }
                    }
                }
                if (!is) {
                    aalldata.add(arr);
                    it.remove();
                }
            }
        }
    }

}
