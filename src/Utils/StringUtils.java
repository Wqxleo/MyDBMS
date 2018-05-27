package Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by srg
 *
 * @date 2017/11/23
 */
public class StringUtils {

    //只能是一层括号
    public static String getStringInBracket(String string){
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find())
            return matcher.group();
        else
            return "";
    }

    public static int getNumByColumnName(String[] strings,String string){
        int j = 0;
        for(int i = 0;i < strings.length;i++){
            if(strings[i].startsWith(string)){
                j = i;
                break;
            }
        }
        return j;
    }

    public static int getNumOfColumnInCreate(String[] strings,String string){
        int j = 0;
        for(int i = 0;i < strings.length;i++){
            String[] strings1 = strings[i].split(" ");
            if(string.contains(strings1[0])){
                j = i;
                break;
            }
        }
        return j;
    }

    //判断字符串是否为数字
    public static boolean isNumeric(String str){
        for (int i = 0; i < str.length(); i++){
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    public static boolean equalsIgnoreCase(String a,String b){
        return a.toUpperCase().equals(b.toUpperCase());
    }

    public static boolean containsString(String a, String b){
        return a.toUpperCase().contains(b.toUpperCase());
    }
}
