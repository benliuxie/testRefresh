package benliu.com.test.currencyconverter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;


/**
 * This class sends an http request to an url in a new thread
 * The response contains the http response body (but not the response code)
 * The response is given as a string to the listener.
 * The arguments given to execute method are:
 * - the url string (mandatory)
 * - the payload string if method is "POST" (optional) 
 * @see HttpListener
 */
public class HttpTask extends AsyncTask<String, Void, byte[]> {
	static private final String TAG = "HttpTask";
	
	// requests
	static public final String GET = "GET";
	static public final String POST = "POST";
	static public final String PUT = "PUT";
	static private final String CONTENT_TYPE = "Content-Type"; 
	
	// request string : GET, POST, PUT
	private String mRequestMethod;
	// listener
	protected final HttpListener mListener;
  // number of retry attempts left
	private int mRetry = 8;
	
	public HttpTask() {
		this(GET, null);
	}
	
	public HttpTask(HttpListener listener) {
		this(GET, listener);
	}

	public HttpTask(String method, HttpListener listener) {
		this.mRequestMethod = method;
		mListener = listener;
	}
	
	// Precondition: args[0] is url, args[1] is payload String to send to server if method is "POST"
	@Override
	protected byte[] doInBackground(String... args) {
		if (args.length == 0) {
			Log.e(TAG, "Url parameter is absent");
			return null;
		}
		String urlString = args[0];
		String payload = null;
		if (args.length > 1) {
			payload = args[1];
		}
		
		byte[] result = null;
		
		while(mRetry-- > 0){
			 result = request(urlString,payload);
			 if (result != null)
				 mRetry = 0;
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(byte[] result) {
//		Log.d(TAG, "onPostExecute");
		if (mListener != null) {
			String response;
			if (result == null) {
				Log.d(TAG, "response is null");
				response = new String();
				//Utility.showGenericErrorMessage(null, SMApplication.getApplication().getString(R.string.generic_network_error), null);
			} else {
				response = new String(result);
				Log.v(TAG, "response: "+response);
			}
			mListener.onResponse(response);
		} else {
			Log.e(TAG, "listener is null");
		}
	}

	private byte[] readBytesFromInputStream(InputStream in)
			throws IOException {

		InputStream inputStream = new BufferedInputStream(in);
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		byte[] resultBytes = null;
		try {


			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];

			int len = 0;
			while ((len = inputStream.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, len);
			}
			resultBytes = byteBuffer.toByteArray();
		} catch (Exception e) {
			Log.e(TAG, "readBytesFromInputStream Exception");
		} finally {
			try {
				if (byteBuffer != null) {
					byteBuffer.close();
					byteBuffer = null;
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				Log.e(TAG, "readBytesFromInputStream Exception in finally");
			}
		}
		return resultBytes;
	}


	/**
	 * send the request
	 * @param urlString url string
	 * @param payload payload string if method is POST (optional)
	 * @return the response body, null in case of error 
	 */
	protected byte[] request(String urlString, String payload) {
		byte[] bytes = null;
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		Log.d(TAG, "request url:"+urlString);
		try {
			URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setConnectTimeout(10000);
			
			int responseCode = urlConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				inputStream = urlConnection.getInputStream();
				bytes = readBytesFromInputStream(inputStream);

			} else {
				Log.e(TAG, String.format("Response Code: %d", responseCode+", for url: "+urlString));
			}
		} catch (IOException exception) {
			Log.e(TAG, "An IO exception occured("+exception.getClass().getCanonicalName()+") with url: "+urlString);
		} catch (Exception e) {
			Log.e(TAG, "An exception occured with url: " + urlString + " :"+e.toString()); 
		} finally {
			try {
				if (urlConnection != null) {
					urlConnection.disconnect();
				}
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
			} catch (Exception e) {
				Log.e(TAG, "An exception occured during finally with url: "+urlString+e.toString());
			}
		}
		return bytes;
	}
}
