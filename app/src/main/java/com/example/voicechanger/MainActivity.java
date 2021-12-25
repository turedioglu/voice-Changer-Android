package com.example.voicechanger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

    public static Boolean recording;
    private Spinner spFrequency;
    ImageView imageView1, playBack, startRec;
    ArrayAdapter<String> adapter;

    AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] arrayOfStrings = new String[8];
        arrayOfStrings[0] = "Ghost";
        arrayOfStrings[1] = "Slow Motion";
        arrayOfStrings[2] = "Robot";
        arrayOfStrings[3] = "Normal";
        arrayOfStrings[4] = "Chipmunk";
        arrayOfStrings[5] = "Funny";
        arrayOfStrings[6] = "Bee";
        arrayOfStrings[7] = "Elephant";

        startRec = (ImageView) findViewById(R.id.startrec);
        imageView1 = (ImageView) findViewById(R.id.imageViewMic);
        playBack = (ImageView) findViewById(R.id.startplay);
        spFrequency = (Spinner) findViewById(R.id.frequency);


        startRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecordingDialog.class);
                startActivity(intent);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        recording = true;
                        try {
                            startRecord();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        playBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (file.exists()) {
                    try {
                        playRecord();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayOfStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrequency.setAdapter(adapter);
        imageView1.setVisibility(View.VISIBLE);
    }

    private void playRecord() throws IOException {
        int i= 0;
        String str = (String) spFrequency.getSelectedItem();
        int shortSizeInBytes = Short.SIZE / Byte.SIZE;
        int bufferSizeInBytes = (int) (file.length() / shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        InputStream inputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

        int j=0;
        while(dataInputStream.available() >0){
            audioData[j] = dataInputStream.readShort();
            j++;
        }

        dataInputStream.close();
        if (str.equals("Ghost")) {

            i=5000;
        }
        if (str.equals("Slow Motion")) {

            i=6050;
        }
        if (str.equals("Robot")) {

            i=8500;
        }
        if (str.equals("Normal")) {

            i=11025;
        }
        if(str.equals("Chipmunk")){
            i=16000;
        }
        if(str.equals("Funny")){
            i=22050;
        }
        if(str.equals("Bee")){
            i=41000;
        }
        if(str.equals("Elephant")){
            i=30000;
        }
        audioTrack = new AudioTrack(3,i,2,2,bufferSizeInBytes,1);

        audioTrack.play();
        audioTrack.write(audioData,0,bufferSizeInBytes);
    }

    private void startRecord() throws IOException {
        File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "test.pcm");
        myFile.createNewFile();

        OutputStream outputStream = new FileOutputStream(myFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
        int minBufferSize = AudioTrack.getMinBufferSize(11025, 2, 2);

        short[] audioData = new short[minBufferSize];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        AudioRecord audioRecord = new AudioRecord(1, 11025, 2, 2, minBufferSize);

        audioRecord.startRecording();

        while(recording){
            int numberOfShort = audioRecord.read(audioData, 0,minBufferSize);
            for(int i=0; i<numberOfShort;i++ ){
                dataOutputStream.writeShort(audioData[i]);
            }
        }

        if(recording.booleanValue()){
            audioRecord.stop();
            dataOutputStream.close();
        }
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        recording = false;
        if(audioTrack != null){
            audioTrack.release();
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}