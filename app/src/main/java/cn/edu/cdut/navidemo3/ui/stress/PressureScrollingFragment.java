package cn.edu.cdut.navidemo3.ui.stress;

import static android.content.Context.MODE_PRIVATE;
import static cn.edu.cdut.navidemo3.ui.home.HomeFragment.FILE_FOLDER;
import static cn.edu.cdut.navidemo3.ui.home.HomeFragment.FOLDER_DAYACTIVITYDATA;
import static cn.edu.cdut.navidemo3.ui.home.HomeFragment.FOLDER_POSITION;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTabHost;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.edu.cdut.navidemo3.LabelActivity;
import cn.edu.cdut.navidemo3.MainActivity;
import cn.edu.cdut.navidemo3.R;
import cn.edu.cdut.navidemo3.WriteData2CSVThread;
import cn.edu.cdut.navidemo3.databinding.FragmentSlideshowBinding;
import cn.edu.cdut.navidemo3.extra.data.BaseTabFragment;
import cn.edu.cdut.navidemo3.extra.data.ESContinuousActivity;
import cn.edu.cdut.navidemo3.extra.data.ESDatabaseAccessor;
import cn.edu.cdut.navidemo3.extra.data.ESLabelStrings;
import cn.edu.cdut.navidemo3.extra.data.ESSettings;
import cn.edu.cdut.navidemo3.extra.data.ESTimestamp;
import cn.edu.cdut.navidemo3.extra.data.FeedbackActivity;
import cn.edu.cdut.navidemo3.extra.data.OnSwipeTouchListener;
import cn.edu.cdut.navidemo3.ui.home.HomeFragment;
import cn.edu.cdut.navidemo3.databinding.FragmentPressureScrollingBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PressureScrollingFragment extends BaseTabFragment {
    public PressureScrollingFragment(){

    }
    private String path;

    private String url = "http://192.168.32.188:5000/upload"; //http://192.168.32.188:5000/upload


    private static PressureScrollingFragment instance;
    private FragmentPressureScrollingBinding binding;
    private TabHost _fragmentTabHost;
    private static final String LOG_TAG = "[ESHistoryFragment]";
    private static final String INVALID_MERGE_ZONE_ALERT_TEXT = "The marked events contain more than one labeled event. Can't merge them to a single event.";
    private static final String ALERT_BUTTON_TEXT_OK = "O.K.";
    private static final int FEEDBACK_FROM_HISTORY_REQUEST_CODE = 3;
    //public static ESContinuousActivity[] _activityArray = null;
    public static ESContinuousActivity[] _activityArray = new ESContinuousActivity[1];
    private String _headerText = null;
    private int _dayRelativeToToday = 0;
    private boolean _presentingSplitContinuousActivity = false;
    private boolean _justGotBackFromFeedback = false;
    private ESTimestamp _markZoneStartTimestamp = null;
    private ESTimestamp _markZoneEndTimestamp = null;
    private void clearMergeMarkZone() {
        _markZoneStartTimestamp = null;
        _markZoneEndTimestamp = null;
    }
    public static PressureScrollingFragment getInstance(){
        return instance;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        instance = this;
        binding = FragmentPressureScrollingBinding.inflate(inflater, container, false);

        return inflater.inflate(R.layout.fragment_pressure_scrolling, container, false);
    }



    @Override
    public void onStart() {
        super.onStart();

        if (_justGotBackFromFeedback) {
            // Don't change the day.
            //不要改变这一天。
            _justGotBackFromFeedback = false;
        }
        else {
            // Change the day to "today"
            //将日期改为“今天”。
            _dayRelativeToToday = 0;
        }
        _presentingSplitContinuousActivity = false;
        clearMergeMarkZone();
        setTimeUnitSelectorContent();//设置TimeUnit选择器内容();
        calculateAndPresentDaysHistory();//计算和呈现当前历史();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //清除孤儿记录
        ESDatabaseAccessor.getESDatabaseAccessor().clearOrphanRecords(new ESTimestamp(0));
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == FEEDBACK_FROM_HISTORY_REQUEST_CODE) {
            // Remain in the same day
            // 保持在同一天
            _justGotBackFromFeedback = true;
        }
    }

    private boolean allowedToEditDaysActivities() {
        // (allowed to edit only from today and yesterday):
        // 只允许从今天和昨天编辑
        return _dayRelativeToToday >= -1;
    }
    /**
     * Calculate the history of a single day and present it as a list of continuous activities
     *计算一天的历史记录，并将其显示为连续活动的列表
     */
    private synchronized void calculateAndPresentDaysHistory() {
        _presentingSplitContinuousActivity = false;

        //getting today's activities
        //获取今天的活动
        ESTimestamp todayStartTime = ESTimestamp.getStartOfTodayTimestamp();
        ESTimestamp focusDayStartTime = new ESTimestamp(todayStartTime,_dayRelativeToToday);
        ESTimestamp focusDayEndTime = new ESTimestamp(focusDayStartTime, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd", Locale.US);
        _headerText = dateFormat.format(focusDayStartTime.getDateOfTimestamp());
        if (_dayRelativeToToday == 0) {
            // Then it's today:
            // 然后就是今天:
            _headerText = "Today- " + _headerText;
        }
        if (!allowedToEditDaysActivities()) {
            _headerText += " (view only)";
        }

        Log.d(LOG_TAG, "getting activities from " + focusDayStartTime.infoString() + " to " + focusDayEndTime.infoString());

        boolean addGapDummies = true;
        _activityArray = ESDatabaseAccessor.getESDatabaseAccessor().
                getContinuousActivitiesFromTimeRange(focusDayStartTime, focusDayEndTime, addGapDummies);
        //展示历史内容
        presentHistoryContent();
        //ESDatabaseAccessor.getESDatabaseAccessor().getESActivity();

        //这句是我加的
        //_activityArray[0].getLocationLatLongFromFirstInstance();
    }

    private static final String[] TIME_UNIT_LABELS = new String[]{"1 minute","5 minutes","10 minutes","15 minutes","20 minutes","30 minutes"};
    private static final int[] TIME_UNIT_VALS_MINUTES = new int[]{1,5,10,15,20,30};
    private void setTimeUnitSelectorContent() { //设置TimeUnit选择器内容
        int latestSelectedTimeUnit = ESSettings.historyTimeUnitInMinutes();
        int latestSelectedPos = 0;
        Spinner timeUnitSelector = (Spinner)(getView().findViewById(R.id.spinner_time_unit_in_history));
        List<String> timeUnitStrings = new ArrayList(TIME_UNIT_LABELS.length);
        for (int i = 0; i < TIME_UNIT_VALS_MINUTES.length; i ++) {
            timeUnitStrings.add(TIME_UNIT_LABELS[i]);
            if (latestSelectedTimeUnit == TIME_UNIT_VALS_MINUTES[i]) {
                latestSelectedPos = i;
            }
        }
        ArrayAdapter<String> timeUnitAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, timeUnitStrings);
        timeUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeUnitSelector.setAdapter(timeUnitAdapter);
        timeUnitSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int timeUnitMinutes = TIME_UNIT_VALS_MINUTES[position];
                ESSettings.setHistoryTimeUnitInMinutes(timeUnitMinutes);
                calculateAndPresentDaysHistory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        timeUnitSelector.setSelection(latestSelectedPos);
    }


    private void presentHistoryContent() {

        //Set day title
        View header = getView().findViewById(R.id.history_header);
        TextView headerLabel = (TextView) header.findViewById(R.id.text_history_header_title);
        headerLabel.setText(_headerText);

        // Time-unit list:

        // Adjust the day-navigation buttons:
        Button prevButton = (Button) header.findViewById(R.id.button_previous_day_in_history_header);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_presentingSplitContinuousActivity) {
                    _dayRelativeToToday--;
                }
                clearMergeMarkZone();
                calculateAndPresentDaysHistory();
            }
        });
        Button nextButton = (Button) header.findViewById(R.id.button_next_day_in_history_header);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_presentingSplitContinuousActivity) {
                    _dayRelativeToToday++;
                }
                clearMergeMarkZone();
                calculateAndPresentDaysHistory();
            }
        });

        Log.d(LOG_TAG,"==== Got " + _activityArray.length + " cont activities: ");
        for (int i= 0; i < _activityArray.length; i++) {
            Log.d(LOG_TAG, _activityArray[i].toString());
        }

        ArrayList<ESContinuousActivity> activityList = getArrayList(_activityArray);


        SimpleDateFormat formatter= new SimpleDateFormat("MM_dd");

        Button btn_savelables = getView().findViewById(R.id.btn_savelables);
        btn_savelables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //这是以前的代码
