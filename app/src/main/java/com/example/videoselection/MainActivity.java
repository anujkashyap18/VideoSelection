package com.example.videoselection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    VideoView showVideo;
    ImageView pickVideo,editVideo,saveVideo,editImage;
    Uri uri;
    MediaController mediaController;
    InputStream id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showVideo = findViewById(R.id.sho_video); 
        pickVideo = findViewById(R.id.pick_video);
        editVideo = findViewById(R.id.edit_video);
        saveVideo = findViewById(R.id.edit_save);
        editImage = findViewById(R.id.edit_image);

        mediaController = new MediaController(this);

        pickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,"video/*");
                startActivityForResult(intent,2000);
            }
        });

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    //path of the video of which you want frames
                    retriever.setDataSource(String.valueOf(uri));
                }catch (Exception e) {

                }

                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int duration_millisec = Integer.parseInt(duration); //duration in millisec
                int duration_second = duration_millisec / 1000;  //millisec to sec.
                int frames_per_second = 30;  //no. of frames want to retrieve per second
                int numeroFrameCaptured = frames_per_second * duration_second;
                long frame_us=1000000/30;
                //capture=="+numeroFrameCaptured);

                    //setting time position at which you want to retrieve frames

                  Bitmap bm = retriever.getFrameAtTime(1000);


                retriever.release();
            }


        });
    }

    public String getPath ( final Context context , final Uri uri ) {

        // DocumentProvider
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri ( context , uri ) ) {

            if ( isExternalStorageDocument ( uri ) ) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId ( uri );
                final String[] split = docId.split ( ":" );
                final String type = split[ 0 ];
                String storageDefinition;


                if ( "primary".equalsIgnoreCase ( type ) ) {

                    return Environment.getExternalStorageDirectory ( ) + "/" + split[ 1 ];

                }
                else {

                    if ( Environment.isExternalStorageRemovable ( ) ) {
                        storageDefinition = "EXTERNAL_STORAGE";

                    }
                    else {
                        storageDefinition = "SECONDARY_STORAGE";
                    }

                    return System.getenv ( storageDefinition ) + "/" + split[ 1 ];
                }

            }
            else if ( isDownloadsDocument ( uri ) ) {// DownloadsProvider

                final String id = DocumentsContract.getDocumentId ( uri );
                final Uri contentUri = ContentUris.withAppendedId (
                        Uri.parse ( "content://downloads/public_downloads" ) , Long.valueOf ( id ) );

                return getDataColumn ( context , contentUri , null , null );

            }
            else if ( isMediaDocument ( uri ) ) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId ( uri );
                final String[] split = docId.split ( ":" );
                final String type = split[ 0 ];

                Uri contentUri = null;
                if ( "image".equals ( type ) ) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ( "video".equals ( type ) ) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ( "audio".equals ( type ) ) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[ 1 ]
                };

                return getDataColumn ( context , contentUri , selection , selectionArgs );
            }

        }
        else if ( "content".equalsIgnoreCase ( uri.getScheme ( ) ) ) {// MediaStore (and general)

            // Return the remote address
            if ( isGooglePhotosUri ( uri ) ) {
                return uri.getLastPathSegment ( );
            }

            return getDataColumn ( context , uri , null , null );

        }
        else if ( "file".equalsIgnoreCase ( uri.getScheme ( ) ) ) {// File
            return uri.getPath ( );
        }

        return null;
    }

    public static String getDataColumn ( Context context , Uri uri , String selection , String[] selectionArgs ) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver ( ).query ( uri , projection , selection , selectionArgs , null );
            if ( cursor != null && cursor.moveToFirst ( ) ) {
                final int column_index = cursor.getColumnIndexOrThrow ( column );
                return cursor.getString ( column_index );
            }
        } finally {
            if ( cursor != null ) {
                cursor.close ( );
            }
        }
        return null;
    }

    public static boolean isExternalStorageDocument ( Uri uri ) {
        return "com.android.externalstorage.documents".equals ( uri.getAuthority ( ) );
    }

    public static boolean isDownloadsDocument ( Uri uri ) {
        return "com.android.providers.downloads.documents".equals ( uri.getAuthority ( ) );
    }

    public static boolean isMediaDocument ( Uri uri ) {
        return "com.android.providers.media.documents".equals ( uri.getAuthority ( ) );
    }

    public static boolean isGooglePhotosUri ( Uri uri ) {
        return "com.google.android.apps.photos.content".equals ( uri.getAuthority ( ) );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2000){
            if (resultCode == RESULT_OK){
                uri = data.getData();
                showVideo.setVideoURI(uri);
                mediaController.setAnchorView(showVideo);
                showVideo.setMediaController(mediaController);
                showVideo.start();
            }
        }
    }
}