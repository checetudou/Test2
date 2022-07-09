package com.edu.zust.test2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

//    private final String[] fnames = {
//            "hug", "laugh", "peep", "snore", "stop",
//            "tired", "full", "what", "afraid", "no_way",
//            "hug", "laugh", "peep", "snore", "stop",
//            "tired", "full", "what", "afraid", "no_way"
//    };
    ImageView[] imageViews = new ImageView[20];
    ArrayList<Bitmap> imgBits = new ArrayList<Bitmap>();
    ProgressBar progressBar;
    Content content = null;
    int clickCount = 0;
    LinearLayout gallery;
    TextView textView;
    String url;
    Toast success, error;
    //File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    private int imagecount = 0;

    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
        success = Toast.makeText(this, "Download Completed!", Toast.LENGTH_SHORT);
        error = Toast.makeText(this, "Please use another URL", Toast.LENGTH_LONG);

//        MyCustomAdapter adapter = new MyCustomAdapter(this, 0);
//        adapter.setData(fnames);

        gallery = findViewById(R.id.gallery);
        textView = findViewById(R.id.progress_text);

        progressBar=findViewById(R.id.progress_bar);
        progressBar.setMax(20);

        loadDefaultImageViews();

        Button fetchBtn = findViewById(R.id.fetchBtn);
        if(fetchBtn!=null){
            fetchBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    EditText newUrl = findViewById(R.id.urlBar);
                    if(newUrl!= null){
                        url=newUrl.getText().toString();
                    }
                    hideKeyboard(view);
                    revertToDefault();

                    if (content != null) {
                        content.cancel(true);
                    }
                    content = new Content();
                    content.execute();
                }
            });
        }
    }

    private class Content extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(ProgressBar.VISIBLE);
            textView.setVisibility(textView.VISIBLE);
        }
        //下载图片downloadimage
        private void getWebImage(String url) {
            try {
                //获取地址get url
                URL httpUrl = new URL(url);
                //打开网络连接open connection
                HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                //设置超时断开连接disconnect over 5s
                //conn.setReadTimeout(5000);
                //设置读取数据的方式
                conn.setRequestMethod("GET");
                //设置一个输入流
                conn.setDoInput(true);
                //获取输入流
                InputStream inputStream = conn.getInputStream();

                //系统时间 System time
                String strDate = String.valueOf(System.currentTimeMillis());
                //文件输出流对象
                FileOutputStream out = null ;
                //创建子目录
                File downloadFile = null ;
                //判断是否有SD卡 if sd card exists
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    //SD卡的目录
                    File parentFile = Environment.getExternalStorageDirectory();
                    //创建子目录，这里如果不加“.JPG”同样能在ImageView中显示，但是不能再图库中显示。parentFile，是父级目录，必须存在，第二个参数可以自己随便写
                    downloadFile = new File(parentFile,strDate+".jpg");

                    //下载图片到本地的SD卡中
                    out = new FileOutputStream(downloadFile);
                }
                //字节流
                byte[] b = new byte[2*1024];
                int len ;
                if(out!=null){
                    //当长度为 -1 表示没有内容可读了
                    while ((len=inputStream.read(b))!=-1) {
                        //写入SD卡文件中
                        out.write(b, 0, len);
                    }
                }

                System.out.println("==========================="+downloadFile.getAbsolutePath());


            } catch (MalformedURLException | ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        public void delAllFile(String path) {
            File file = new File(path);
            if (!file.exists()) {
                return;
            }
            if (!file.isDirectory()) {
                return;
            }
            String[] tempList = file.list();
            File temp = null;
            for (int i = 0; i < tempList.length; i++) {
                if (path.endsWith(File.separator)) {
                    temp = new File(path + tempList[i]);
                }
                else {
                    temp = new File(path + File.separator + tempList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
                if (temp.isDirectory()) {
                    delAllFile(path+"/"+ tempList[i]);//先删除文件夹里面的文件
                }

            }

        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Document document = Jsoup.connect(url).get();
                //select all img elements
                Elements imgs = document.select("img[src~=(?i)\\.(png|jpe?g)]");
                //SD卡的目录
                File parentFile = Environment.getExternalStorageDirectory();
                delAllFile(parentFile.toString());
                ListIterator<Element> elementIt = imgs.listIterator();

                for (int i = 0; i < 20; i++) {

                    if (elementIt.hasNext()) {

                        String imgSrc = elementIt.next().absUrl("src");
                        Log.i("data",imgSrc);
                        getWebImage(imgSrc);
                        InputStream input = new java.net.URL(imgSrc).openStream();
                        Bitmap imgbit = BitmapFactory.decodeStream(input);

                        //crop the downloaded img to imageView ratio
                        float imgBitRatio = (float) imgbit.getHeight() / imgbit.getWidth();
                        float imgViewRatio = (float) imageViews[1].getMeasuredHeight() / imageViews[1].getMeasuredWidth();
                        if (imgViewRatio > imgBitRatio) {
                            int imgbitWidth = (int) (imgbit.getHeight() / imgViewRatio);
                            int startPosX = (int) (imgbit.getWidth() - (imgbit.getHeight() / imgViewRatio)) / 2;
                            imgbit = Bitmap.createBitmap(imgbit, startPosX, 0, imgbitWidth, imgbit.getHeight());
                        } else {
                            int imgbitHeight = (int) (imgbit.getWidth() * imgViewRatio);
                            int startPosY = (int) (imgbit.getHeight() - (imgbit.getWidth() * imgViewRatio)) / 2;
                            imgbit = Bitmap.createBitmap(imgbit, 0, startPosY, imgbit.getWidth(), imgbitHeight);
                        }

                        if (isCancelled()) {
                            return null;
                        }
                        imgBits.add(imgbit);
                        progressBar.incrementProgressBy(1);
                        publishProgress(i);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (!isCancelled()) {
                imageViews[values[0]].setImageBitmap(imgBits.get(values[0]));
                textView.setText(values[0] + 1 + "/" + progressBar.getMax());
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(ProgressBar.INVISIBLE);

            if (imgBits.size() < 6) {
                textView.setVisibility(View.INVISIBLE);
                error.show();
            }
        }
    }

    private void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void revertToDefault() {
        imgBits.clear();
        clickCount = 0;
        progressBar.setProgress(0);
        textView.setText("0/" + progressBar.getMax());
        for (ImageView iv : imageViews) {
            iv.setImageResource(R.drawable.peep);
            iv.setForeground(null);
            iv.setClickable(false);
        }
        for (View v : imageViews) {
            v.setAlpha(1);
        }
    }

    private void loadDefaultImageViews() {
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        linearParams.setMargins(10, 0, 10, 0);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        ivParams.setMargins(10, 10, 10, 10);

        int column, row;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            column = 4;
            row = 5;
        } else {
            column = 10;
            row = 2;
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) gallery.getLayoutParams();
            lp.height = 480;
            gallery.setLayoutParams(lp);
        }

        int count = 0;
        for (int i = 0; i < row; i++) {
            LinearLayout layout = new LinearLayout(this);
            layout.setWeightSum(column);
            layout.setLayoutParams(linearParams);

            for (int j = 0; j < column; j++) {
                ImageView iv = new ImageView(this);
                iv.setImageResource(R.drawable.peep);
                iv.setLayoutParams(ivParams);
                iv.setId(count);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViews[count] = iv;
                layout.addView(iv);
                count++;
            }
            gallery.addView(layout);
        }
    }
}
