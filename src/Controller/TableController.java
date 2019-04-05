package Controller;

import Common.*;
import Common.Error;
import Service.GetResultWithCheck;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangquanxiu at 2018/5/25 20:33
 */
public class TableController {

    public static String tableName = null;
    public static String[] values = null;
    public static String sql;
    public static boolean isview = false;
    public static String view = null;
    public static String natures = null;
    public static Pattern pattern = null;
    public static Matcher matcher = null;
    public static String order_nature = null;
    public static String order = "asc";
    public static String func = null;// 聚集函数
    public static String condition = null;
    public static boolean isLinkQuery = false;
    public static List<String[]> alldata = null;
    public static String linkNature = null;
    public static String column = null;
    public static String value = null;
    public static int indexOfNature = -1;
    public static String subQuery = null;
    public static String natures2 = null;
    public static String setIdentify = null;
    public static String table2 = null;
    public static JSONArray items = null;
    public static String columnOfCondition = null;
    public static String valueOfCondition = null;
    public static int indexOfRecord = -1;
    public static int valuePositionInTable = 0;
    public static int tableBeforeSize = 0;
    public static int tableAfterSize = 0;

    //新建表
    public static void createTable(String arrs[]) throws JSONException {

        //正则表达式匹配sql语句
        boolean tem;
        String sql = Util.arrayToString(arrs);
        final String matchCreateTab = "create table [a-z_][a-z0-9_]{0,99} ?\\( ?( ?[a-z_][a-z0-9_]{0,99} (( ?int ?)|( ?do"
                + "uble ?)|( ?varchar ?))(( ?primary key ?)|( ?not null ?)|( unique ?))?)( ?, ?[a-z_][a-z0-9_]{0,99} (( ?i"
                + "nt ?)|( ?double ?)|( ?varchar ?))(( ?primary key ?)|( ?not null ?)|( unique ?))?){0,} ?\\)";

        Pattern pattern = Pattern.compile(matchCreateTab);
        Matcher matcher = pattern.matcher(sql);
        if(matcher.matches()){
            tableName = sql.substring(13,sql.indexOf("(")).trim();
            tem = true;
        }
        else {
            tem = false;
        }
        if(tem == false){
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }

        //检查用户权限
        if(!PermissionControl.checkChangeStructurePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }

        //检查当前的数据库是否在使用
        if(Constant.currentDatabase == null){
            Util.showInTextArea(sql,Error.NO_DATABASE_SELECT);
            return;
        }
        //检查表名是否已经存在
        if(Constant.currentDatabase.getJSONObject("table").has(tableName)){
            Util.showInTextArea(sql,Error.TABLE_EXIST);
            return;
        }

        try {
            String strTabNature = sql.substring(sql.indexOf("(")+1, sql.indexOf(")")).trim();
            String tabItem[] = strTabNature.split(",");
            JSONArray items = new JSONArray();
            for(int i = 0; i < tabItem.length; i++){
                String elem[] = tabItem[i].trim().split(" ");
                JSONObject temp = new JSONObject();
                temp.put("nature",elem[0].trim());//表的属性
                temp.put("type",elem[1].trim());//表的属性的类型
                if(elem.length == 3){
                    temp.put("limit",elem[2].trim());
                }
                else if(elem.length == 4){
                    temp.put("limit",elem[2].trim()+ " "+elem[3].trim());
                }
                items.put(temp);

            }
            JSONObject table = new JSONObject();
            table.put("size","0");
            table.put("items",items);
            //在当前使用的数据库中加入当前的新建的表
            Constant.currentDatabase.getJSONObject("table").put(tableName,table);
            //更新数据字典
            Util.writeData(Constant.PATH_DICTIONARY,Constant.DICTIONARY.toString());
            Util.createFile(Constant.PATH_ROOT+Constant.currentDatabaseName,tableName+".sql");
            Util.showInTextArea(sql, Prompt.CREATE_TABLE_SUCCESS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //在表中插入
    public static void insertTable(String arrs[]){
        sql = Util.arrayToString(arrs);
        if(!checkInsertSqlGram()){
            Util.showInTextArea(sql,Error.COMMAND_ERROR);
            return;
        }
        if(!checkDataType()){
            Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH);
            return;
        }

        String str = values[0];
        for(int i = 1; i < values.length; i++){
            str += Constant.SPLIT+values[i];
        }
        try {
            int size = Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName).getInt("size");
            Util.insertDataToTable(str+"\r\n",tableName) ;
            Util.updateTableSize(tableName,size+1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Util.showInTextArea(sql,Prompt.INSERT_DATA_SUCCESS + arrs[2] + "!" +" : " + "1 rows affected" );


    }

    //在表中查找
    public static void selectTable(String arrs[]) throws JSONException {
        sql = Util.arrayToString(arrs);

        //检查查找语句是否符合规范
        if(!checkSelectSqlGram(subQuery)){
            Util.showInTextArea(sql,Error.COMMAND_ERROR);
            return;
        }

        //检查当前是否有数据库被use
        if(Constant.currentDatabase == null){
            Util.showInTextArea(sql,Error.NO_DATABASE_SELECT);
            return;

        }

        //检查用户是否具备查找权限
        if(!PermissionControl.checkSelectPermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }

        String[] tableArr = tableName.split(",");

        for(String tem: tableArr){
            //检查用户输入的表在不在table中
            try {
                if(!Constant.currentDatabase.getJSONObject("table").has(tem)){
                    //检查用户输入的表在不在view中
                    if(!Constant.currentDatabase.getJSONObject("view").has(tem)){
                        //如果检查不到则报错
                        Util.showInTextArea(sql,Error.TABLE_NOT_EXIST+ ":"+tem);
                        return;
                    }
                    else {
                        isview = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(tableArr.length > 1){
            linkQuery();
        }

        //检查是不是嵌套查询
        if(subQuery != null){
            nestQuery();
        }

        //是不是视图
        if(isview){
            view = tableName;
            try {
                tableName = Constant.currentDatabase.getJSONObject("view").getJSONObject(tableName).getString("table");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        //不带where限定条件
        if(condition == null){
            if(isLinkQuery == false) {
                alldata = Util.readDataFromTable(tableName);
            }
        }
        //带有where限定条件
        else {
            if(isLinkQuery == false){
                alldata = GetResultWithCheck.getAllResult(sql,tableName,view,condition,false,isview);
            }
        }


        // 获得最终的数据
        if (alldata == null) {
            return;
        }
        // 匹配是否有聚集函数,提取出natures
        matchNaturesGrammer();
        //排序
        if (order_nature != null) {
            if (!sort()) {
                return;
            }
        }
        if (!natures.equals("*")) {
            if (!getResultWithNatures(natures)) {
                return;
            }
        } else {//是* ，判断是不是视图
            if (isview) {
                String[] arr = Util.getNaturesArray(view, isview);
                String content = arr[0];
                for (int i = 1; i < arr.length; i++) {
                    content += "," + arr[i];
                }
                if (!getResultWithNatures(content)) {
                    return;
                }
            }
        }
        if (func != null) {// 处理聚集函数
            handleFunc();
            return;
        }
        String tt = tableName;
        if (isview)
            tt = view;
        String head = "";
        if (natures.equals("*"))
            head = Util.getNaturesString(tt, isview);
        else {
            String[] temp = natures.split(",");
            head = temp[0];
            for (int i = 1; i < temp.length; i++) {
                head += "\t\t" + temp[i].trim();
            }
        }
        String result = Util.parseListToOutput(alldata);
        Util.showInTextArea(sql, head + "\n" + result + "total : " + alldata.size());






    }


    //更新表
    public static void updateTable(String arrs[]){
        sql = Util.arrayToString(arrs);

        //检查数据库是否选中
        if(Constant.currentDatabase == null){
            Util.showInTextArea(sql,Error.NO_DATABASE_SELECT);
            return;
        }

        //检查用户权限
        if(!PermissionControl.checkUpdatePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }

        //检查语句是否合法
        if(!checkUpdateGram()){
            Util.showInTextArea(sql,Error.COMMAND_ERROR);
            return;
        }
        //检查表是不是存在
        try {
            if(!Constant.currentDatabase.getJSONObject("table").has(tableName)){
                Util.showInTextArea(sql,Error.TABLE_NOT_EXIST);
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(condition == null){
            updateWithoutCondition();
        }else{
            updateWithCondition();
        }



    }


    //删除表中的记录
    public static void deleteInTable(String arrs[]) throws JSONException {
        sql = Util.arrayToString(arrs);

        //检查是否选中数据库
        if(Constant.currentDatabase == null){
            Util.showInTextArea(sql,Error.NO_DATABASE_SELECT);
            return;
        }

        //检查语法是否正确
        if(!checkDeleteGram()){
            Util.showInTextArea(sql,Error.COMMAND_ERROR);
            return;
        }

        //检查表或者视图是否存在
        try {
            if(!Constant.currentDatabase.getJSONObject("table").has(tableName)){
                if(!Constant.currentDatabase.getJSONObject("view").has(tableName)){
                    Util.showInTextArea(sql,Error.TABLE_NOT_EXIST);
                    return;
                }
                else {
                    isview = true;
                    view = tableName;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //处理
        if(condition == null){
            deleteWithoutCondition();
        }else {
            deleteWithCondition();
        }


    }
    //展示所有表
    public static void  showTable(String arrs[]){

        sql = Util.arrayToString(arrs);
        if(!PermissionControl.checkChangeStructurePermission()){
            Util.showInTextArea(sql,Error.ACCESS_DENIED);
            return;
        }
        if(Constant.currentDatabase == null){
            Util.showInTextArea(sql,Error.NO_DATABASE_SELECT);
            return;
        }

        try {
            JSONObject tables = Constant.currentDatabase.getJSONObject("table");
            Iterator iterator = tables.keys();
            String tableStr = "";
            int total = 0;
            while(iterator.hasNext()){
                String tem = iterator.next().toString();
                tableStr += tem + "\n";
                total += 1;
            }
            Util.showInTextArea(sql,tableStr + "total : " + total);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //help table
    public static void helpTable(String arrs[]){
        sql = Util.arrayToString(arrs);
        // 检查语法
        if (arrs.length != 3) {
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }
        // 是否选中数据库
        if (Constant.currentDatabase == null) {
            Util.showInTextArea(sql, Error.NO_DATABASE_SELECT);
            return;
        }
        // 表是否存在
        try {
            if (!Constant.currentDatabase.getJSONObject("table").has(arrs[2])) {
                Util.showInTextArea(sql, Error.TABLE_NOT_EXIST + " : " + arrs[2]);
                return;
            }
            // 检查权限
            if(!PermissionControl.checkChangeStructurePermission()) {
                Util.showInTextArea(sql, Error.ACCESS_DENIED);
                return;
            }
            JSONArray table = Constant.currentDatabase.getJSONObject("table").getJSONObject(arrs[2]).getJSONArray("items");
            String content = arrs[2] + "\nnature\ttype\tlimit\n";
            for (int i = 0; i < table.length(); i++) {
                JSONObject temp = table.getJSONObject(i);
                content += temp.getString("nature") + "\t";
                content += temp.getString("type");
                if (temp.has("limit")) {
                    content += "\t" + temp.getString("limit");
                }
                content += "\n";
            }
            Util.showInTextArea(sql, content.substring(0, content.length() - 1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    //检查insert语句是否合法
    public static boolean  checkInsertSqlGram() {
        try {
            Pattern pattern = Pattern.compile("insert into (.{1,}) values ?\\((.{1,})\\)");
            Matcher m = pattern.matcher(sql);
            if(m.matches()) {
                //捕获组
                tableName = m.group(1);
                String temp_value = m.group(2); //(1,'Tom',26)
                pattern = Pattern.compile("( ?.{1,} ?, ?)+");
                m = pattern.matcher(temp_value + ",");
                if(m.matches()) {
                    values = temp_value.split(",");

                    for(int i=0; i < values.length; i++) {
                        values[i] = values[i].trim();
                    }
                    return true;
                }
            }
            pattern = Pattern.compile("insert into (.{1,})  values ?\\\\((.{1,})\\\\)\"");



        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //检查属性的数据类型是否合法
    public static boolean checkDataType(){
        try {
            JSONArray items = Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName).getJSONArray("items");
            if(items.length() != values.length){
                Util.showInTextArea(sql,Error.DATATYPE_NOT_MATCH);
                return  false;
            }
            for(int i=0;i<values.length;i++) {
                String type = items.getJSONObject(i).getString("type");
                boolean match = false;
                switch(type) {
                    case "int" :
                        match = isMatchInt(values[i]);
                        break;
                    case "double" :
                        match = isMatchDouble(values[i]);
                        break;
                    case "varchar" :
                        match = isMatchCharOrVarchar(values[i]);
                        break;
                    case "char" :
                        match = isMatchCharOrVarchar(values[i]);
                        break;
                    default :
                        Util.showInTextArea(sql, Error.SYSTEM_ERROR);
                        break;
                }
                if(match == false) {
                    Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH);
                    return false;
                }
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH);
        return false;

    }
    //检查属性的数据类型是否符合int类型
    public static boolean isMatchInt(String value) {
        if(value.matches("[0-9]+") || value.equals("")) {
            return true;
        } else {
            return false;
        }
    }
    //检查属性的数据类型是否符合Double类型
    public static boolean isMatchDouble(String value) {
        if(value.contains(".") || value.equals("")) {
            return true;
        } else {
            return false;
        }
    }
    //检查属性的数据类型是否符合字符类型
    public static boolean isMatchCharOrVarchar(String value) {
        if((value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
                || (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') || value.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    //检查Select语句是否合法
    public static boolean checkSelectSqlGram(String sql){
        //没有限制语句的查找
        String match = "select (.{1,}) from ([a-z0-9_]+)";
        Pattern pattern = Pattern.compile(match);
        Matcher matcher = pattern.matcher(sql);
        if(matcher.matches()){
            //捕获分组，获取属性组
            natures = matcher.group(1).trim();
            //捕获查找表的组，连接查询
            tableName = matcher.group(2).trim();
            return true;
        }

        //带order by的查询
        match = "select (.{1,}) from ([a-z0-9_]+) order by (.{1,}?)( desc|asc)";
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(sql);
        if(matcher.matches()){
            natures = matcher.group(1).trim();
            tableName = matcher.group(2).trim();
            order_nature = matcher.group(3).trim();
            if(matcher.group(4) != null){
                order = matcher.group(4).trim();
            }
            return  true;
        }

        //带where的条件查询
        match = "select (.+) from ([a-z0-9_]+) where (.+)";
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(sql);
        if(matcher.matches()){
            //捕获组
            natures = matcher.group(1).trim();
            tableName = matcher.group(2).trim();
            condition = matcher.group(3).trim();
            return  true;
        }
        //带where和orderby的条件查询
        match = "select (.+) from ([a-z0-9_]+) where (.+) order by (.+)( desc|asc)";
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(sql);
        if (matcher.matches()) {
            natures = matcher.group(1).trim();
            tableName = matcher.group(2).trim();
            condition = matcher.group(3).trim();
            order_nature = matcher.group(4).trim();
            if (matcher.group(5) != null)
                order = matcher.group(5).trim();
            return true;
        }

        //带where的连接查询
        match = "select (.{1,}) from ([a-z0-9_]+,[a-z0-9_]+) where (.{1,}?)"; //select * from t1,t2 where t1.id = t2.id|select id,name from t1,t2 where t1.id = t2.id
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(sql);
        if(matcher.matches()) {
            natures = matcher.group(1).trim();
            tableName = matcher.group(2).trim();

            condition = matcher.group(3).trim();
            isLinkQuery = true;
            return true;
        }

        //带where和in的条件查询
        match = "select (.{1,}?) from ([a-z0-9_]+) where ([a-z0-9_]+) in (.{1,})"; //select * from t1 where id in (select id from t2 where name = 'jim')
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(sql);
        if(matcher.matches()) {
            natures = matcher.group(1).trim();
            tableName = matcher.group(2).trim();
            linkNature = matcher.group(3).trim();
            subQuery = matcher.group(4).trim();
            return true;
        }
        //union|intersect|minus查询

        match = "select (.{1,}) from ([a-z0-9_]+) (union|intersect|minus) select (.{1,}) from ([a-z0-9_]+)";//select * from t1 union|intersect|minus select * from t2
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(sql);
        if(matcher.matches()) {
            natures = matcher.group(1).trim();
            tableName = matcher.group(2).trim();
            setIdentify = matcher.group(3).trim();
            natures2 = matcher.group(4).trim();
            table2 = matcher.group(5).trim();
            return true;
        }

        return  false;
    }

    /**
     * 连接查询
     * @return
     */
    public static void linkQuery() { //select * from t1,t2 where t1.id=t2.id

        return;
    }

    //嵌套查询
    /**
     * 嵌套查询
     */
    public static void nestQuery() { //select * from t1 where id in (select id from t2)
        //检查子查询的语法
        if(!checkSelectSqlGram(subQuery)) {
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }
        //先进行子查询
        String[] subQueryArr = subQuery.split(" ");
        String subNature = subQueryArr[1].trim();
        String subTable = subQueryArr[3].trim();
        try {
            if(!Constant.currentDatabase.getJSONObject("table").has(subTable)) {
                Util.showInTextArea(sql, Error.TABLE_NOT_EXIST + " : " + subTable);
                return;
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        int subNaturIndex = 0; //表示第几个，从1开始
        //找到subNature是第几个属性，即给subNatureIndex赋值
        try {
            JSONArray items = Constant.currentDatabase.getJSONObject("table").getJSONArray("items");
            for(int i=0; i<items.length(); i++) {
                subNaturIndex++;
                if(((String)items.getJSONObject(i).getString("nature")).equals(subNature)) {
                    break;
                }
            }
            String subTablePath = Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+subTable+".sql"; //子查询
            String subTableData = Util.readData(subTablePath);
            List<String> subQueryResult = new LinkedList<String> (); //子查询的结果
            Iterator<String> iterator = subQueryResult.iterator();
            String[] subTableRecord = subTableData.split("\r\n");
            for(int i=0; i<subTableRecord.length; i++) {
                String[] temp = subTableRecord[i].split(Constant.SPLIT);
                subQueryResult.add(temp[subNaturIndex - 1]);
            }
            List<String[]> queryResult = new LinkedList<String[]> (); //最终的查询结果
            String fatTablePath = Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+tableName+".sql"; //父查询
            String fatTableData = Util.readData(fatTablePath);
            //List<String[]> fatQueryResult = new LinkedList<String[]> (); //父查询的结果
            String[] fatTableDataRecord = fatTableData.split("\r\n");
            for(int i=0; i<fatTableDataRecord.length; i++) {
                String[] temp = fatTableDataRecord[i].split(Constant.SPLIT);
                while(iterator.hasNext()) {
                    if(temp[subNaturIndex - 1].equals(((String)iterator.next()))) { //符合in条件，就把这条记录加进最终结果中
                        queryResult.add(temp);
                        break;
                    }
                }
            }
            String result = Util.parseListToOutput(queryResult);
            String head = "";
            for(int i=0; i<items.length(); i++) {
                head += (String)items.getJSONObject(i).getString("nature") + "\t\t";
            }
            Util.showInTextArea(sql, head + "\n" + result + "total : " + queryResult.size());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 查看是否含有聚集函数并提取出nature
     *
     * @return
     */
    public static void matchNaturesGrammer() {
        String match = "(count|max|avg|sum|min) ?\\( ?(.+) ?\\)";
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(natures);
        if (matcher.matches()) {
            func = matcher.group(1).trim();
            natures = matcher.group(2).trim();
        }
    }
    /**
     * 对数据冒泡排序
     */
    private static boolean sort() throws JSONException {
        String ttt = tableName;
        if (isview)
            ttt = view;
        int pos = 0;
        if ((pos = Util.getNaturePosition(ttt, order_nature, isview)) != -1) {
            String type = Util.getNatureType(ttt, order_nature, isview);
            String temp[] = null;
            String[][] arr = Util.parseListToArray(alldata);
            for (int i = arr.length - 1; i > 0; --i) {
                for (int j = 0; j < i; ++j) {
                    if (order.equals("asc")) {
                        if (type.equals("int") || type.equals("double")) {
                            if (Double.parseDouble(arr[j + 1][pos]) < Double.parseDouble(arr[j][pos])) {
                                temp = arr[j];
                                arr[j] = arr[j + 1];
                                arr[j + 1] = temp;
                            }
                        }else{
                            if(compareString(arr[j + 1][pos], arr[j][pos]) < 0){
                                temp = arr[j];
                                arr[j] = arr[j + 1];
                                arr[j + 1] = temp;
                            }
                        }
                    } else {
                        if (type.equals("int") || type.equals("double")) {
                            if (Double.parseDouble(arr[j + 1][pos]) > Double.parseDouble(arr[j][pos])) {
                                temp = arr[j];
                                arr[j] = arr[j + 1];
                                arr[j + 1] = temp;
                            }
                        }else{
                            if(compareString(arr[j + 1][pos], arr[j][pos]) > 0){
                                temp = arr[j];
                                arr[j] = arr[j + 1];
                                arr[j + 1] = temp;
                            }
                        }
                    }
                }
            }
            alldata = Util.parseArrayToList(arr);
            return true;
        } else {
            Util.showInTextArea(sql, Error.ATTR_NOT_EXIST + " : " + order_nature);
        }
        return false;
    }

    /**
     * 获得结果
     *
     * @return
     */
    public static boolean getResultWithNatures(String anatures) {
        // 获得所有的属性
        String[] natures_array = anatures.split(",");
        for (int i = 0; i < natures_array.length; i++)
            natures_array[i] = natures_array[i].trim();
        List<String> natures_list;
        if (isview)
            natures_list = Util.getNaturesList(view, isview);
        else
            natures_list = Util.getNaturesList(tableName, isview);
        // 检查所有的属性是否存在
        String result = Util.checkAllNatureExsit(natures_list, natures_array);
        if (result != null) {
            Util.showInTextArea(sql, Error.ATTR_NOT_EXIST + " : " + result);
            return false;
        }
        // 获得所有属性的位置
        int[] pos = new int[natures_array.length];
        for (int i = 0; i < natures_array.length; i++)
            pos[i] = Util.getNaturePosition(tableName, natures_array[i], false);
        for (int i = 0; i < alldata.size(); i++) {
            String[] temp = alldata.get(i);
            String[] newv = new String[pos.length];
            alldata.remove(i);
            for (int j = 0; j < pos.length; j++) {
                newv[j] = temp[pos[j]];
            }
            alldata.add(i, newv);
        }
        return true;
    }

    //处理聚集函数
    public static void handleFunc() throws JSONException {
        String tt = tableName;
        if (isview)
            tt = view;
        switch (func) {
            case "count":
                Util.showInTextArea(sql, "count(" + natures + ")\n" + alldata.size());
                break;
            case "max":
                if (Util.getNatureType(tt, natures, isview).equals("int")
                        || Util.getNatureType(tt, natures, isview).equals("double")) {
                    if (alldata.size() > 0) {
                        double max = 0;
                        max = Double.parseDouble(alldata.get(0)[0]);
                        for (int i = 1; i < alldata.size(); i++) {
                            if (max <= Double.parseDouble(alldata.get(i)[0])) {
                                max = Double.parseDouble(alldata.get(i)[0]);
                            }
                        }
                        Util.showInTextArea(sql, "max(" + natures + ")\n" + String.valueOf(max));
                    } else {
                        Util.showInTextArea(sql, "max(" + natures + ")\n" + "null");
                    }
                } else {
                    Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH + " : " + natures);
                }
                break;
            case "min":
                if (Util.getNatureType(tt, natures, isview).equals("int")
                        || Util.getNatureType(tt, natures, isview).equals("double")) {
                    if (alldata.size() > 0) {
                        double min = 0;
                        min = Double.parseDouble(alldata.get(0)[0]);
                        for (int i = 1; i < alldata.size(); i++) {
                            if (min >= Double.parseDouble(alldata.get(i)[0])) {
                                min = Double.parseDouble(alldata.get(i)[0]);
                            }
                        }
                        Util.showInTextArea(sql, "min(" + natures + ")\n" + String.valueOf(min));
                    } else {
                        Util.showInTextArea(sql, "min(" + natures + ")\n" + "null");
                    }
                } else {
                    Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH + " : " + natures);
                }
                break;
            case "avg":
                if (Util.getNatureType(tt, natures, isview).equals("int")
                        || Util.getNatureType(tt, natures, isview).equals("double")) {
                    if (alldata.size() > 0) {
                        double sum = 0;
                        for (int i = 0; i < alldata.size(); i++) {
                            sum += Double.parseDouble(alldata.get(i)[0]);
                        }
                        Util.showInTextArea(sql, "avg(" + natures + ")\n" + String.valueOf(sum / alldata.size()));
                    } else {
                        Util.showInTextArea(sql, "avg(" + natures + ")\n" + "null");
                    }
                } else {
                    Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH + " : " + natures);
                }
                break;
            case "sum":
                if (Util.getNatureType(tt, natures, isview).equals("int")
                        || Util.getNatureType(tt, natures, isview).equals("double")) {
                    if (alldata.size() > 0) {
                        double sum = 0;
                        for (int i = 0; i < alldata.size(); i++) {
                            sum += Double.parseDouble(alldata.get(i)[0]);
                        }
                        Util.showInTextArea(sql, "sum(" + natures + ")\n" + String.valueOf(sum));
                    } else {
                        Util.showInTextArea(sql, "sum(" + natures + ")\n" + "null");
                    }
                } else {
                    Util.showInTextArea(sql, Error.DATATYPE_NOT_MATCH + " : " + natures);
                }
                break;
        }
    }


    /**
     * 比较字符串
     * @param s1
     * @param s2
     * @return
     */
    public static int compareString(String s1, String s2){
        s1 = s1.substring(1, s1.length() - 1);
        s2 = s2.substring(1, s2.length() - 1);
        int i = 0;
        for(i = 0; i < s1.length() && i < s2.length(); i++){
            if(s1.charAt(i) != s2.charAt(i))
                return s1.charAt(i) -s2.charAt(i);
        }
        return s1.length() - s2.length();
    }
    /**
     * 检查更新语法 并获取表名，更新条件，where条件
     * @return
     */
    public static boolean checkUpdateGram() {
        String match = "update ([a-z0-9_]+) set (.+) where (.+)";
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(sql);
        if(matcher.matches()) {
            tableName = matcher.group(1).trim();
            natures = matcher.group(2).trim();
            condition = matcher.group(3).trim();
            return true;
        } else {
            match = "update ([a-z0-9_]+) set (.+)";
            pattern = Pattern.compile(match);
            matcher = pattern.matcher(sql);
            if(matcher.matches()) {
                tableName = matcher.group(1).trim();
                natures = matcher.group(2).trim();
                return true;
            }
        }
        return false;
    }


    /**
     * 不带有where子句的update操作
     */
    public static void updateWithoutCondition() {
        //以等号分割
        String[] temp = natures.split("=");
        //属性列
        column = temp[0].trim();
        value = temp[1].trim();
        //检查列是否存在
        if(!checkColumnExsitOfUpdate()) {
            Util.showInTextArea(sql, Error.COLUMN_NOT_EXSIT);
            return;
        }

        String tablePath = Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+tableName+".sql";
        String tableData = Util.readData(tablePath);
        if(tableData == null || tableData == "" || tableData.matches("[\n\t]+")) { //表中没有数据，不进行更新
            Util.showInTextArea(sql, Prompt.UPDATE_SUCCESS + " : 0 rows affected.");
        } else {
            //以换行符分割，按行处理
            String[] recordArr = tableData.split("\r\n");
            List<String[]> colValList = new LinkedList<String[]> ();
            for(int i=0; i<recordArr.length; i++) {
                //按属性之间的分隔符分割
                String[] temp1 = recordArr[i].split(Constant.SPLIT);
                temp1[indexOfNature - 1] = value;
                colValList.add(temp1);
            }
            String resultTableData = "";
            Iterator<String[]> iterator = colValList.iterator();
            while(iterator.hasNext()) {
                String[] temp2 = (String[])iterator.next();
                for(int i=0; i<temp2.length; i++) {
                    if(i == 0) {
                        resultTableData += temp2[i];
                    } else {
                        resultTableData += Constant.SPLIT;
                        resultTableData += temp2[i];
                    }
                }
                resultTableData += "\r\n";
            }
            /*System.out.println(resultTableData);*/
            Util.rewriteDataToTable(tablePath, resultTableData);
            Util.showInTextArea(sql, Prompt.UPDATE_SUCCESS);
        }
    }

    /**
     * 带有where子句的update操作
     */
    public static void updateWithCondition() {
        String[] temp = natures.split("=");
        column = temp[0].trim();
        value = temp[1].trim();
        //检查列是否存在
        if(!checkColumnExsitOfUpdate()) {
            Util.showInTextArea(sql, Error.COLUMN_NOT_EXSIT + " ： " + column +" in " + tableName);
            return;
        }
        try {
            //找到要修改的列的下标
            List<String> natureArr = new LinkedList<String> ();
            JSONArray items = Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName).getJSONArray("items");
            for(int i=0; i<items.length(); i++) {
                String temp1 = items.getJSONObject(i).getString("nature");
                natureArr.add(temp1);
            }
            indexOfNature = 0;
            Iterator<String> iterator = natureArr.iterator();
            while(iterator.hasNext()) {
                indexOfNature++;
                String temp2 = (String)iterator.next();
                if(temp2.equals(column)) {
                    break;
                }
            }
            //解析condition
            String[] temp3 = condition.split("=");
            columnOfCondition = temp3[0].trim();
            valueOfCondition = temp3[1].trim();
            //根据where的条件找到符合条件的记录的集合，形成一个数组
            String tablePath = Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+tableName+".sql";
            String tableData = Util.readData(tablePath);
            String[] temp4 = tableData.split("\r\n");
            for(int i=0; i<temp4.length; i++) {
                String[] temp5 = temp4[i].split(Constant.SPLIT);
                for(int j=0; j<temp5.length; j++) {
                    if(j+1 == indexOfNature) {
                        indexOfRecord = i;
                    }
                }
            }
            //执行
            for(int i=0; i<temp4.length; i++) {
                if(i == indexOfRecord) {
                    String[] temp5 = temp4[i].split(Constant.SPLIT);
                    for(int j=0; j<temp5.length; j++) {
                        if(j+1 == indexOfNature) {
                            temp5[j] = value;
                            break;
                        }
                    }
                    temp4[i] = "";
                    for(int j=0; j<temp5.length; j++) {
                        if(j == 0) {
                            temp4[i] += temp5[j];
                        } else {
                            temp4[i] += Constant.SPLIT + temp5[j];
                        }
                    }
                }
            }
            String tableResult = "";
            for(int i=0; i<temp4.length; i++) {
                tableResult += temp4[i];
                tableResult += "\r\n";
            }
            //将更新后的数据重新写入数据文件中
            Util.rewriteDataToTable(tablePath, tableResult);
            Util.showInTextArea(sql, Prompt.UPDATE_SUCCESS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查列是否存在
     * @return
     */
    public static boolean checkColumnExsitOfUpdate() {
        try {
            boolean columnExsit = false;
            JSONArray items = Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName).getJSONArray("items");
            List<String> natureArr = new LinkedList<String> ();
            for(int i=0; i<items.length(); i++) {
                natureArr.add(items.getJSONObject(i).getString("nature"));
            }
            Iterator<String> iterator = natureArr.iterator();
            int index = 0;
            while(iterator.hasNext()) {
                index++;
                String tempNature = (String)iterator.next();
                if(tempNature.equals(column)) {
                    columnExsit = true;
                    break;
                }
            }
            indexOfNature = index;
            return columnExsit;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean checkDeleteGram() {
        String match = "delete from ([a-z0-9_]+) where (.+)";
        pattern = Pattern.compile(match);
        matcher = pattern.matcher(sql);
        if(matcher.matches()) { //带where条件的delete
            tableName = matcher.group(1);
            condition = matcher.group(2);
            return true;
        } else { //不带where条件的delete
            match = "delete from ([a-z0-9_]+)";
            pattern = Pattern.compile(match);
            matcher = pattern.matcher(sql);
            if(matcher.matches()) {
                tableName = matcher.group(1);
                return true;
            }
        }
        return false;
    }

    /**
     * 不带where限定，删除所有行
     */
    public static void deleteWithoutCondition() {
        //先删除表文件再重新创建表文件，在数据字典中更新表的大小
        Util.deleteFile(Constant.PATH_ROOT + "/" +Constant.currentDatabaseName, tableName + ".sql");
        Util.createFile(Constant.PATH_ROOT + "/" + Constant.currentDatabaseName,tableName + ".sql" );
        try {
            Util.showInTextArea(sql, Prompt.DELETE_SUCCESS + ":" +Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName).getInt("size")+" rows deleted!");
            Util.updateTableSize(tableName, 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 带有where限定，删除部分行
     */
    public static void deleteWithCondition() throws JSONException {
        tableBeforeSize = 0;
        //delete from 表名  where 列=值
        String splitCondition[] = condition.split("=");
        column = splitCondition[0].trim();
        value = splitCondition[1].trim();
        //判断列是否存在
        if(!checkColumnExsitOfDelete()) {
            Util.showInTextArea(sql,Error.COLUMN_NOT_EXSIT + " : " + column + " in " + tableName);
            return;
        }
        //执行
        //找到下标为value的，删除
        String tableDataPath = Constant.PATH_ROOT + "/" + Constant.currentDatabaseName + "/" + tableName + ".sql";
        String tableData = Util.readData(tableDataPath);
        String[] tableDataArr = tableData.split("\r\n");
        List<String[]> colValList = new LinkedList<String[]>();
        for(String temp : tableDataArr) {
            String[] tempArr = temp.split(Constant.SPLIT);
            colValList.add(tempArr);
        }
        //找到 列=值的那条数据，删除
        Iterator iterator = colValList.iterator();
        while(iterator.hasNext()) {
            String[] recordArr = (String[]) iterator.next();
            if(recordArr[valuePositionInTable].equals(value)) {
                iterator.remove();
            }
            tableBeforeSize++;
        }
        //将LinkedList转成StringListToString
        String tableString = ListToString(colValList);
        System.out.println(tableString);
        //删除成功后，重新写入table文件中，覆盖写入
        Util.rewriteDataToTable(Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+tableName+".sql", tableString);
        //更新表的大小
        Util.updateTableSize(tableName, tableAfterSize);
        System.out.println("before size: " + tableBeforeSize);
        System.out.println("after size: "+ tableAfterSize);
        Integer affecedSize = tableBeforeSize - tableAfterSize;
        Util.showInTextArea(sql, Prompt.RECORD_DELETE_SUCCESS + " : " + affecedSize.toString() +" rows affected in table " +tableName);
    }


    /**
     * 判断列是否存在（结构）
     * @return
     */
    public static boolean checkColumnExsitOfDelete() {
        try {
            boolean isColumnExsit = false;
            valuePositionInTable = 0;
            items = Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName).getJSONArray("items");
            for(int i=0;i < items.length();i++) {
                valuePositionInTable++;
                if(items.getJSONObject(i).getString("nature").equals(column)) {
                    isColumnExsit = true;
                    break;
                }
            }
            if(isColumnExsit == false) {
                valuePositionInTable = 0;
            }
            return isColumnExsit;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 链表转成table存储格式的String
     * @param colValList
     * @return
     */
    public static String ListToString(List<String[]> colValList) {
        tableAfterSize = 0;
        String str = "";
        Iterator iterator = colValList.iterator();
        while(iterator.hasNext()) {
            String[] recordArr = (String[]) iterator.next();
            for(int i=0;i<recordArr.length;i++) {
                if(i==0) {
                    str += recordArr[i];
                } else {
                    str += Constant.SPLIT + recordArr[i];
                }
            }
            str += "\r\n";
            tableAfterSize++;
        }
        return str;
    }


}