/*                Date date = new Date(System.currentTimeMillis());
                StringBuilder theDayActivityData = new StringBuilder();
                for (int i= 0; i < activityList.size(); i++) {
                    //Log.d("ASDFGHJKL", activityList.get(i).toString());
                    theDayActivityData.append(activityList.get(i).toString()).append("\n");
                }
                DeleteFolder(FILE_FOLDER + FOLDER_DAYACTIVITYDATA+ File.separator + _headerText);
                WriteData2CSVThread myThread_saveposision = new WriteData2CSVThread(theDayActivityData,FILE_FOLDER + FOLDER_DAYACTIVITYDATA+ File.separator + _headerText,"theDayActivityData_data_" +_headerText+".csv");
                myThread_saveposision.run();
                theDayActivityData.setLength(0);
                Toast.makeText(getContext(), "已保存", Toast.LENGTH_SHORT).show();*/
                SharedPreferences preference = getActivity().getSharedPreferences("user",MODE_PRIVATE);
                //Toast.makeText(getActivity(),""+preference.getString("name","请输入用户名：").contains("："),Toast.LENGTH_SHORT).show();

                if (preference.getString("name","请输入用户名：").contains("：")){
                    Toast.makeText(getActivity(),"请到导航栏中修改用户名！",Toast.LENGTH_SHORT).show();
                    return;
                }

                //这是新版本
                Intent intent = new Intent(getActivity(), LabelActivity.class);
                startActivity(intent);

            }
        });

        Button btn_uploadLables = getView().findViewById(R.id.btn_uploadlables);
        btn_uploadLables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    path = "/storage/emulated/0/upload/label1.db";
                    uploadFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // Get the list view and set it using this adapter
        // 获取列表视图并使用此适配器对其进行设置
        ListView listView = (ListView) getView().findViewById(R.id.listview_history_items);
        if (listView.getAdapter() == null) {
            HistoryAdapter histAdapter = new HistoryAdapter(getActivity().getBaseContext(), R.layout.history_rowlayout, activityList,this);
            listView.setAdapter(histAdapter);
        }
        else {
            ((HistoryAdapter)listView.getAdapter()).resetItems(activityList);
        }

    }

    private ArrayList<ESContinuousActivity> getArrayList(ESContinuousActivity[] items) {
        if (items == null) {
            return new ArrayList<>(0);
        }
        ArrayList<ESContinuousActivity> arrayList = new ArrayList<>(items.length);
        for (int i=0; i<items.length; i++) {
            arrayList.add(items[i]);
        }
        return arrayList;
    }

    @Override
    protected void reactToRecordsUpdatedEvent() {
        super.reactToRecordsUpdatedEvent();
        Log.v(LOG_TAG,"reacting to records-update");
        if (_presentingSplitContinuousActivity) {
            Log.v(LOG_TAG,"Since presenting split continuous activity, not refreshing the history page.");
            return;
        }
        calculateAndPresentDaysHistory();
    }
    private boolean isActivityInTheMergeMarkZone(ESContinuousActivity continuousActivity) {
        if (_markZoneStartTimestamp == null || _markZoneEndTimestamp == null) {
            return false;
        }

        ESTimestamp timestamp = continuousActivity.getStartTimestamp();
        if (timestamp.isEarlierThan(_markZoneStartTimestamp)) {
            return false;
        }

        if (timestamp.isLaterThan(_markZoneEndTimestamp)) {
            return false;
        }

        return true;
    }

    private synchronized boolean isMarkZoneValidForMerging() {
        // Just as sanity check, make sure there is a mark zone:
        if (_markZoneStartTimestamp == null || _markZoneEndTimestamp == null) {
            return false;
        }

        boolean foundUserProvidedLabels = false;
        for (ESContinuousActivity continuousActivity : _activityArray) {
            if (continuousActivity.isUnrecordedGap()) {
                // Ignore this dummy-activity:
                continue;
            }
            if (continuousActivity.getStartTimestamp().isEarlierThan(_markZoneStartTimestamp)) {
                continue;
            }
            if (continuousActivity.getStartTimestamp().isLaterThan(_markZoneEndTimestamp)) {
                // Then there's no use continuing on the list, we passed the mark zone safely:
                break;
            }

            if (continuousActivity.hasUserProvidedLabels()) {
                // did we already find another activity with user-labels?
                if (foundUserProvidedLabels) {
                    // Then this zone is not valid. We have 2 different activities with user labels:
                    return false;
                }
                foundUserProvidedLabels = true;
            }
        }
        // If reached here safely, we haven't found 2 different activities with user labels
        return true;
    }

    private void alertUserOfInvalidMergeMarkZone() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher_foreground).setMessage(INVALID_MERGE_ZONE_ALERT_TEXT);
        builder.setPositiveButton(ALERT_BUTTON_TEXT_OK,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                clearMergeMarkZone();
                presentHistoryContent();
            }
        });
        builder.create().show();
    }

    private void rowClicked(ESContinuousActivity continuousActivity) {
        ESContinuousActivity continuousActivityForFeedback = continuousActivity;

        // See if this row is in the mark zone:
        if (isActivityInTheMergeMarkZone(continuousActivity)) {
            // Then the feedback should be done over the whole time range of the marked zone
            // First check the validity of the time zone for merging:
            if (!isMarkZoneValidForMerging()) {
                // Then the mark zone is not valid. We should alert the user and clear the zone:
                Log.i(LOG_TAG,"Clicked a row in an invalid merge zone.");
                alertUserOfInvalidMergeMarkZone();
                return;
            }

            // Mark zone is safe for merging,
            // so we should go to feedback for a single continuous activity for the whole marked time range:
            continuousActivityForFeedback = ESDatabaseAccessor.getESDatabaseAccessor().
                    getSingleContinuousActivityFromTimeRange(_markZoneStartTimestamp, _markZoneEndTimestamp);
        }

        Intent intent = new Intent(getActivity(),FeedbackActivity.class);
        FeedbackActivity.setFeedbackParametersBeforeStartingFeedback(new FeedbackActivity.FeedbackParameters(continuousActivityForFeedback));
        startActivityForResult(intent,FEEDBACK_FROM_HISTORY_REQUEST_CODE);
        //startActivity(intent);
    }

    private void rowSwipedRight(ESContinuousActivity continuousActivity) {
        // Are we presenting a split continuous activity?
        if (_presentingSplitContinuousActivity) {
            // Then ignore this gesture:
            Log.v(LOG_TAG,"[swipe-right] In split-activities mode. Ignoring swipe to the right.");
            return;
        }

        // Is this continuous activity already in the mark zone?
        if (isActivityInTheMergeMarkZone(continuousActivity)) {
            // Then this gesture should cause clearing the mark zone:
            Log.v(LOG_TAG, "[swipe-right] Row already in marked zone. Clearing mark zone.");
            clearMergeMarkZone();
            presentHistoryContent();
            return;
        }

        ESTimestamp swipedStartTimestamp = continuousActivity.getStartTimestamp();
        ESTimestamp swipedEndTimestamp = continuousActivity.getEndTimestamp();

        // Is there no mark zone currently?
        if (_markZoneStartTimestamp == null || _markZoneEndTimestamp == null) {
            // Then this marked activity is starting a new mark zone:
            Log.v(LOG_TAG,"[swipe-right] No mark zone. Starting new mark zone with this one row.");
            _markZoneStartTimestamp = swipedStartTimestamp;
            _markZoneEndTimestamp = swipedEndTimestamp;
        }
        else if (swipedStartTimestamp.isEarlierThan(_markZoneStartTimestamp)) {
            Log.v(LOG_TAG,"[swipe-right] Row earlier than mark zone. Expanding zone to earlier.");
            _markZoneStartTimestamp = swipedStartTimestamp;
        }
        else if (swipedEndTimestamp.isLaterThan(_markZoneEndTimestamp)) {
            Log.v(LOG_TAG,"[swipe-right] Row later than mark zone. Expanding zone to later.");
            _markZoneEndTimestamp = swipedEndTimestamp;
        }
        else {
            // We should have covered all the cases.
            Log.e(LOG_TAG,"Swipe right failed to fit any case.");
        }

        presentHistoryContent();
    }

    private synchronized void rowSwipedLeft(ESContinuousActivity continuousActivity) {
        // Split the chosen continuous activity and present it as separate minute activities:
        _presentingSplitContinuousActivity = true;

        Date startTime = continuousActivity.getStartTimestamp().getDateOfTimestamp();
        Date endTime = continuousActivity.getEndTimestamp().getDateOfTimestamp();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE hh:mm", Locale.US);
        _headerText = String.format("%s - %s",dateFormat.format(startTime),dateFormat.format(endTime));

        // Split to atomic (minute) activities:
        ESContinuousActivity[] splitActivities = ESDatabaseAccessor.getESDatabaseAccessor().splitToSeparateContinuousActivities(continuousActivity);
        _activityArray = splitActivities;
        clearMergeMarkZone();
        presentHistoryContent();
    }

    private static class HistoryAdapter extends ArrayAdapter {

        private static final int ITEM_TYPE_DUMMY = 0;
        private static final int ITEM_TYPE_ACTUAL = 1;

        private ArrayList<ESContinuousActivity> _items;
        private PressureScrollingFragment _handler;

        /**
         * Constructor for History Adapter
         * @param context context from activity
         * @param layoutResourceId The xml rowlayout
         * @param items The list of ESContinuousActivity with the values we want to display
         * @param handler The HistoryFragment that uses this adapter
         */
        public HistoryAdapter(Context context, int layoutResourceId, ArrayList<ESContinuousActivity> items, PressureScrollingFragment handler) {
            super(context,layoutResourceId,R.id.text_main_activity_in_history_row,items);
            this._items = items;
            this._handler = handler;
        }

        @Override
        public int getViewTypeCount() { return 2; }

        @Override
        public int getItemViewType(int position) {
            return _items.get(position).isUnrecordedGap() ? ITEM_TYPE_DUMMY : ITEM_TYPE_ACTUAL;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int rowType = getItemViewType(position);
            ESContinuousActivityHolder holder = null;

            View row;
            if (convertView == null) {
                holder = new ESContinuousActivityHolder();
                switch (rowType) {
                    case ITEM_TYPE_DUMMY:
                        row = LayoutInflater.from(getContext()).inflate(R.layout.history_gap_rowlayout,null);
                        holder.gapTime = (TextView)row.findViewById(R.id.text_gap_in_history_dummy_row);
                        break;
                    case ITEM_TYPE_ACTUAL:
                        row = super.getView(position,convertView,parent);
                        //row = LayoutInflater.from(getContext()).inflate(R.layout.history_rowlayout,null);
                        holder.time = (TextView)row.findViewById(R.id.text_time_in_history_row);
                        holder.mainActivity = (TextView)row.findViewById(R.id.text_main_activity_in_history_row);
                        holder.details = (TextView)row.findViewById(R.id.text_details_in_history_row);
                        break;
                    default:
                        throw new InvalidParameterException("Got unsupported history row type: " + rowType);
                }
                row.setTag(holder);
            }
            else {
                row = convertView;
                holder = (ESContinuousActivityHolder)row.getTag();
            }

            //get one activity from the array
            final ESContinuousActivity continuousActivity = _items.get(position);

            // Set the values for the row:
            if (rowType == ITEM_TYPE_DUMMY) {
                int gapSeconds = continuousActivity.gapDurationSeconds();
                int gapMinutes = gapSeconds / 60;
                int gapHours = gapSeconds / 3600;
                String gapDurStr = (gapHours >= 1) ? "" + gapHours + " hours" : "" + gapMinutes + " minutes";
                String gapStr = "Gap ~" + gapDurStr;

                holder.gapTime.setText(gapStr);
                return row;
            }

            // Assume now we have a regular actual continuous activity row:
            String activityLabel = "";
            String mainActivityForColor = "";
            String timeLabel = "";
            String endTimeLabel = "";
            Date date;

            if(continuousActivity.getMainActivityUserCorrection() != null){
                activityLabel = continuousActivity.getMainActivityUserCorrection();
                mainActivityForColor = activityLabel;
            }
            else{
                mainActivityForColor = continuousActivity.getMainActivityServerPrediction();
                if (mainActivityForColor == null) {
                    activityLabel = "in process...";
                }
                else {
                    activityLabel = mainActivityForColor + "?";
                }
            }

            // Add location coordinates (each instance may have a representative location point):
//            activityLabel += " loc: " + ESLabelStrings.makeCSV(continuousActivity.getLocationLatLongFromFirstInstance());

            //setting time label
            date = continuousActivity.getStartTimestamp().getDateOfTimestamp();
            timeLabel = new SimpleDateFormat("hh:mm a").format(date);
            date = continuousActivity.getEndTimestamp().getDateOfTimestamp();
            endTimeLabel = new SimpleDateFormat("hh:mm a").format(date);
            if (!endTimeLabel.equals(timeLabel)) {
                timeLabel = timeLabel + " - " + endTimeLabel;
            }

            //setting activity label
            holder.mainActivity.setText(activityLabel);
            holder.time.setText(timeLabel);

            // Setting the details label:
            holder.details.setText(getDetailsString(continuousActivity));

            row.setBackgroundColor(ESLabelStrings.getColorForMainActivity(mainActivityForColor));

            // Is this row marked for merging?
            ImageView chckmarkView = (ImageView)row.findViewById(R.id.image_mark_for_merge_in_history);
            if (_handler.isActivityInTheMergeMarkZone(continuousActivity)) {
                chckmarkView.setImageResource(R.drawable.checkmark_in_circle);
            }
            else {
                chckmarkView.setImageBitmap(null);
            }

            // If allowed to edit activities, define the listener for click and swipes:
            //如果允许编辑活动，定义点击和滑动的监听器:
            if (_handler.allowedToEditDaysActivities()) {
                // Setting the click listener:
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(LOG_TAG, "row clicked");
                        _handler.rowClicked(continuousActivity);
                    }
                });

                // Setting the gesture detections:
                row.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
                    @Override
                    public boolean onSwipeRight() {
                        Log.i(LOG_TAG, "Swiped row to the right");
                        _handler.rowSwipedRight(continuousActivity);
                        return true;
                    }

                    @Override
                    public boolean onSwipeLeft() {
                        Log.i(LOG_TAG, "Swiped row to the left");
                        _handler.rowSwipedLeft(continuousActivity);
                        return true;
                    }
                });
            }
            else {
                // Make sure this row has no response to click or swipes:
                row.setOnClickListener(null);
                row.setOnTouchListener(null);
            }

            return row;
        }

        private String getDetailsString(ESContinuousActivity continuousActivity) {
            String details = "";

            String[] moods = continuousActivity.getMoods();
            if (moods != null && moods.length > 0) {
                details += moods[0];
                for (int i=1; i<moods.length; i++) {
                    details += ", " + moods[i];
                }
            }

            String[] sec;
            String delim = ",";
            String suffix = "";
            if (continuousActivity.hasUserProvidedLabels()) {
                sec = continuousActivity.getSecondaryActivities();
            }
            else {
                sec = continuousActivity.getSecondaryActivitiesOrServerGuesses();
                delim = "";
                suffix = "?";
            }
            if (sec != null && sec.length > 0) {
                details += " (" + sec[0] + suffix;
                for (int i=1; i<sec.length; i++) {
                    details += delim + " " + sec[i] + suffix;
                }
                details += ")";
            }

            if (details.length() > 100) {
                details = details.substring(0,100) + "...";
            }
            return details;
        }

        public void resetItems(ArrayList<ESContinuousActivity> items) {
            this._items.clear();
            this._items.addAll(items);
            notifyDataSetChanged();
        }

        static class ESContinuousActivityHolder
        {
            TextView time;
            TextView mainActivity;
            TextView details;
            TextView gapTime;

            public String toString() {
                return "time: " + time + ". main: " + mainActivity + ". details: " + details + ". gap: " + gapTime;
            }
        }
    }


    /**
     *  根据路径删除指定的目录或文件，无论存在与否
     *@param sPath  要删除的目录或文件
     *@return 删除成功返回 true，否则返回 false。
     */
    public boolean DeleteFolder(String sPath) {
        Boolean flag = false;
        File file = new File(sPath);
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
            return flag;
        } else {
            // 判断是否为文件
            if (file.isFile()) {  // 为文件时调用删除文件方法
                return deleteFile(sPath);
            } else {  // 为目录时调用删除目录方法
                return deleteDirectory(sPath);
            }
        }
    }


    /**
     * 删除单个文件
     * @param   sPath    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String sPath) {
        Boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }


    /**
     * 删除目录（文件夹）以及目录下的文件
     * @param   sPath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        Boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static ESContinuousActivity[] get_activityArray() {
        ESTimestamp todayStartTime = ESTimestamp.getStartOfTodayTimestamp();
        ESTimestamp focusDayStartTime = new ESTimestamp(todayStartTime,0);
        ESTimestamp focusDayEndTime = new ESTimestamp(focusDayStartTime, 1);
        return ESDatabaseAccessor.getESDatabaseAccessor().
                getContinuousActivitiesFromTimeRange(focusDayStartTime, focusDayEndTime, false);
    }

    private void uploadFile(String path) throws IOException {  //用uploadFile函数上传文件
        OkHttpClient client = new OkHttpClient();//避免多次生成实例
        File file1 = new File(path);
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("label","label1.db", RequestBody.create(MediaType.parse("multipart/form-data"),file1))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30,TimeUnit.SECONDS)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //Log.d(TAG, "onFailure: "+e.getLocalizedMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"上传失败",Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String res = response.body().string();
                //Log.d(TAG, "onResponse: "+res);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //result.setText(res);
                        Toast.makeText(getContext(),"上传成功,服务器返回：" +res ,Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
        client =null;
    }

}

