package com.example.mew.testapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CSVFile {
    InputStream inputStream;

    public CSVFile(InputStream inputStream){
        this.inputStream = inputStream;
    }

    public List<String[]> read(){
        List resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                resultList.add(row);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }
        return resultList;
    }
}

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "M Permission";
    private int PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button req = (Button) findViewById(R.id.requestButton);
        req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check The permission
                if (PermissionChecker.checkSelfPermission(
                        MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // request the permission
                    requestCameraPermission();
                    return;
                }
                Log.d(TAG, "checkSelfPermission: GRANTED");
                Toast.makeText(MainActivity.this, "パーミッションは取得済みです！やり直す場合はアプリをアンインストールするか設定から権限を再設定してください",
                        Toast.LENGTH_LONG).show();
                // TODO : Access
            }
        });

    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {

            Log.d(TAG, "shouldShowRequestPermissionRationale:追加説明");
            // 権限チェックした結果、持っていない場合はダイアログを出す
            new AlertDialog.Builder(this)
                    .setTitle("パーミッションの追加説明")
                    .setMessage("このアプリを使用するには位置使用のパーミッションが必要です")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                        }
                    })
                    .create()
                    .show();
            return;
        }

        // 権限を取得する
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length != 1 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult:DENYED");

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Log.d(TAG, "[show error]");
                    new AlertDialog.Builder(this)
                            .setTitle("パーミッション取得エラー")
                            .setMessage("再試行する場合は、再度Requestボタンを押してください")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // サンプルのため、今回はもう一度操作をはさんでいますが
                                    // ここでrequestCameraPermissionメソッドの実行でもよい
                                }
                            })
                            .create()
                            .show();

                } else {
                    Log.d(TAG, "[show app settings guide]");
                    new AlertDialog.Builder(this)
                            .setTitle("パーミッション取得エラー")
                            .setMessage("今後は許可しないが選択されました。アプリ設定＞権限をチェックしてください（権限をON/OFFすることで状態はリセットされます）")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    openSettings();
                                }
                            })
                            .create()
                            .show();
                }
            } else {
                Log.d(TAG, "onRequestPermissionsResult:GRANTED");
                // TODO 許可されたので位置情報にアクセスする
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        //Fragmentの場合はgetContext().getPackageName()
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }






    /*private int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0;

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 既に許可されているか確認
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // 許可されていなかったらリクエストする
                // ダイアログが表示される
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
                return;
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 許可された場合
            ScanResults();
        } else {
            // 許可されなかった場合
            // 何らかの対処が必要
        }
    }
*/

    public void ScanResults(View view){
        ListView lv;

        InputStream inputStream = getResources().openRawResource(R.raw.floor1);
        CSVFile csvFile = new CSVFile(inputStream);
        List<String[]> lookup = csvFile.read();

        setContentView(R.layout.activity_main);

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        assert manager != null;
        if (manager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

            // Scan WiFi
            manager.startScan();
            List<ScanResult> ap_list = manager.getScanResults();

            // Calculate distinct locations
            Integer num_locations = 0;
            String prev_location = "";
            for (int i = 0; i < lookup.size(); i++) {
                String location = lookup.get(i)[1];
                if (!location.equals(prev_location)) {
                    num_locations += 1;
                    prev_location = location;
                }
            }

            // Iterate lookup
            String location_with_most_matches = "";
            Integer most_matches = 0;
            prev_location = ""; // Reset
            Integer location_num = 0;
            Integer matches_in_location = 0;
            ArrayList<String> matches = new ArrayList<String>();
            String str_matches_in_location = "initial";
            String[] aps = new String[num_locations];
            for (int i = 0; i < lookup.size(); i++) {
                String location = lookup.get(i)[1];
                String bssid = lookup.get(i)[2];
                String rssi = lookup.get(i)[3];
                // IF NEW LOCATION
                if (i == 0 || !location.equals(lookup.get(i-1)[1])) {
                    // BUT IF NOT THE FIRST PASS
                    if (!prev_location.equals("")) {
                        // Debug print
                        aps[location_num-1] = "You are in location nr " + prev_location
                                + " with num_maches: " + matches_in_location
                                + "\n"
                                + str_matches_in_location;
                        // THEN CHECK IF PREVIOUS LOCATION WAS BIGGEST
                        if (matches_in_location >= most_matches) {
                            location_with_most_matches = prev_location;
                        }
                    }
                    // RESET VARS FOR NEW LOCATION
                    matches_in_location = 0;
                    prev_location = location;
                    location_num += 1;
                    matches.clear();
                }
                // CHECK IF LINE MATCHES ANY OF THE LOOKUP BSSIDs
                Boolean match = false;
                for (int j = 0; j < ap_list.size(); j++) {
                    if (ap_list.get(j).BSSID.trim().equals(bssid.trim())) {
                        match = true;
                        matches.add(bssid.trim());
                    }
                }
                str_matches_in_location = String.join(",", matches);
                if (match) {
                    matches_in_location += 1;
                }
            }
            // Debug print
            aps[location_num-1] = "You are in location nr " + prev_location
                    + " with num_maches: " + matches_in_location
                    + "\n"
                    + str_matches_in_location;
            if (matches_in_location >= most_matches) {
                location_with_most_matches = prev_location;
            }
            //aps[i] = "BSSID:" + ap_list.get(i).BSSID + "\n" + ap_list.get(i).frequency + "MHz " + ap_list.get(i).level + "dBm";
            lv = (ListView) findViewById(R.id.listview_id);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, aps);
            lv.setAdapter(adapter);
        }
    }
}



