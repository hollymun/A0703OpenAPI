package haru.boxy.a0703openapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AllItemActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> list;

    //ListView의 데이터를 재출력 해주는 핸들
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_item);

        listView = (ListView)findViewById(R.id.itemlist);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(
                AllItemActivity.this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        Thread th =new Thread(){
            @Override
            public void run(){
                String json ="";
                try{
                    String addr = "http://192.168.2.6:8090/allItem";
                    URL url = new URL(addr);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    //StringBuilder 자신에게 작업을 수행 - 수정가능
                    //String 자신에게 작업을 수행 못함 - 읽기전용
                    //부분적으로 읽어가면서 추가할 때는 StringBuilder를 사용
                    //String을 사용하면 계속 새로운 곳에 복사하면서 작업
                    StringBuilder sb = new StringBuilder();

                    //BufferedReader는 입출력을 모아서 처리하기 위해서 사용
                    //Buffer를 사용하지 않으면 입출력할 때마다 Native 메소드를 호출해서 입출력 효율이 떨어짐
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                                    con.getInputStream()));

                    while (true){
                        String line = br.readLine();
                        if (line == null){
                            break;
                        }
                        sb.append(line);
                    }
                    json = sb.toString();
                    br.close();
                    con.disconnect();

                }catch (Exception e){
                    Log.e("다운로드 예외", e.getMessage());
                }
        //Log.e("json", json);

                //파싱
                try{
                    //[ - 배열로 변경
                    JSONArray items = new JSONArray(json);
                    //임시변수를 만들어 사용하면 stack까지만 가도 됨
                    int len = items.length();
                    for(int i=0; i<len; i=i+1){
                        JSONObject item = items.getJSONObject(i);
                        list.add(item.getString("itemname"));
                    }
                    handler.sendEmptyMessage(0);
                }catch (Exception e){
                    Log.e("파싱예외", e.getMessage());
                }
            }
        };
        th.start();

        //리스트 뷰의 셀을 선택했을 때 이벤트 처리
        listView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id){
                //다음에 출력할 Intent 만들기
                Intent intent = new Intent(AllItemActivity.this, DetailItemActivity.class);
                //데이터 넘겨줄 때 설정
                //데이터가 2개면 putExtra를 2번해도 되지만 Map으로 묶어서 보내도 됨
                intent.putExtra("itemid",position+1);
                //화면 출력
                startActivity(intent);
                //이 전체를 들구 디테일 액티비티로 감
            }
        });
    }
}