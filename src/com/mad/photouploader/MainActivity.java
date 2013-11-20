package com.mad.photouploader;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int CAMERA_RESULT = 7;
	private Uri pictureUri;
	private ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onTakePhotoButtonClicked(View v) {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        pictureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + File.separator + "temp.png"));
        camera.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(camera, CAMERA_RESULT);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
            	showPreview();
            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }
	
	public void onUploadButtonClicked(View v) {
		if (pictureUri != null) {
			new UploadTask().execute();
		}
	}
	
	private void showPreview() {
        imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setImageURI(null);
        imageView.setImageURI(pictureUri);
	}

	private void uploadPicture() {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

			HttpPost httppost = new HttpPost("http://54.203.254.210:4567/upload");
			File file = new File(pictureUri.getPath());

			MultipartEntity mpEntity = new MultipartEntity();
			ContentBody cbFile = new FileBody(file, "image/png");
			mpEntity.addPart("file", cbFile);


			httppost.setEntity(mpEntity);
			System.out.println("executing request " + httppost.getRequestLine());
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();

			System.out.println(response.getStatusLine());
			if (resEntity != null) {
				System.out.println(EntityUtils.toString(resEntity));
			}
			if (resEntity != null) {
				resEntity.consumeContent();
			}

			httpclient.getConnectionManager().shutdown();
		}
		catch (Exception e) {
			Log.e("Uploader", "Problem uploading " + e.getMessage() + " " + e.getStackTrace());
			Log.e("Uploader", e.getClass().toString());
		}
	}
	
	 private class UploadTask extends AsyncTask<Void, Void, Void> {
		 
		 @Override
		 protected void onPreExecute() {
			 Toast.makeText(getApplicationContext(), "Uploading Picture, please wait", Toast.LENGTH_SHORT).show();
			 findViewById(R.id.uploadButton).setEnabled(false);
			 findViewById(R.id.takePhotoButton).setEnabled(false);
		 }

		@Override
		protected Void doInBackground(Void... params) {
			uploadPicture();
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			Toast.makeText(getApplicationContext(), "Done uploading!", Toast.LENGTH_SHORT).show();
			imageView.setImageBitmap(null);
			pictureUri = null;
			findViewById(R.id.uploadButton).setEnabled(true);
			findViewById(R.id.takePhotoButton).setEnabled(true);
		}
	 }
}
