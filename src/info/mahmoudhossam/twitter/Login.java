package info.mahmoudhossam.twitter;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import twitter4j.auth.AccessToken;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Login extends ListActivity {

	private static final int OAUTH_REQUEST = 1;
	private static final String PREFS = "prefs";
	private TwitterBackend backend;
	SharedPreferences prefs;
	static String consumerKey;
	static String consumerSecret;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeVariables();
		if (tokenExists()) {
			backend.twitterInit(getToken());
			startActivity(new Intent("mahmoud.tweets"));
			finish();
		} else {
			try {
				login();
			} catch (OAuthMessageSignerException e) {
				logError(e);
			} catch (OAuthNotAuthorizedException e) {
				logError(e);
			} catch (OAuthExpectationFailedException e) {
				logError(e);
			} catch (OAuthCommunicationException e) {
				logError(e);
			}
		}

	}

	private void logError(Exception e) {
		Log.e("Twitter", e.getMessage());
	}

	private void initializeVariables() {
		consumerKey = getResources().getString(R.string.consumer_key);
		consumerSecret = getResources().getString(R.string.consumer_secret);
		backend = new TwitterBackend();
		prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
	}

	private void login() throws OAuthMessageSignerException,
			OAuthNotAuthorizedException, OAuthExpectationFailedException,
			OAuthCommunicationException {
		Intent intent = new Intent("mahmoud.browser");
		Log.i("URL", backend.getAuthorizationURL());
		intent.putExtra("url", backend.getAuthorizationURL());
		startActivityForResult(intent, OAUTH_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == OAUTH_REQUEST && resultCode == RESULT_OK) {
			Uri url = Uri.parse(data.getExtras().getString("url"));
			String verifier = url.getQueryParameter("oauth_verifier");
			Log.i("Verifier", verifier);
			try {
				backend.setAccessToken(verifier);
			} catch (OAuthMessageSignerException e) {
				logError(e);
			} catch (OAuthNotAuthorizedException e) {
				logError(e);
			} catch (OAuthExpectationFailedException e) {
				logError(e);
			} catch (OAuthCommunicationException e) {
				logError(e);
			}
			backend.twitterInit();
			saveToken();
			startActivity(new Intent("mahmoud.tweets"));
			finish();
		} else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this,
					"Cannot connect to twitter, app not authorized",
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean tokenExists() {
		return prefs.contains("oauth_token") && prefs.contains("oauth_secret");
	}

	private void saveToken() {
		AccessToken token = backend.getAccessToken();
		if (token != null) {
			Editor editor = prefs.edit();
			editor.putString("oauth_token", token.getToken());
			editor.putString("oauth_secret", token.getTokenSecret());
			editor.commit();
		}
	}

	private AccessToken getToken() {
		String token = prefs.getString("oauth_token", null);
		String secret = prefs.getString("oauth_secret", null);
		AccessToken accessToken = new AccessToken(token, secret);
		return accessToken;
	}

}