package com.nothinglin.uploadimage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mysql.jdbc.Connection;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ImageView head;
    ImageView imageView;
    Uri uri;
    String imageString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //主线程使用网络请求
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        //注册视图组件
        textView = findViewById(R.id.textview);
        head = findViewById(R.id.imageView);
        imageView = findViewById(R.id.imageView2);

        //选择本地图片
        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,0);
            }
        });


        //数据库提取base64编码
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;

                try {
                    connection = DBOpenHelper.getConn();
                    String sql = "select head from image where username = ?";
                    preparedStatement = connection.prepareStatement(sql);

                    //将用户名为nothinglin的人的头像取出，放入imageview内
                    preparedStatement.setString(1,"nothinglin");
                    resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()){
                        byte[] imageBytes = Base64.decode(resultSet.getString("head"),Base64.DEFAULT);
                        Bitmap decodeImage = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
                        imageView.setImageBitmap(decodeImage);
                    }

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }finally {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if (resultSet != null){
                        try {
                            resultSet.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    if (preparedStatement != null){
                        try {
                            preparedStatement.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == -1){
            uri = data.getData();
            head.setImageURI(uri);

            Log.i("tt",uri.getPath());
            Log.i("tt",uri.getEncodedPath());

            //将图片转换成base64编码
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] imageBytes = baos.toByteArray();
                imageString = Base64.encodeToString(imageBytes,Base64.DEFAULT);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                Connection conn = DBOpenHelper.getConn();
                String sql = "insert into image(username,head) values(?,?)";
//                PreparedStatement preparedStatement = null;
                PreparedStatement preparedStatement = (PreparedStatement) conn.prepareStatement(sql);

                preparedStatement.setString(1, "nothinglin");
                preparedStatement.setString(2, imageString);

                int res = preparedStatement.executeUpdate();

                if (res > 0) {
                    System.out.println("Success");
                } else {
                    System.out.println("Failed");
                }

                conn.close();//记得关闭 不然内存泄漏


            } catch (SQLException throwables) {
                throwables.printStackTrace();

                textView.setText("数据库链接失败" + throwables);
            }

        }

    }
}