package benliu.com.test.currencyconverter;

/**
 * interface to get the body of the http response after starting a HttpTask
 * @see HttpTask , SMHttpImageTask
 */
public interface HttpListener {
	/**
	 * the server response, called during onPostExecute of HttpTask
	 * @param response response body, empty in case of error
	 */
	public void onResponse(String response);
}
