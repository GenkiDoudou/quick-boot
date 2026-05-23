package io.github.genkidoudou.web.hotel;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hotel {
  public static void main(String[] args) {

    Map<String, String> headers = new HashMap<>();
    headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36 Edg/148.0.0.0");

//    GetCityList(headers);

//    GetHotelList("北京", "447");

//    for (String s : FileUtil.readLines("E:/tmp/20260523/city.txt", "utf-8")) {
//      String[] split = s.split("\\|");
//      String Name = split[2];
//      String ID = split[1];
//      String ParentID = split[0];
//
//      System.out.println(Name + "," + ID + "," + ParentID);
//      GetHotelList(Name, ID);
//    }
    List<File> fileList = FileUtil.loopFiles("E:/tmp/20260523/", file -> file.getName().endsWith(".csv"));
    List<HotelData> hotelDatas = new ArrayList<>();
    for (File file : fileList) {
      String name = file.getName();
      String city = name.replace(".csv", "");
      System.out.println(name);
      List<HotelData> hotelData = saveToExcel2(file.getAbsolutePath());
      for (HotelData hotelDatum : hotelData) {
        hotelDatum.setCity(city);
      }
      hotelDatas.addAll(hotelData);
    }
        String outputPath = "E:/tmp/20260523/hotels.xlsx";
    EasyExcel.write(outputPath, HotelData.class)
      .sheet("酒店列表")
      .doWrite(hotelDatas);

    System.out.println("数据已导出到: " + outputPath);
    System.out.println("共导出 " + hotelDatas.size() + " 条酒店数据");
  }


// ... existing code ...

  public static void GetHotelList(String cityName, String cityId) {
    String url = "https://hotel.998.com/v1/api/HotelList/GetHotelList";
    Integer pageindex = 1;
    while (true) {
      try {
        Map<String, Object> params = new HashMap<>();
        params.put("pageindex", pageindex);
        params.put("cityId", cityId);
        params.put("cityName", cityName);
        params.put("activityJson", "[]");
        params.put("facilityjson", "[]");
        params.put("labeljson", "[]");
        params.put("brandjson", "[]");
        params.put("screeningJson", "[]");
        params.put("hotelSortRule", 1);
        params.put("checkintime", "2026-05-27");
        params.put("days", 2);
        params.put("pagesize", 10);
        params.put("xzId", "");
        params.put("keySearchJson", "");
        params.put("hotPointId", "");
        params.put("hotelcode", "");
        params.put("userId", "");
        params.put("keyValue", "");
        params.put("crtLng", "");
        params.put("crtLat", "");
        params.put("checkouttime", "2026-05-29");

        Map<String, String> headers = new HashMap<>();
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36 Edg/148.0.0.0");
        headers.put("Content-Type", "application/json");

        String jsonBody = JSONUtil.toJsonStr(params);
        String result = HttpUtil.createPost(url)
          .addHeaders(headers)
          .body(jsonBody)
          .execute()
          .body();

        System.out.println(result);
        JSONArray jsonArray = JSONUtil.parseObj(result).getJSONObject("data").getJSONArray("items");
        FileUtil.appendString(result + "\n", "E:/tmp/20260523/" + cityName + ".csv", "utf-8");
        if (CollectionUtil.isEmpty(jsonArray)) {
          break;
        }
        pageindex = pageindex + 1;
        try {
          Thread.sleep(1000 * RandomUtil.randomInt(5, 10));
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      } catch (RuntimeException e) {
        throw new RuntimeException(e);
      }
    }


  }

// ... existing code ...


  public static void GetCityList(Map<String, String> headers) {
    String url = "https://hotel.998.com/v1/api/HotelList/GetCityList";
    String body = HttpUtil.createGet(url)
      .addHeaders(headers)
      .execute().body();
    System.out.println(body);
    JSONArray CityList = JSONUtil.parseObj(body).getJSONObject("data").getJSONObject("China").getJSONArray("CityList");
    for (int i = 0; i < CityList.size(); i++) {
      JSONObject jsonObject = CityList.getJSONObject(i);
      String Name = jsonObject.getStr("Name");
      String ID = jsonObject.getStr("ID");
      String ParentID = jsonObject.getStr("ParentID");
      String line = ParentID + "|" + ID + "|" + Name;
      FileUtil.appendString(line + "\n", "E:/tmp/20260523/city.txt", "utf-8");
    }

  }


// ... existing code ...

  public static List<HotelData> saveToExcel2(String path) {
    List<HotelData> hotelDataList = new ArrayList<>();

    List<String> strings = FileUtil.readLines(path, "utf-8");
    for (String string : strings) {
      JSONArray items = JSONUtil.parseObj(string).getJSONObject("data").getJSONArray("items");
      if (CollectionUtil.isEmpty(items)) {
        continue;
      }

      for (int i = 0; i < items.size(); i++) {
        JSONObject jsonObject = items.getJSONObject(i);

        HotelData hotelData = new HotelData();
        hotelData.setName(jsonObject.getStr("name"));
        hotelData.setPrice(jsonObject.getStr("price"));
        hotelData.setLongitude(jsonObject.getStr("longitude"));
        hotelData.setLatitude(jsonObject.getStr("latitude"));
        hotelData.setAddressLandmark(jsonObject.getStr("AddressLandmark"));
        hotelData.setScoreText(jsonObject.getStr("ScoreText"));
        hotelData.setScore(jsonObject.getStr("score"));

        String HotelLable = jsonObject.getStr("HotelLable");
        String labelName = null;
        if (cn.hutool.core.util.StrUtil.isNotBlank(HotelLable)) {
          JSONArray parsedArray = JSONUtil.parseArray(HotelLable);
          if (CollectionUtil.isNotEmpty(parsedArray)) {
            labelName = parsedArray.getJSONObject(0).getStr("LabelName");
          }
        }
        hotelData.setLabelName(labelName);

        hotelDataList.add(hotelData);
      }
    }

    return hotelDataList;
//    String outputPath = "E:/tmp/20260523/hotels.xlsx";
//    EasyExcel.write(outputPath, HotelData.class)
//      .sheet("酒店列表")
//      .doWrite(hotelDataList);
//
//    System.out.println("数据已导出到: " + outputPath);
//    System.out.println("共导出 " + hotelDataList.size() + " 条酒店数据");
  }

  @Data
  public static class HotelData {
    @ExcelProperty(value = "城市", index = 0)
    private String city;

    @ExcelProperty(value = "酒店名称", index = 1)
    private String name;

    @ExcelProperty(value = "价格", index = 2)
    private String price;

    @ExcelProperty(value = "经度", index = 3)
    private String longitude;

    @ExcelProperty(value = "纬度", index = 4)
    private String latitude;

    @ExcelProperty(value = "地址", index = 5)
    private String addressLandmark;

    @ExcelProperty(value = "评分描述", index = 6)
    private String scoreText;

    @ExcelProperty(value = "评分", index = 7)
    private String score;

    @ExcelProperty(value = "标签", index = 8)
    private String labelName;
  }
}
