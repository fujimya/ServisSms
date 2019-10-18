package com.example.fujimiya.servissms;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BackgroundService extends Service {
    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    String waktuku;
    private String JSON_STRING;

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
        try {
//            getJSONStok();
//            getJSONPiutang();
//            getJSONHutang();
        }catch (Exception e){
            Toast.makeText(this, "Error!"+ e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                //Toast.makeText(context, "Servis sedang berjalan", Toast.LENGTH_LONG).show();
                SimpleDateFormat formatterKu = new SimpleDateFormat("ss");
                waktuku = formatterKu.format(Calendar.getInstance().getTime());
                if(waktuku.equals("00")){
//                    showNotification();
                    Toast.makeText(context, "Servis sedang berjalan", Toast.LENGTH_LONG).show();
                    getSms();
                }
                handler.postDelayed(runnable, 1000);
            }
        };

        handler.postDelayed(runnable, 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        //Toast.makeText(this, "Notifikasi Hidup", Toast.LENGTH_LONG).show();
    }

    private void getSms(){

        class GetJSON extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //loading = ProgressDialog.show(BackgroundService.this,"Mengambil Data","Mohon Tunggu...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                loading.dismiss();
                JSON_STRING = s;
                showSms();

            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String s = rh.sendGetRequest("https://perpus-app.000webhostapp.com/Controller_Pinjam/API");
                return s;
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }

    private void setSms(final String id_sms){

        class GetJSON extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //loading = ProgressDialog.show(BackgroundService.this,"Mengambil Data","Mohon Tunggu...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                loading.dismiss();
                Toast.makeText(context,"Data Terupdate",Toast.LENGTH_SHORT).show();
                JSON_STRING = s;
//                showSms();

            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String s = rh.sendGetRequest("https://perpus-app.000webhostapp.com/Controller_Pinjam/API_SMS/"+id_sms);
                return s;
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }

    private void showSms(){

        JSONObject jsonObject = null;
//        ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
        try {
            jsonObject = new JSONObject(JSON_STRING);
            JSONArray result = jsonObject.getJSONArray("pesan");
            int barangKosong = 0;

            for(int i = 0; i<result.length(); i++){
                JSONObject jo = result.getJSONObject(i);
                String id_pinjam = jo.getString("id_pinjam");
                String nama = jo.getString("nama");
                String judul = jo.getString("judul");
                String tanggal_pinjam = jo.getString("tgl_pinjam");
                String tanggal_kembali = jo.getString("tgl_kembali");
                String sms = jo.getString("pesan");
                String nope = jo.getString("nope");
                SimpleDateFormat formatterKu = new SimpleDateFormat("yyyy-MM-dd");
                String now = formatterKu.format(Calendar.getInstance().getTime());
                Toast.makeText(context,"Sekarang : "+now+" Perbandingan :"+tanggal_kembali,Toast.LENGTH_SHORT).show();
                try {
                    Date tanggalP = formatterKu.parse(tanggal_pinjam);
                    Date sekarang = formatterKu.parse(now);
                    if(sekarang.after(tanggalP)) {
                        try {
                        SmsManager smgr = SmsManager.getDefault();
                        smgr.sendTextMessage(nope, null, sms, null, null);
                            Toast.makeText(context, "SMS Terkirim", Toast.LENGTH_SHORT).show();
                            setSms(id_pinjam);
                        } catch (Exception e) {
                            Toast.makeText(context, "SMS gagal terkirim" + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
