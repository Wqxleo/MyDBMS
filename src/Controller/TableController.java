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
    public static String subQuery = null;
    public static String natures2 = null;
    public static String setIdentify = null;
    public static String table2 = null;

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
        if(alldata == null){
            return;
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
        // 获得投影数据
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
        String[] tableArr = tableName.split(",");
        System.out.println(tableName);
        String linkTable1 = tableArr[0].trim();
        String linkTable2 = tableArr[1].trim();
        //分离
        String[] tableAndNature = condition.split("=");
        String[] temp1 = tableAndNature[0].split("\\.");
        String tableName1 = temp1[0].trim(); //t1
        String tableNature1 = temp1[1].trim(); //id
        String[] temp2 = tableAndNature[1].split("\\.");
        String tableName2 = temp2[0].trim(); //t2
        String tableNature2 = temp2[1].trim(); //id
        if(!tableNature1.equals(tableNature2)) {
            Util.showInTextArea(sql, Error.COMMAND_ERROR);
            return;
        }
        //找到tableNature1和tableNature2在数据中的顺序下标
        int tableNature1Index = 0; //表示第几个，从1开始
        int tableNature2Index = 0; //表示第几个,从1开始
        try {
            JSONArray items1 = Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName1).getJSONArray("items");
            JSONArray items2 = Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName2).getJSONArray("items");
            for(int i=0; i<items1.length(); i++) {
                tableNature1Index++;
                String natureName = items1.getJSONObject(i).getString("nature");
                if(natureName.equals(tableNature1)) {
                    break;
                }
            }
            for(int i=0; i<items2.length(); i++) {
                tableNature2Index++;
                String natureName = items2.getJSONObject(i).getString("nature");
                if(natureName.equals(tableNature2)) {
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //取数据
        String linkTable1Path = Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+linkTable1+".sql";
        String linkTable2Path = Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+linkTable2+".sql";
        System.out.println(linkTable1);
        String linkTable1Data = Util.readData(linkTable1Path);
        String linkTable2Data = Util.readData(linkTable2Path);
        String[] linkTable1DataRecordArr = linkTable1Data.split("\r\n");
        String[] linkTable2DataRecordArr = linkTable2Data.split("\r\n");
        List<String[]> table1List = new LinkedList<String[]>(); //表1中的数据
        List<String[]> table2List = new LinkedList<String[]> (); //表2中的数据
        String table1NatureValue = null;
        for(int i=0; i<linkTable1DataRecordArr.length;i++) {
            String[] temp = linkTable1DataRecordArr[i].split(Constant.SPLIT);
            table1List.add(temp);
        }

        for(int i=0; i<linkTable2DataRecordArr.length;i++) {
            String[] temp = linkTable2DataRecordArr[i].split(Constant.SPLIT);
            table2List.add(temp);
        }
        List<String[]> newLinkedTable = new LinkedList<String[]> (); //连接后的新表
        Iterator<String[]> iterator1 = table1List.iterator();
        Iterator<String[]> iterator2 = table2List.iterator();
        while(iterator1.hasNext()) {
            String[] record1 = (String[])iterator1.next();
            while(iterator2.hasNext()) {
                String[] record2 = (String[])iterator2.next();
                if(record1[tableNature1Index - 1].equals(record2[tableNature2Index - 1])) { //相等 这条数据可以连接
                    String[] temp = Util.linkTwoStringArr(record1, record2, record1[tableNature1Index - 1]);
                    for(String nmb : temp) {
                        System.out.print(nmb + " ");
                    }
                    System.out.println();
                    newLinkedTable.add(temp);
                }
            }
        }
        String head = "";
        try {
            JSONArray items1 = Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName1).getJSONArray("items");
            JSONArray items2 = Constant.currentDatabase.getJSONObject("table").getJSONObject(tableName2).getJSONArray("items");
            for(int i=0; i<items1.length(); i++) {
                head += items1.getJSONObject(i).getString("nature") + "\t\t";
            }
            for(int i=0; i<items2.length(); i++) {
                if(!items1.getJSONObject(i).getString("nature").equals(tableNature1)) {
                    head += items2.getJSONObject(i).getString("nature") + "\t\t";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String result = Util.parseListToOutput(newLinkedTable);
        Util.showInTextArea(sql, head + "\n" + result + "total : " + newLinkedTable.size());
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




}
