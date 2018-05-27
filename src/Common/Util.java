package Common;

/**
 * Created by wangquanxiu at 2018/5/27 21:30
 */

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.util.Arrays;
        import java.util.Iterator;
        import java.util.LinkedList;
        import java.util.List;

        import UI.MainFrame;
        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;



public class Util {
    private static Object[] values;
    /**
     * 在窗口输出错误信息
     * @param sql
     * @param result
     */
	/*public static void showErrMsg(String errMsg) {
		MainFrame.output.append(errMsg);
	}*/
    public static void showInTextArea(String sql, String result) {
        result = "[" + Constant.currentDatabaseName + " @ " + Constant.currentUserName + "]>" + sql + ";\n" + result + "\n";
        MainFrame.output.append(result);
    }
    /**
     * 按字节写入数据
     * @param path
     * @param data
     * @return boolean
     */
    public static boolean writeData(String path, String data) {
        try {
            FileOutputStream output= new FileOutputStream(path);
            output.write(data.getBytes());
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    /**
     * 按字节从头读取数据
     * @param path
     * @return String
     */
    public static String readData(String path) {
        String readStr = "";
        try {
            FileInputStream input = new FileInputStream(path);
            int hasRead = 0;
            byte[] bbuf = new byte[1024];
            while((hasRead = input.read(bbuf)) > 0) {
                readStr += new String(bbuf, 0, hasRead);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readStr;
    }
    /**
     * 数组转字符串
     * @param arr
     * @return
     */
    public static String arrayToString(String[] arr) {
        String str = "";
        for(int i=0; i<arr.length; i++) {
            str += arr[i];
            str += " ";
        }
        return str.trim();
    }
    /**
     * 生成文件夹
     * @param path
     * @param folderName
     */
    public static void createFolder(String path, String folderName) {

    }
    /**
     * 生成文件
     * @param path
     * @param fileName
     */
    public static void createFile(String path, String fileName) {
        path = path + "/" + fileName;
        File file = new File(path);
        try {
            if(!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 从数据表读取所有的数据
     *
     * @param table
     * @return
     */
    public static List<String[]> readDataFromTable(String table) {
        List<String[]> list = new LinkedList<String[]>();
        String path = Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+table+".sql";
        String tableData = Util.readData(path);
        String[] tableDataArr = tableData.split("\r\n");
        for(String temp : tableDataArr) {
            String[] tempArr = temp.split(Constant.SPLIT);
            list.add(tempArr);
        }
        return list;
    }
    /**
     * 获得属性的位置
     *
     * @param table
     * @param nature
     * @return
     */
    public static int getNaturePosition(String table, String nature, boolean isview) {
        try {
            List<String> list = getNaturesList(table, isview);
            return list.indexOf(nature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    /**
     * 获得所有属性
     *
     * @param table
     * @return
     */
    public static String[] getNaturesArray(String table, boolean isview) {
        try {
            net.sf.json.JSONArray arr = null;
            if (isview) {
                arr = Constant.currentDatabase.getJSONObject("view").getJSONObject(table).getJSONArray("items");
            } else {
                arr = Constant.currentDatabase.getJSONObject("table").getJSONObject(table).getJSONArray("items");
            }
            String[] result = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = arr.getJSONObject(i).getString("nature");
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获得所有属性
     *
     * @param table
     * @return
     */
    public static String getNaturesString(String table, boolean isview) {
        try {
            String str = "";
            net.sf.json.JSONArray arr;
            if (isview) {
                arr = Constant.currentDatabase.getJSONObject("view").getJSONObject(table).getJSONArray("items");
            } else {
                arr = Constant.currentDatabase.getJSONObject("table").getJSONObject(table).getJSONArray("items");
            }
            for (int i = 0; i < arr.size(); i++) {
                str += arr.getJSONObject(i).getString("nature") + "\t\t";
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获得属性的数据类型
     *
     * @param table
     * @param nature
     * @param isview
     * @return
     */
    public static String getNatureType(String table, String nature, boolean isview) {
        int pos = getNaturePosition(table, nature, isview);
        if (pos != -1) {
            net.sf.json.JSONArray arr;
            if (isview)
                arr = Constant.currentDatabase.getJSONObject("view").getJSONObject(table).getJSONArray("items");
            else
                arr = Constant.currentDatabase.getJSONObject("table").getJSONObject(table).getJSONArray("items");
            return arr.getJSONObject(pos).getString("type");
        }
        return null;
    }
    /**
     * 链表转数组
     * @param list
     * @return
     */
    public static String[][] parseListToArray(List<String[]> list) {
        Iterator<String[]> it = list.iterator();
        int i = 0;
        String[][] arr = new String[list.size()][];
        while (it.hasNext()) {
            arr[i++] = it.next();
        }
        return arr;
    }
    /**
     * 数组转链表
     * @param array
     * @return
     */
    public static List<String[]> parseArrayToList(String[][] array) {
        List<String[]> list = new LinkedList<String[]>();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }
    /**
     * 检测所有的属性是否存在
     *
     * @param list
     * @param array
     * @return
     */
    public static String checkAllNatureExsit(List<String> list, String[] array) {
        for (int i = 0; i < array.length; i++) {
            if (!list.contains(array[i])) {
                return array[i];
            }
        }
        return null;
    }
    /**
     * 将链表装换成输出格式
     */
    public static String parseListToOutput(List<String[]> list) {
        String content = "";
        Iterator<String[]> it = list.iterator();
        while (it.hasNext()) {
            String[] array = it.next();
            if (array.length > 0) {
                content += array[0];
                for (int j = 1; j < array.length; j++) {
                    content += "\t\t" + array[j];
                }
                content += "\n";
            }
        }
        return content;
    }
    /**
     * 获得所有属性
     *
     * @param table
     * @param isview
     * @return
     */
    public static List<String> getNaturesList(String table, boolean isview) {
        try {
            List<String> list = new LinkedList<String>();
            net.sf.json.JSONArray arr;
            if (isview) {
                arr = Constant.currentDatabase.getJSONObject("view").getJSONObject(table).getJSONArray("items");
            } else {
                arr = Constant.currentDatabase.getJSONObject("table").getJSONObject(table).getJSONArray("items");
            }
            for (int i = 0; i < arr.size(); i++) {
                list.add(arr.getJSONObject(i).getString("nature"));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 检查主键约束
     * *@param sql
     * @param table
     * @param value
     * @param isview
     * @return
     */
    public static boolean checkPrimaryKeyLimit(String table, String[] value, boolean isview) {
        //insert into t1 values(1,'hh',2) | value[]中为 插入的数据 如 1 'hh' 2
        boolean primaryKeyLimit = true; //是否符合主键约束
        boolean hasPrimaryKey = false;
        List<String> primaryKeyValueInTableData = new LinkedList<String> ();
        //获取数据字典中的字段的约束，赋值给primaryKeyValidity
        net.sf.json.JSONArray items = Constant.currentDatabase.getJSONObject("table").getJSONObject(table).getJSONArray("items");
        List<String> limitArr = new LinkedList<String>();
        for(int i=0; i<items.size(); i++) {
            net.sf.json.JSONObject temp = items.getJSONObject(i);
            //System.out.println("JSON对象" + temp.toString());
            if(temp.has("limit")) {
                String tempLimit = temp.getString("limit");
                limitArr.add(tempLimit);
            } else {
                limitArr.add("");
            }
        }
        int indexOfValue = -1;
        Iterator<String> iterator = limitArr.iterator();
        while(iterator.hasNext()) { //遍历看是否存在主键约束
            indexOfValue++;
            if(((String)iterator.next()).equals("primary key")) {
                hasPrimaryKey = true;
                break;
            }
        }
        if(hasPrimaryKey == true) {  //如果存在primary key约束
            //从表文件中取出primary key的值
            String tableDataPath = Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+table+".sql";
            String tableData = Util.readData(tableDataPath);
            if(tableData == null || tableData.equals("") || tableData.matches("[\\s]+")) { //数据文件为空白时，符合约束
                primaryKeyLimit = true;
            } else {
                String[] tableDataArr = tableData.split("\r\n");
                for(String temp : tableDataArr) {
                    String[] tempArr = temp.split(Constant.SPLIT);
                    primaryKeyValueInTableData.add(tempArr[indexOfValue]);
                }
                Iterator<String> it = primaryKeyValueInTableData.iterator();
                while(it.hasNext()) {
                    //System.out.println("表中的主键值的数量： " +primaryKeyValueInTableData.size());
                    String temp = (String)it.next();
                    //System.out.println("表中的 ：" + temp);
                    //System.out.println("插入的 ：" + value[indexOfValue]);
                    //System.out.println();
                    if(temp.equals(value[indexOfValue])) {
                        primaryKeyLimit = false;
                        break;
                    }
                }
            }

        } else { //不存在主键约束，可认为符合主键约束，不报错
            primaryKeyLimit = true;
        }
        return primaryKeyLimit;
    }
    /**
     * 检查非空约束
     ** *@param sql
     * @param table
     * @param value
     * @param isview
     * @return
     */
    public static boolean checkNotNullLimit(String table, String[] value, boolean isview) {
        //insert into t1 values(1,'hh',2) | value[]中为 插入的数据 如 1 'hh' 2
        boolean notNullLimit = true; //是否符合非空约束
        boolean hasNotNullLimit = false;
        //List<String> notNullValueInTableData = new LinkedList<String> (); //存放的是not null那一列数据本身，不是存放定义
        try {
            //获取数据词典中的字段的约束，赋值给notNullValueInTableData
            net.sf.json.JSONArray items = Constant.currentDatabase.getJSONObject("table").getJSONObject(table).getJSONArray("items");
            List<String> limitArr = new LinkedList<String>();
            for(int i=0; i<items.size(); i++) {
                net.sf.json.JSONObject temp = items.getJSONObject(i);
                if(temp.has("limit")) {
                    String tempLimit = temp.getString("limit");
                    limitArr.add(tempLimit);
                } else {
                    limitArr.add("");
                }

            }
            int indexOfValue = -1;
            Iterator iterator = limitArr.iterator();
            while(iterator.hasNext()) { //遍历看是否存在not null约束
                indexOfValue++;
                if(((String)iterator.next()).equals("not null")) {
                    hasNotNullLimit = true;
                    break;
                }
            }
            if(hasNotNullLimit == true) { //如果存在not null约束,检查约束是否正确
                //同时遍历limitArr和value[]，查看每一个元素是否符合非空
                int i = 0;
                Iterator<String> it = limitArr.iterator();
                while(it.hasNext()) {
                    if(((String)it.next()).equals("not null")) {
                        if(value[i] == "") {
                            notNullLimit = false;
                            break;
                        }
                    }
                    i++;
                }
                return notNullLimit;
            } else { //不存在not null约束，可认为符合not null约束，不报错
                notNullLimit = true;
            }
            return notNullLimit;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     *检查唯一约束
     * *@param sql
     * @param table
     * @param value
     * @param isview
     * @return
     */
    public static boolean checkUniqueLimit(String table, String[] value, boolean isview) {
        //insert into t1 values(1,'hh',2) | natures[]为数据字典中列名 | value[]中为 插入的数据 如 1 'hh' 2
        boolean uniqueLimit = true; //是否符合唯一约束
        boolean hasUniqueLimit = false;
        List<String> uniqueValueInTableData = new LinkedList<String> ();
        //获取数据字典中的字段的约束，赋值给uniqueValueInTableData
        net.sf.json.JSONArray items = Constant.currentDatabase.getJSONObject("table").getJSONObject(table).getJSONArray("items");
        List<String> limitArr = new LinkedList<String>();
        for(int i=0; i<items.size(); i++) {
            net.sf.json.JSONObject temp = items.getJSONObject(i);
            //System.out.println("JSON对象" + temp.toString());
            if(temp.has("limit")) {
                String tempLimit = temp.getString("limit");
                limitArr.add(tempLimit);
            } else {
                limitArr.add("");
            }
        }
        int indexOfValue = -1;
        Iterator iterator = limitArr.iterator();
        while(iterator.hasNext()) { //遍历看是否存在唯一约束
            indexOfValue++;
            if(((String)iterator.next()).equals("unique")) {
                hasUniqueLimit = true;
                break;
            }
        }
        if(hasUniqueLimit == true) {  //如果存在unique约束
            //从表文件中取出unique的值
            String tableDataPath = Constant.PATH_ROOT+"/"+Constant.currentDatabaseName+"/"+table+".sql";
            String tableData = Util.readData(tableDataPath);
            if(tableData == null || tableData.equals("") || tableData.matches("[\\s]+")) { //数据文件为空白时，符合约束
                uniqueLimit = true;
            } else {
                String[] tableDataArr = tableData.split("\r\n");
                for(String temp : tableDataArr) {
                    String[] tempArr = temp.split(Constant.SPLIT);
                    uniqueValueInTableData.add(tempArr[indexOfValue]);
                }
                Iterator<String> it = uniqueValueInTableData.iterator();
                while(it.hasNext()) {
                    String temp = (String)it.next();
                    if(temp.equals(value[indexOfValue])) {
                        uniqueLimit = false;
                        break;
                    }
                }
            }
        } else { //不存在唯一约束，可认为符合唯一约束，不报错
            uniqueLimit = true;
        }
        return uniqueLimit;
    }
    /**
     * 插入一条数据到表
     *
     * @param data
     * @param table
     */
    public static void insertDataToTable(String data, String table) {
        String path = Constant.PATH_ROOT + "/" + Constant.currentDatabaseName + "/" + table + ".sql";
        try {
            FileOutputStream output= new FileOutputStream(path,true);//第二个参数true表示追加，默认为false表示覆盖重写
            output.write(data.getBytes());
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 更新表的长度
     *
     * @param table
     * @param len
     */
    public static void updateTableSize(String table, int len) {
        Constant.currentDatabase.getJSONObject("table").getJSONObject(table).put("size", len);
        Util.writeData(Constant.PATH_DICTIONARY, Constant.DICTIONARY.toString());
    }
    /**
     * 删除指定路径的文件
     * @param path
     */
    public static boolean deleteFile(String path, String fileName) {
        path = path + "/" +fileName;
        try {
            File file = new File(path);
            file.delete();
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 覆盖表中内容重新写入数据
     * @param path
     * @param content
     * @return
     */
    public static boolean rewriteDataToTable(String path, String content) {
        try {
            FileOutputStream output= new FileOutputStream(path);
            output.write(content.getBytes());
            output.flush();
            output.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 通过字符串形式的json对象获取所有的key
     * @param jsonString
     * @return
     */
    public static List<String> getUserJsonKeyByString(String jsonString) {
        List<String> jsonKey = new LinkedList<String>();
        jsonString = jsonString.trim();
        jsonString = jsonString.substring(1);
        jsonString = jsonString.substring(0, jsonString.length() - 1);
        String[] jsonObjectArr = jsonString.split(",");
        //取下标的偶数
        for(int i=0; i<jsonObjectArr.length; i=i+2) {
            String[] temp = jsonObjectArr[i].split(":");
            for(int j=0;j<temp.length;j=j+3) {
                temp[j] = temp[j].substring(1);
                temp[j] = temp[j].substring(0, temp[j].length() - 1);
                jsonKey.add(temp[j]);
            }
        }
        return jsonKey;
    }
    /**
     * 将两个String[]拼接成一个String[]并去除重复项
     * @param arr1
     * @param arr2
     * *@param linkTableNature
     * @return
     */
    public static String[] linkTwoStringArr(String[] arr1, String[] arr2,String linkTableNatureValue) {
        int arr1Length = arr1.length;
        int arr2Length = arr2.length;
        arr1 = Arrays.copyOf(arr1, arr1Length+arr2Length - 1);//数组扩容
        for(int i=arr1Length,j=0;j<arr2Length;j++) {
            if(!arr2[j].equals(linkTableNatureValue)) {
                arr1[i] = arr2[j];
                i++;
            }
        }
        return arr1;
    }
}
