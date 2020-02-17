package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executor;

public class Main_Activity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    private Set<BluetoothDevice> pairedDevice;
    private int pairedDeviceCount;
    private Animation anim, anim2;
    private TextView tv_dust, tv_state, tv_o3, tv_co, tv_no2, tv_so2, tv_pm25, tv_pm10;
    private TextView tv_color[], tv_searchBubble;
    private ImageView img_blutooth, img_level, img_dustMc, img_cloud, img_dustBg, img_progress, img_progress2;
    private BluetoothAdapter mAdapter;
    private BluetoothDevice mDevice;
    private boolean blueToothfair = false;
    private Thread mWorkerThread;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private Spinner spinner;
    private String sp_naturalList[];
    private String cityList[];
    private int sp_getNumber;
    private ArrayList sp_ArrayList;
    private ArrayAdapter sp_ArrayAdapter;
    private byte mDelimiter = '.';    // 문장 마지막 문자
    private int readBufferPosition;
    private int dustWidth,dustHeight,dustCurrentVelue=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initSetting();
        checkBT();

    }

    //----------------초기 셋팅 및 메소드----------------------------------------------------------
    private void initSetting() {
        img_blutooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBT();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sp_getNumber = i;
                for (int j = 0; j < sp_naturalList.length; j++) {
                    String url1 = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnMesureLIst?" +
                            "serviceKey=Kv72vV63EtA%2Bf49pCxN4eRo35S4kbZTJDGg%2FGo08p2EBXwMEg0Gn2eodSI%2FRMnNdf4EBN%2BI%2FTUC8fmyFYsbXww%3D%3D&" +
                            "numOfRows=1&pageNo=1&dataGubun=HOUR&searchCondition=MONTH&itemCode=" +
                            sp_naturalList[j];
                    DownloadTask_Rank boxTask = new DownloadTask_Rank();
                    boxTask.execute(url1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        img_dustMc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_dustMc.startAnimation(anim);
                img_dustMc.setImageResource(R.drawable.dust_mc3);

                Timer mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        img_dustMc.setImageResource(R.drawable.dust_mc1);
                    }
                }, 150);
            }
        });

    }

    public void initView() {
        tv_searchBubble = (TextView) findViewById(R.id.tv_measureDust);
        tv_state = (TextView) findViewById(R.id.BluetoothStateTv);
        tv_dust = (TextView) findViewById(R.id.FineDustValueTv);
        tv_o3 = (TextView) findViewById(R.id.tv_o3);
        tv_co = (TextView) findViewById(R.id.tv_co);
        tv_no2 = (TextView) findViewById(R.id.tv_no2);
        tv_so2 = (TextView) findViewById(R.id.tv_so2);
        tv_pm10 = (TextView) findViewById(R.id.tv_pm10);
        tv_pm25 = (TextView) findViewById(R.id.tv_pm25);
        tv_color = new TextView[4];
        tv_color[0] = (TextView) findViewById(R.id.tv_color1);
        tv_color[1] = (TextView) findViewById(R.id.tv_color2);
        tv_color[2] = (TextView) findViewById(R.id.tv_color3);
        tv_color[3] = (TextView) findViewById(R.id.tv_color4);
        img_blutooth = (ImageView) findViewById(R.id.bluetooth_Icon);
        img_dustMc = (ImageView) findViewById(R.id.img_dustMc);
        img_cloud = (ImageView) findViewById(R.id.img_cloud);
        img_progress = (ImageView) findViewById(R.id.img_progress);
        img_progress2 = (ImageView) findViewById(R.id.img_progress_2);
        img_dustBg = (ImageView) findViewById(R.id.img_dustBg);

        anim = AnimationUtils.loadAnimation(Main_Activity.this, R.anim.dust_move);
        anim2 = AnimationUtils.loadAnimation(Main_Activity.this, R.anim.dust_loding);
        img_dustMc.setAnimation(anim);
        sp_naturalList = new String[]{"SO2", "CO", "O3", "NO2", "PM10", "PM25"};
        cityList = new String[]{"seoul", "busan", "daegu", "incheon", "gwangju", "daejeon", "ulsan", "gyeonggi", "gangwon",
                "chungbuk", "chungnam", "jeonbuk", "jeonnam", "gyeongbuk", "gyeongnam", "jeju", "sejong"};
        int img_level_int[] = new int[4];
        img_level_int[0] = R.id.img_level1;
        img_level_int[1] = R.id.img_level2;
        img_level_int[2] = R.id.img_level3;
        img_level_int[3] = R.id.img_level4;
        for (int i = 0; i < 4; i++) {
            img_level = (ImageView) findViewById(img_level_int[i]);
            img_level.setBackground(new ShapeDrawable(new OvalShape()));
            img_level.setClipToOutline(true);
            img_level.setBackgroundColor(tv_color[i].getCurrentTextColor());
        }
        sp_ArrayList = new ArrayList<>();
        arrayListStringAdd();
        sp_ArrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                sp_ArrayList);
        spinner = (Spinner) findViewById(R.id.spn_name);
        spinner.setAdapter(sp_ArrayAdapter);

        img_dustBg.post(new Runnable() {
            @Override
            public void run() {
                dustHeight = img_dustBg.getDrawable().getIntrinsicHeight() - img_dustBg.getHeight();
                dustWidth = img_dustBg.getDrawable().getIntrinsicWidth()  / 100;
                img_dustBg.setScaleType(ImageView.ScaleType.MATRIX);
                img_dustBg.setImageMatrix(getMatrix(img_dustBg.getWidth(), -dustHeight));
            }
        });

        IntentFilter bfilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, bfilter);

    }

    private void arrayListStringAdd() {
        sp_ArrayList.add("서울");
        sp_ArrayList.add("부산");
        sp_ArrayList.add("대구");
        sp_ArrayList.add("인천");
        sp_ArrayList.add("광주");
        sp_ArrayList.add("대전");
        sp_ArrayList.add("울산");
        sp_ArrayList.add("경기");
        sp_ArrayList.add("강원");
        sp_ArrayList.add("충북");
        sp_ArrayList.add("충남");
        sp_ArrayList.add("전북");
        sp_ArrayList.add("전남");
        sp_ArrayList.add("경북");
        sp_ArrayList.add("경남");
        sp_ArrayList.add("제주");
        sp_ArrayList.add("세종");
    }

    private void naturalTextColorValue(TextView textView, float values, String naturalName) {
        textView.setText("" + values);
        if (naturalName.equals("SO2")) {
            if (values > 0.02f) {
                textView.setTextColor(tv_color[1].getCurrentTextColor()); //보통
                if (values > 0.05f) {
                    textView.setTextColor(tv_color[2].getCurrentTextColor()); //나쁨
                    if (values > 0.15f)
                        textView.setTextColor(tv_color[3].getCurrentTextColor()); //매우나쁨
                }
            } else
                textView.setTextColor(tv_color[0].getCurrentTextColor()); //좋음
        } else if (naturalName.equals("CO")) {
            if (values > 2f) {
                textView.setTextColor(tv_color[1].getCurrentTextColor()); //보통
                if (values > 9f) {
                    textView.setTextColor(tv_color[2].getCurrentTextColor()); //나쁨
                    if (values > 15f)
                        textView.setTextColor(tv_color[3].getCurrentTextColor()); //매우나쁨
                }
            } else
                textView.setTextColor(tv_color[0].getCurrentTextColor()); //좋음
        } else if (naturalName.equals("O3")) {
            if (values > 0.03f) {
                textView.setTextColor(tv_color[1].getCurrentTextColor()); //보통
                if (values > 0.09f) {
                    textView.setTextColor(tv_color[2].getCurrentTextColor()); //나쁨
                    if (values > 0.15f)
                        textView.setTextColor(tv_color[3].getCurrentTextColor()); //매우나쁨
                }
            } else
                textView.setTextColor(tv_color[0].getCurrentTextColor()); //좋음
        } else if (naturalName.equals("NO2")) {
            if (values > 0.03f) {
                textView.setTextColor(tv_color[1].getCurrentTextColor()); //보통
                if (values > 0.06f) {
                    textView.setTextColor(tv_color[2].getCurrentTextColor()); //나쁨
                    if (values > 0.2f)
                        textView.setTextColor(tv_color[3].getCurrentTextColor()); //매우나쁨
                }
            } else
                textView.setTextColor(tv_color[0].getCurrentTextColor()); //좋음
        } else if (naturalName.equals("PM10")) {
            if (values > 30f) {
                textView.setTextColor(tv_color[1].getCurrentTextColor()); //보통
                if (values > 80f) {
                    textView.setTextColor(tv_color[2].getCurrentTextColor()); //나쁨
                    if (values > 150f)
                        textView.setTextColor(tv_color[3].getCurrentTextColor()); //매우나쁨
                }
            } else
                textView.setTextColor(tv_color[0].getCurrentTextColor()); //좋음
        } else if (naturalName.equals("PM2.5")) {
            if (values > 15f) {
                textView.setTextColor(tv_color[1].getCurrentTextColor()); //보통
                if (values > 35f) {
                    textView.setTextColor(tv_color[2].getCurrentTextColor()); //나쁨
                    if (values > 75f)
                        textView.setTextColor(tv_color[3].getCurrentTextColor()); //매우나쁨
                }
            } else
                textView.setTextColor(tv_color[0].getCurrentTextColor()); //좋음
        }
    }

    private void measureDustStateValueView(int valueDust) {
        int indexNumber;

        if (valueDust > 75) {  //매우나쁨
            indexNumber = 3;
            tv_searchBubble.setText("숨 쉬는 것보다\n안 쉬는게 이득!");
        }
        else if (valueDust > 35) { //나쁨
            indexNumber = 2;
            tv_searchBubble.setText("미세먼지가 나를\n괴롭힌다.");
        }
        else if (valueDust > 15) { //보통
            indexNumber = 1;
            tv_searchBubble.setText("이 정도면\n나쁘지 않아.");
        }
        else {  //좋음
            indexNumber = 0;
            tv_searchBubble.setText("공기 좋아~\n집에만 있지말고\n나가서 산책이라도 해");
        }

        tv_dust.setTextColor(tv_color[indexNumber].getCurrentTextColor());
        img_cloud.setColorFilter(tv_color[indexNumber].getCurrentTextColor(), PorterDuff.Mode.SRC_IN);
        bgMoveView(valueDust, dustCurrentVelue);
        dustCurrentVelue = valueDust;
    }

    private void bgMoveView(int nextValue, int currentValue){
        int addValue = 1;
        int standardValue = 80; //기준치

        if(nextValue >= standardValue){
            nextValue = standardValue;
        }
        if(currentValue >= standardValue){
            currentValue = standardValue;
        }

        final int moveNext = (int)(((float)nextValue/standardValue) * 100) * dustWidth;
        final int moveCurrent = (int)(((float)currentValue/standardValue) * 100) * dustWidth;
        if(moveCurrent < moveNext){
            addValue = 1;
        }
        else if(moveCurrent > moveNext) {
            addValue = -1;
        }
        else{
            return;
        }

        final int[] valueCount = {moveCurrent};
        final int finalAddValue = addValue;
        img_dustBg.setScaleType(ImageView.ScaleType.MATRIX);
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                valueCount[0] += finalAddValue;
                img_dustBg.setImageMatrix(getMatrix(-valueCount[0],0));
                if(valueCount[0]  == moveNext){
                    this.cancel();

                    }
                }
            }, 1,1);

    }

    private Matrix getMatrix(float valueX_m,float valueY_m) {
        Matrix m = new Matrix();
        m.postTranslate(img_dustBg.getWidth()+valueX_m,valueY_m);
        return  m;
    }

    //---------------공공데이터 테스크-----------------------------------------------------------
    public class DownloadTask_Rank extends AsyncTask<String, Void, String> {

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            img_progress.setVisibility(View.VISIBLE);
            img_progress.setAnimation(anim2);
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            publishProgress();
            if (this.isCancelled()) {
                return null;
            }
            try {
                return (String) downloadUrl(params[0]);
            } catch (IOException ex) {
                return "인터넷 연결을 확인해주세요";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            img_progress.setAnimation(null);
            img_progress.setVisibility(View.INVISIBLE);
            parseXml(s);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        private String downloadUrl(String myurl) throws IOException { //HTTP 로 URL 접속을 위한 객체 생성
            HttpURLConnection conn = null;
            try {
                URL url = new URL(myurl);
                conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                BufferedInputStream bufIs = new BufferedInputStream(is);
                InputStreamReader isReader = new InputStreamReader(bufIs, "utf-8");
                BufferedReader bufReader = new BufferedReader(isReader);
                String line = null;
                StringBuilder builder = new StringBuilder();
                while (null != (line = bufReader.readLine())) {
                    builder.append(line);
                }
                return builder.toString();
            } finally {
                conn.disconnect();
            }
        }

        private void parseXml(String result) {
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();

                parser.setInput(new StringReader(result));

                int eventType = parser.getEventType();
                StringBuilder builer = new StringBuilder();

                String titleValue = "";
                String itemCodeName = "";

                boolean Title = false;
                boolean itemCode = false;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        ;
                    }
                    else if (eventType == XmlPullParser.START_TAG) {
                        String tag_name = parser.getName();
                        if (tag_name.equals(cityList[sp_getNumber])) {
                            Title = true;
                        } else if (tag_name.equals("itemCode")) {
                            itemCode = true;
                        }

                    }
                    else if (eventType == XmlPullParser.TEXT) {
                        if (Title) {
                            titleValue = parser.getText();
                            Title = false;
                        } else if (itemCode) {
                            itemCodeName = parser.getText();
                            itemCode = false;
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG) {
                        String tag_name = parser.getName();
                        if (tag_name.equals("item")) {
                            if (itemCodeName.equals("SO2"))
                                naturalTextColorValue(tv_so2, Float.parseFloat(titleValue), itemCodeName);
                            else if (itemCodeName.equals("CO"))
                                naturalTextColorValue(tv_co, Float.parseFloat(titleValue), itemCodeName);
                            else if (itemCodeName.equals("O3"))
                                naturalTextColorValue(tv_o3, Float.parseFloat(titleValue), itemCodeName);
                            else if (itemCodeName.equals("NO2"))
                                naturalTextColorValue(tv_no2, Float.parseFloat(titleValue), itemCodeName);
                            else if (itemCodeName.equals("PM10"))
                                naturalTextColorValue(tv_pm10, Float.parseFloat(titleValue), itemCodeName);
                            else if (itemCodeName.equals("PM2.5"))
                                naturalTextColorValue(tv_pm25, Float.parseFloat(titleValue), itemCodeName);
                            Title = false;
                            itemCode = false;
                        }
                    }
                    eventType = parser.next();
                }
            } catch (Exception ex) {            //인터넷 연결 문제
            }
        }
    }

    //---------------블루투스 브로드 캐스트----------------------------------------------------
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        img_blutooth.setVisibility(View.VISIBLE);
                        if (blueToothfair == false) {
                            tv_state.setText("블루투스 활성화가 필요합니다.");
                        }
                        tv_searchBubble.setText("미세먼지를 측정하자!");
                        img_progress2.setAnimation(null);
                        img_progress2.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        }
    };

    //--------------------------블루투스 연결---------------------------------------------
    public void checkBT() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null) {                                                         // 장치가 블루투스 지원하지 않는 경우
            Toast.makeText(this, "장치가 블루투스를 지원하지 않습니다!", Toast.LENGTH_SHORT).show();
            finish();
        } else {                                                                        // 장치가 블루투스 지원하는 경우
            if (!mAdapter.isEnabled()) {                                                // 블루투스를 지원하지만 활성화 상태가 아닐 경우
                Intent intentBT = new Intent(mAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBT, REQUEST_ENABLE_BT);
            } else {
                selectDevice();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {        //블루투스 on/off 확인 다이얼로그
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {   // 블루투스 활성
                    blueToothfair = true;
                    selectDevice();
                } else if (resultCode == RESULT_CANCELED) {   // 블루투스 취소 버튼
                    blueToothfair = true;
                    tv_state.setText("블루투스 활성화가 필요합니다.");
                    img_blutooth.setVisibility(View.VISIBLE);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void selectDevice() {
        pairedDevice = mAdapter.getBondedDevices();
        pairedDeviceCount = pairedDevice.size();

        Log.d("state : ", "selectDevice");
        if (pairedDeviceCount == 0) {   // 페어링된 장치가 없을 경우
            tv_state.setText("페어링 가능한 장치가 없습니다.");
            blueToothfair = true;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final List<String> listItems = new ArrayList<String>();
        for (BluetoothDevice device : pairedDevice) {
            listItems.add(device.getName().toString());
        }
        listItems.add("취소");

        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == pairedDeviceCount) {                   // 취소 버튼 누를 시,
                    blueToothfair = false;
                    mAdapter.disable();
                } else {                                           // 기기 목록 누를 경우
                    connectToSelectedDevice(listItems.get(item));  // 기기 연결하기
                }
            }
        });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public BluetoothDevice getDeviceFromBondedList(String name) {
        BluetoothDevice selectedDevice = null;
        for (BluetoothDevice device : pairedDevice) {
            if (name.equals(device.getName())) {
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }

    public void connectToSelectedDevice(String selectedDeviceName) {
        mDevice = getDeviceFromBondedList(selectedDeviceName);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 소켓 생성(RFCOMM 채널을 통한 연결)
            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();

            // 데이터 송수신을 위한 스트림 열기
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            // 작업 데이터 송신
            sendData("dkoo");

            // 데이터 수신 준비
            beginListenForData();

            // 블루투스 연결 중 오류 발생 시,
        } catch (Exception e) {
            blueToothfair = true;
            tv_state.setText("장치를 검색할 수 없습니다.\n");
            mAdapter.disable();
        }
    }

    // 블루투스 송신
    void sendData(String msg) {
        msg += ".";
        try {                     // 문자열 전송
            mOutputStream.write(msg.getBytes());
        } catch (Exception e) {    // 전송 중 오류 발생 시,
            Toast.makeText(this, "통신 오류 발생", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // 블루투스 수신
    public void beginListenForData() {
        final Handler handler = new Handler();
        final byte[] readBuffer = new byte[1024];    // 수신 버퍼
        final float[] data = new float[1];
        readBufferPosition = 0;  //    버퍼 내 수신 문자 저장 위치
        img_blutooth.setVisibility(View.INVISIBLE);
        tv_state.setText("측정중..");
        img_progress2.setAnimation(anim2);
        img_progress2.setVisibility(View.VISIBLE);
        if (mWorkerThread == null) {
            mWorkerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            int bytesAvaliable = mInputStream.available();  // 수신 데이터 확인
                            if (bytesAvaliable > 0) {   // 데이터가 수신된 경우
                                byte[] packetBytes = new byte[bytesAvaliable];
                                mInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvaliable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == mDelimiter) {
                                        try {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                            data[0] = ByteBuffer.wrap(encodedBytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                                            readBufferPosition = 0;
                                            handler.post(new Runnable() {
                                                public void run() {
                                                    // 수신된 문자열 데이터에 대한 처리 작업
                                                    tv_dust.setText("" + (int) data[0]);
                                                    measureDustStateValueView((int) data[0]);
                                                }
                                            });
                                        }catch (Exception e){
                                            Log.d("bluetooth","data value error!"); //센서 값의 이상 현상 발생 체크
                                        }
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }
                        } catch (IOException e) {  // 데이터 수신 중 오류 발생
                            tv_state.setText("데이터 수신중 오류 발생!");
                        }
                    }
                }
            });
            mWorkerThread.start();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mWorkerThread.interrupt();  // 데이터 수신 쓰레드 종료
            mInputStream.close();
            mOutputStream.close();
            mSocket.close();
            unregisterReceiver(mBroadcastReceiver1);
        } catch (Exception e) {
        }
        super.onDestroy();
    }
}