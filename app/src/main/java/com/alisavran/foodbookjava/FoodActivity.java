package com.alisavran.foodbookjava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class FoodActivity extends AppCompatActivity {

    private com.alisavran.foodbookjava.databinding.ActivityFoodBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedFood;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.alisavran.foodbookjava.databinding.ActivityFoodBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("Foods",MODE_PRIVATE,null);


        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.equals("new")){
            binding.foodText.setText("");
            binding.infoText.setText("");
            binding.imageView.setImageResource(R.drawable.selectfood);
            binding.button.setVisibility(View.VISIBLE);
        }else {
            int foodId = intent.getIntExtra("foodId",0);
            binding.button.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM foods WHERE id = ?",new String[] {String.valueOf(foodId)});
                int foodnameIx = cursor.getColumnIndex("foodname");
                int foodinfoIx = cursor.getColumnIndex("foodinfo");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.foodText.setText(cursor.getString(foodnameIx));
                    binding.infoText.setText(cursor.getString(foodinfoIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();

            }catch(Exception e){
                e.printStackTrace();
            }



        }
    }

    public void selectFood(View view){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){//ANDROİD 33 plus => read_media_images

            /*depolama izni verilmemiş mi kontrolü*/
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){//İZİN İSTEME MANTIĞINI AÇIKLAMA
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission (izin isteme)
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                }else {
                    //request permission (izin isteme)
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            }else {
                //Gallery(izin verilmiş galeriye git)
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//Galeriden görsel alma işlemi
                activityResultLauncher.launch(intentToGallery);
            }

        }else{//Android 32-- => read_external_storage

            /*depolama izni verilmemiş mi kontrolü*/
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){//İZİN İSTEME MANTIĞINI AÇIKLAMA
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission (izin isteme)
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }else {
                    //request permission (izin isteme)
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }else {
                //Gallery(izin verilmiş galeriye git)
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//Galeriden görsel alma işlemi
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }

    public void save(View view){

        String name = binding.foodText.getText().toString();
        String info = binding.infoText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedFood,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS foods ( id INTEGER PRIMARY KEY, foodname VARCHAR, foodinfo VARCHAR , image BLOB) ");

            String sqlString = "INSERT INTO foods(foodname, foodinfo, image ) VALUES( ? , ? , ? )";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);

            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,info);
            sqLiteStatement.bindBlob(3,byteArray);
            sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(FoodActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if(bitmapRatio >1){
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        }else{
            height = maximumSize;
            width = (int) (width * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);

    }

    private void registerLauncher(){ // activitiyresultlauncherların tanımı
        //activityresultlauncher sonuca göre aktiviteyi başlatmasını tanımladık
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData(); //sonuca göre veriyi alma işlemi
                    if(intentFromResult != null){ //aldığımız verinin boş olup olmadığını kontrol ediyoruz
                        Uri imageData =  intentFromResult.getData(); // kullanıcının seçtiği görselin yerini söylüyor

                        try {
                            //ImageDecoder alınan görüntüyü ilemeyi sağlar
                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                            selectedFood = ImageDecoder.decodeBitmap(source); //seçilen görüntüyü bitmape çevirme
                            binding.imageView.setImageBitmap(selectedFood);
                        }catch (Exception e){
                            e.printStackTrace(); // uygulamayı çökme olursa kullanıcıya uygulamayı çökertmeden hata mesajı verir.
                        }

                    }
                }
            }
        });
        //permission launcherın izin alma old. tanımı
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //permission granted(izin verildi)
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//Galeriden görsel alma işlemi
                    activityResultLauncher.launch(intentToGallery);
                }else{
                    //permission denied(izin verilmedi)
                    Toast.makeText(FoodActivity.this, "Permission needed !!!!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}