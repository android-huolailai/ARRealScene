package com.demo.arrealscene;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.demo.arrealscene.adapter.PoiListAdapter;
import com.demo.arrealscene.location.LocationHelper;
import com.demo.arrealscene.sensor.SensorHelper;
import com.demo.arrealscene.util.ToastUtil;
import com.demo.arrealscene.view.FloatView;
import com.demo.arrealscene.view.RadarView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import butterknife.ButterKnife;

/**
 * Created by Be on 2017/1/23.
 * AR导航界面
 */

public class ARNavigation extends AppCompatActivity
        implements LocationHelper.OnLocationListener,PoiSearch.OnPoiSearchListener, SensorHelper.OnSensorListener{
    private FrameLayout poiContent;
    private ArrayList<FloatView> floatViewList = new ArrayList<>(); //存放悬浮View的集合
    private ArrayList<Integer> angle = new ArrayList<>();  //存放返回poi点的角度
    private ArrayList<PoiItem> pois;    //存放poi集合
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private LatLonPoint centerpoint;//= new LatLonPoint(30.230867, 120.189743);
    private RadarView radarView;
    private float x;
    private float y;
    private float z;
    private float fixedZ;
    private float fixedX1;
    private float tempx;
    //记录控件高
    private int viewHeight = 130;
    //存放屏幕宽高
    private int width;
    private int height;
    private DrawerLayout dl;
    private LinearLayout rightContent;
    private RecyclerView poiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
    }
    /**
     * 初始化V
     */
    private void initView(){
        dl = (DrawerLayout) findViewById(R.id.dl);
        rightContent = (LinearLayout) findViewById(R.id.rightContent);
        poiList = (RecyclerView) findViewById(R.id.poiList);

        //初始化定位
        initLocator();
        //初始化AMap对象&注册监听
        init();
    }
    /**
     * 初始化高德定位
     */
    private void initLocator(){
        LocationHelper.sharedInstance(this).setOnLocationListener(this);
        LocationHelper.sharedInstance(this).startLocation();
    }
    /**
     * 初始化AMap对象&注册监听
     */
    private void init() {
        poiContent = (FrameLayout) findViewById(R.id.poiContent);   //悬浮view容器
        //将poicontent右移400像素以校准
        //poiContent.setTranslationX(8640);
        dl.setOnClickListener(new onClickListenr());
        rightContent.setOnClickListener(new onClickListenr());
        radarView = (RadarView) findViewById(R.id.radarView);
        radarView.setOnClickListener(new onClickListenr());
        //editQuery = (EditText) findViewById(R.id.editQuery);

        //得到屏幕宽高
        WindowManager wm = this.getWindowManager();
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();

        SensorHelper.getSensorHelper(this).setOnSensorListener(this);
        poiContent.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                for (FloatView floatView : floatViewList) {
                    floatView.setSelectedState(false);
                    floatView.setAlpha(0.8f);
                }
            }
        });
        dl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }
    @Override
    public void locationSuccess(AMapLocation aMapLocation) {
        System.out.println("locationSuccess......");
        startSearchQuery(aMapLocation);
    }
    @Override
    public void locationFailed(AMapLocation aMapLocation) {

    }
    /**
     * * 开始进行poi搜索
     */
    protected void doSearchQuery(String keyWord) {
        //清空悬浮view
        for (FloatView floatView : floatViewList) {
            poiContent.removeView(floatView);
        }
        floatViewList.clear();
        angle.clear();
        int currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", "深圳市");
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.setBound(new PoiSearch.SearchBound(centerpoint, 1000));//设置周边搜索的中心点以及半径
        poiSearch.searchPOIAsyn();// 异步搜索
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        pois = poiResult.getPois();
        radarView.clearPOI();//清空poi
        if (poiResult == null || poiResult.getPois().size() == 0) {
            Toast.makeText(ARNavigation.this, "没有结果，请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
         //设置布局管理器
        poiList.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        poiList.setAdapter(new PoiListAdapter(this, pois));
        //设置最远距离
        radarView.setMaxDistance(pois.get(pois.size() - 1).getDistance());
        for (final PoiItem poi : pois) {
            System.out.println("距离：   " + poi.getDirection() + poi.getDistance() + "==============");
            double x1 = (poi.getLatLonPoint().getLongitude());
            double x2 = (poi.getLatLonPoint().getLatitude());
            double y1 = (centerpoint.getLongitude());
            double y2 = (centerpoint.getLatitude());
            System.out.println("  poi点：" + x1 + "==" + x2 + "==\n 中心点：" + y1 + "==" + y2 + "");
            RadarView.MyLatLng A = new RadarView.MyLatLng(x1, x2);
            RadarView.MyLatLng B = new RadarView.MyLatLng(y1, y2);
            int poiAngle = (int) RadarView.getAngle(A, B);
            System.out.println("角度：==" + poiAngle);
            radarView.addPoint(poi, centerpoint);
            System.out.println("==============================================================================");
            //初始化悬浮view位置，根据角度设置left， 并把角度20度之内的poi实现层叠
            int left = (int) ((((float) (360 - RadarView.getAngle(A, B)) / (float) 360)) * -width * 6) + 2160;
            int top = getTop(poiAngle) - 500;
            System.out.println("left" + left + "top:" + top);
            FloatView floatView = new FloatView(this, poi, left, top);
            //floatView.floatview.setOnClickListener(new FloatOnclickListener());
            floatView.setOnCheckedListener(new CheckedListener());
            System.out.println("tttttt" + poi.getTypeCode() + poi.getTypeDes());
            angle.add(poiAngle);
            floatViewList.add(floatView);
            poiContent.addView(floatView);
            System.out.println("评分：" + poi.getPoiExtension().getmRating());
        }
    }
    /**
     * 计算控件的top属性
     *
     * @param angle1
     * @return
     */
    private int getTop(int angle1) {
        int top = 0;
        int count = 0;
        for (int i = floatViewList.size() - 1; i >= 0; i--) {
            if (Math.abs(angle1 - angle.get(i)) < 20) {
                count++;
                top = count * viewHeight;
            }
        }
        return top;
    }
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
        System.out.println(poiItem.getAdName() + "=====");
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        radarView.unregisterListenter();
        //销毁定位客户端之后，若要重新开启定位请重新New一个AMapLocationClient对象。
    }
    /**
     * ARCode#######################################################################################################
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        z = event.values[2];
        y = event.values[1];
        x = event.values[0];
        double fixedX = x;
        if (y <= 0 && z >= 0) {                                  // 第一象限
            if (y == 0) {
                fixedX += 90;
            } else {
                float radio = Math.abs(z / y);
                fixedX += Math.atan(radio) * 180 / Math.PI;
            }
        } else if (y >= 0 && z >= 0) {                             // 第二象限
            if (y == 0) {
                fixedX += 90;
            } else {
                float radio = Math.abs(z / y);
                fixedX += 90 - Math.atan(radio) * 180 / Math.PI + 90;
            }
        } else if (y >= 0 && z <= 0) {                             // 第三现象
            if (y == 0) {
                fixedX += 90 + 180;
            } else {
                float radio = Math.abs(z / y);
                fixedX += Math.atan(radio) * 180 / Math.PI + 180;
            }
        } else {                                              // 第四现象
            float radio = Math.abs(z / y);
            fixedX += 90 - Math.atan(radio) * 180 / Math.PI + 270;
        }
        while (fixedX > 360) fixedX -= 360;
        while (fixedX < 0) fixedX += 360;
       /* double targetX = event.values[0] + Math.cos(y / 180 * Math.PI) * z + (y > 0 ? y / 0.5 : 0);
        if (targetX > 360) targetX -= 360;*/
        //x = (float) (event.values[0] + Math.cos(y / 180 * Math.PI) * z + (y > 0 ? y / 0.5 : 0));
        //if (x > 360) x -= 360;
        fixedZ = (180 - (z + 90)) + 270;
        double temY = -(Math.cos(z / 180 * Math.PI) * (-Math.abs(y)));
        double banlanceZ = Math.abs(z);
        double fixedY = temY / 90 * height - 0.5f * height;
        double fixed_Y_L = temY / 90 * width + width / 3;
        //System.out.println(fixedY + ":::::" + fixed_Y_L + "：：：：" + banlanceZ);
        fixedY = banlanceZ / 90 * fixed_Y_L + (90 - banlanceZ) / 90 * fixedY;
        //手机倒置的情况
        if (y > 0) {
            fixedZ = 90 + 90 - fixedZ;
        }
        //fixedY=y;
        //修正Z
        //System.out.println(z + "----");
        //改变所有悬浮view的位置
        if (floatViewList.size() > 0) {
            for (int i = 0; i < floatViewList.size(); i++) {
                //做修正拼接处理,解决X指向360和0度的交界处引起的显示异常，
                if (fixedX < angle.get(i)) {
                    tempx = (float) (fixedX + 360);
                } else {
                    tempx = (float) fixedX;
                }
                //float fixedX = (tempx / (360 - 22.5f)) * 1080 - 540;
                //floatViewList.get(i).setTranslationX((1080 - fixedX * 8) + 4320);
                fixedX1 = (width - ((tempx / (360 - 22.5f)) * width - width / 2) * 6) + width * 6;
                float zz = 90 - Math.abs(event.values[2]);
              /*  if (Math.abs(z)<80) {
                    floatViewList.get(i).setTranslationY(fixedY * zz / 80f);
                }*/
                floatViewList.get(i).setTranslationY((float) fixedY);
                //      System.out.println(fixedY);
                floatViewList.get(i).setRotation(fixedZ);
                poiContent.setRotation(360 - fixedZ);
                floatViewList.get(i).setTranslationX(fixedX1);
            }
        }
    }
    /**
     * 定位成功，搜索周边
     */
    private void startSearchQuery(AMapLocation aMapLocation){
        //获取当前位置经纬度
        if (centerpoint == null) {
            double latitude = aMapLocation.getLatitude();
            double Longitude = aMapLocation.getLongitude();
            //centerpoint = new LatLng(latitude, Longitude);
            centerpoint = new LatLonPoint(latitude, Longitude);
            //第一次搜索初始化控件高度
            doSearchQuery("超市");

            System.out.println("位置发生改变：" + aMapLocation.getAddress() + "街道：" +
                    aMapLocation.getStreet() + "地区：" + aMapLocation.getDistrict() + "城市编码" +
                    aMapLocation.getCityCode() + "地区编码：" +
                    aMapLocation.getAdCode());
        }
    }
    /**
     * 返回控件高度
     *
     * @param view
     * @return
     */
    public int getHW(final View view) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                viewHeight = view.getHeight();
            }
        });
        int height = viewHeight;
        return height;
    }
    /**
     * 悬浮view的点击逻辑
     */
    class FloatOnclickListener implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            if (((FloatView) ((v.getParent()))).getChildAt(0) == v) {
                Toast.makeText(ARNavigation.this, "ssss", Toast.LENGTH_SHORT).show();

            }
            if (((FloatView) ((v.getParent()))).getSelectedState()) {
                for (FloatView floatView : floatViewList) {
                    floatView.setSelectedState(false);
                    floatView.setAlpha(0.8f);
                }
                return;
            }
            for (FloatView floatView : floatViewList) {
                if ((((v.getParent()))) == floatView) {
                    floatView.setSelectedState(true);
                } else {
                    floatView.setSelectedState(false);
                }
            }
        }
    }
    /**
     * 自定义单击事件
     */
    class onClickListenr implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.dl:
                    ToastUtil.show(ARNavigation.this, "clickdl");
                    break;
                case R.id.radarView:
                    ToastUtil.show(ARNavigation.this, "showlist");
                    dl.openDrawer(Gravity.RIGHT);
                    break;
                case R.id.rightContent:
                    ToastUtil.show(ARNavigation.this, "list");
                    break;
            }
        }
    }

    //自定义FloatView监听
    class CheckedListener implements FloatView.OnCheckedListener {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onCehcked(PoiItem poiItem, FloatView floatView) {
            Toast.makeText(ARNavigation.this, "执行回调" + poiItem.getTitle(), Toast.LENGTH_SHORT).show();
            if (poiItem.getPhotos().size() > 0) {
                Picasso.with(ARNavigation.this).load(poiItem.getPhotos().get(0).getUrl()).into(floatView.getPoiImg());
            }
            //如果floatview已经被选中，点击时释放所有floatview
            if (floatView.getSelectedState()) {
                for (FloatView f : floatViewList) {
                    f.setSelectedState(false);
                    f.setAlpha(0.8f);
                }
                return;
            }
            for (FloatView floatView1 : floatViewList) {
                if (floatView == floatView1) {
                    floatView1.setSelectedState(true);

                } else {
                    floatView1.setSelectedState(false);
                }
            }
        }
    }
}

