package cn.edu.cdut.navidemo3;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class CustomYValueFormatter extends ValueFormatter implements IAxisValueFormatter {
    final String labelName[] = {"周一", "周二", "周三", "周四", "周五"};


    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if ((int) value < labelName.length) {
            return labelName[(int) value];
        } else {
            return "";
        }

    }
}
