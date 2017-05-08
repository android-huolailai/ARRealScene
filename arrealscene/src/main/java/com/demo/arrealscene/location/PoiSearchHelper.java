package com.demo.arrealscene.location;

import android.content.Context;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.poisearch.PoiSearch;

/**
 * Created by yixiaofei on 2017/3/28 0028.
 */

public class PoiSearchHelper{

    private static PoiSearchHelper poiSearcheHelper;

    private PoiSearch poiSearch;

    private  PoiSearch.Query searchQuery;

    public static synchronized PoiSearchHelper getPoiSearchHelper(){
            if(poiSearcheHelper==null){
                poiSearcheHelper = new PoiSearchHelper();
            }
            return poiSearcheHelper;
    }
    /**
     * 设置搜索条件
     * @param keyWords
     * @param cityCode
     * @param pageNum
     */
    public PoiSearch.Query getSearchQuery(String keyWords,String cityCode,int pageNum){
        searchQuery = new PoiSearch.Query(keyWords, "", cityCode);
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，
        //POI搜索类型共分为以下20种：汽车服务|汽车销售|
        //汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|
        //住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|
        //金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        searchQuery.setPageSize(30);// 设置每页最多返回多少条poiitem
        searchQuery.setPageNum(pageNum);//设置查询页码

        return searchQuery;
    }
    /**
     * 开始周边搜索
     * @param latLonPoint
     */
    public void startSearchBound(Context context,PoiSearch.Query searchQuery,LatLonPoint latLonPoint,PoiSearch.OnPoiSearchListener listener){
        poiSearch = new PoiSearch(context, searchQuery);
        poiSearch.setOnPoiSearchListener(listener);
        poiSearch.setBound(new PoiSearch.SearchBound(latLonPoint, 5000));//设置周边搜索的中心点以及半径
        poiSearch.searchPOIAsyn();
    }
}
