package cn.edu.cdut.navidemo3;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import cn.edu.cdut.navidemo3.databinding.ActivityScrollingBinding;
import cn.edu.cdut.navidemo3.CustomYValueFormatter;
import cn.edu.cdut.navidemo3.extra.data.BaseActivity;

public class ScrollingActivity extends BaseActivity {

    private ActivityScrollingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityScrollingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        //以下添加柱状图
        BarChart barChart = findViewById(R.id.barChart);
        ArrayList<BarEntry> visitors_forBar = new ArrayList<>();
        visitors_forBar.add(new BarEntry( 0, 0.3f));
        visitors_forBar.add(new BarEntry(  1,0.65f));
        visitors_forBar.add(new BarEntry( 2,0.05f));
        //visitors_forBar.add(new BarEntry( 3,4.5f));

        BarDataSet barDataSet = new BarDataSet(visitors_forBar,"");

        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);
        BarData barData = new BarData(barDataSet);

        String[] labels_bar = {"walk","static","run","uncertain"};
        barChart.getAxisRight().setEnabled(false);//隐藏右侧Y轴   默认是左右两侧都有Y轴
        XAxis xAxis_bar = barChart.getXAxis();
        xAxis_bar.setPosition(XAxis.XAxisPosition.BOTTOM); // 设置x轴显示在下方，默认在上方
        xAxis_bar.setTextSize(15f); // x轴上标签的大小
        xAxis_bar.setGranularity(1);//设置最小间隔，防止当放大时，出现重复标签。
        // 设置x轴显示的值的格式
        xAxis_bar.setValueFormatter(new IndexAxisValueFormatter(labels_bar));


        Legend legend_b = barChart.getLegend();
        legend_b.setFormSize(0f); // 图例的图形大小
        legend_b.setTextSize(0f); // 图例的文字大小

        barChart.setFitBars(true);
        //barChart.getDescription().setEnabled(false); // 不显示描述
        barChart.setData(barData);
        barChart.getDescription().setPosition(910,780);
        barChart.getDescription().setText( "");
        barChart.animateY( 2000);


        //以下添加饼状图
        PieChart pieChart = findViewById(R.id.pieChar);
        ArrayList<PieEntry> visitors_forPie = new ArrayList<>();
        visitors_forPie.add(new PieEntry((float) 0.7,"安静"));
        visitors_forPie.add(new PieEntry((float) 0.3,"嘈杂" ));

        PieDataSet pieDataSet = new PieDataSet(visitors_forPie, "环境音分析");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(0);
        pieDataSet.setSliceSpace(5);
        //pieDataSet.setEnabled(boolean enabled);
        //pieDataSet.setValueLinePart1OffsetPercentage(100f);//数据连接线距图形片内部边界的距离，为百分数
        //pieDataSet.setValueLinePart1Length(0.3f);
        //pieDataSet.setValueLinePart2Length(0.4f);
        //pieDataSet.setValueLineColor( Color.rgb(108, 176, 223));//设置连接线的颜色
        pieChart.setEntryLabelTextSize(0);//标签不显示

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled( false);
        //pieChart.setCenterText("环境音分析");
        //pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(10);
        pieChart.animate();


        //以下添加雷达图
        RadarChart radarChart = findViewById(R.id.radarChart);
        //以下是第一组数据
        ArrayList<RadarEntry> visitorsForFirstWebsite = new ArrayList<>();
        visitorsForFirstWebsite.add(new RadarEntry(1));
        visitorsForFirstWebsite.add(new RadarEntry((float) 1.2));
        visitorsForFirstWebsite.add(new RadarEntry((float) 1.4));
        visitorsForFirstWebsite.add(new RadarEntry((float) 0.5));
        visitorsForFirstWebsite.add(new RadarEntry((float) 1.7));
        visitorsForFirstWebsite.add(new RadarEntry((float) 0.6));
        visitorsForFirstWebsite.add(new RadarEntry((float) 0.8));
        //设置第一组数据样式
        RadarDataSet radarDataSetForFirstWebsite = new RadarDataSet(visitorsForFirstWebsite, "今天");
        radarDataSetForFirstWebsite.setColor(Color.RED);
        radarDataSetForFirstWebsite.setLineWidth(2f);
        radarDataSetForFirstWebsite.setValueTextColor(Color.RED);
        radarDataSetForFirstWebsite.setValueTextSize(8f);

        //以下是第二组数据
        ArrayList<RadarEntry> visitorsForSecondWebsite = new ArrayList<>();
        visitorsForSecondWebsite.add(new RadarEntry(2));
        visitorsForSecondWebsite.add(new RadarEntry((float) 1.1));
        visitorsForSecondWebsite.add(new RadarEntry((float) 1.6));
        visitorsForSecondWebsite.add(new RadarEntry((float) 0.8));
        visitorsForSecondWebsite.add(new RadarEntry(1));
        visitorsForSecondWebsite.add(new RadarEntry((float)1.8));
        visitorsForSecondWebsite.add(new RadarEntry((float) 1.3));
        //设置第二组数据样式
        RadarDataSet radarDataSetForSecondWebsite = new RadarDataSet(visitorsForSecondWebsite, "昨天  （单位：h）");
        radarDataSetForSecondWebsite.setColor(Color.BLUE);
        radarDataSetForSecondWebsite.setLineWidth(2f);
        radarDataSetForSecondWebsite.setValueTextColor(Color.BLUE);
        radarDataSetForSecondWebsite.setValueTextSize(8f);

        RadarData radarData = new RadarData();
        radarDataSetForFirstWebsite.setDrawValues(false);
        radarDataSetForSecondWebsite.setDrawValues(false);
        radarData.addDataSet(radarDataSetForFirstWebsite);
        radarData.addDataSet(radarDataSetForSecondWebsite);
        String[] labels = {"b站","微博","抖音","小红书","微信","QQ","其他"};

        XAxis xAxis = radarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        YAxis yAxis=radarChart.getYAxis();
        //是否绘制Y轴坐标点  和雷达框数据一般不同时存在 否则显得很挤 默认为true
        yAxis.setDrawLabels(true);
        yAxis.setTextColor(Color.GRAY);//Y轴坐标数据的颜色
        yAxis.setAxisMinimum(0);   //Y轴最小数值

        radarChart.getYAxis().setLabelCount(5,false);
        radarChart.getDescription().setText(" ");
        Legend legend_r = radarChart.getLegend();
        legend_r.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        //legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT); //显示位置，水平右对齐
        //legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP); // 显示位置，垂直上对齐


        //所有五边形的颜色
        radarChart.setWebColorInner(Color.GRAY);

        radarChart.setData(radarData);
    }
}