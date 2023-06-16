package com.quincy.weather;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WeatherUtil {
    final String[] HTML_HUABEI;
    final String[] HTML_DONGBEI;
    final String[] HTML_HUADONG;
    final String[] HTML_HUAZHONG;
    final String[] HTML_HUANAN;
    final String[] HTML_XIBEI;
    final String[] HTML_XINAN;
    final String[] HTML_HMT;
    public final String[][] ALL_AREAS;

    public WeatherUtil() throws IOException {
        HTML_HUABEI = getHTMLs("http://www.weather.com.cn/textFC/hb.shtml");
        HTML_DONGBEI = getHTMLs("http://www.weather.com.cn/textFC/db.shtml");
        HTML_HUADONG = getHTMLs("http://www.weather.com.cn/textFC/hd.shtml");
        HTML_HUAZHONG = getHTMLs("http://www.weather.com.cn/textFC/hz.shtml");
        HTML_HUANAN = getHTMLs("http://www.weather.com.cn/textFC/hn.shtml");
        HTML_XIBEI = getHTMLs("http://www.weather.com.cn/textFC/xb.shtml");
        HTML_XINAN = getHTMLs("http://www.weather.com.cn/textFC/xn.shtml");
        HTML_HMT = getHTMLs("http://www.weather.com.cn/textFC/gat.shtml");
        ALL_AREAS= new String[][]{HTML_HUABEI, HTML_DONGBEI,HTML_HUADONG,HTML_HUAZHONG,HTML_HUANAN,HTML_XIBEI,HTML_XINAN,HTML_HMT};
    }
    private ArrayList<String> getProvinces(String[] html,boolean full_mode){
        ArrayList<String> list=new ArrayList<>();
        String[] splitted=html[0].split("class=\"rowsPan\"");
        for (int i = 1; i < splitted.length; i++) {
            String currentString=splitted[i];
            if(!full_mode){
                int beginIndex = currentString.indexOf("target=\"_blank\">") + "target=\"_blank\">".length();
                int endIndex = currentString.indexOf("</a>");
                list.add(currentString.substring(beginIndex, endIndex));
            }else{
                list.add(currentString);
            }
        }
        return list;
    }
    public ArrayList<String> getAllProvinces(boolean full_mode){
        ArrayList<String> list=new ArrayList<>();
        for(String[] tmp:ALL_AREAS){
            ArrayList<String> tmpList=getProvinces(tmp,full_mode);
            list.addAll(tmpList);
        }
        return list;
    }
    private static String[] getHTMLs(String addr) throws IOException {
        URL url=new URL(addr);
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        InputStream is=connection.getInputStream();
        InputStreamReader reader=new InputStreamReader(is);
        StringBuilder builder=new StringBuilder();
        char[] buf=new char[1];
        while(reader.read(buf)!=-1){
            builder.append(buf);
        }
        reader.close();
        is.close();
        connection.disconnect();
        return builder.toString().split("<div class=\"conMidtab\" style=\"display:none;\">");
    }
    public static String getHTML(String addr) throws IOException {
        URL url=new URL(addr);
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        InputStream is=connection.getInputStream();
        InputStreamReader reader=new InputStreamReader(is);
        StringBuilder builder=new StringBuilder();
        char[] buf=new char[1];
        while(reader.read(buf)!=-1){
            builder.append(buf);
        }
        reader.close();
        is.close();
        connection.disconnect();
        return builder.toString();
    }
    public static String getMidString(String source,String pre,String post){
        int beginIndex=source.indexOf(pre)+pre.length();
        int endIndex=source.indexOf(post,beginIndex);
        return source.substring(beginIndex,endIndex);
    }
}