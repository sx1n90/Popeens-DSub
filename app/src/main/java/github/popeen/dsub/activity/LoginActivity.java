package github.popeen.dsub.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import github.popeen.dsub.R;
import github.popeen.dsub.service.MusicService;
import github.popeen.dsub.service.MusicServiceFactory;
import github.popeen.dsub.util.Constants;
import github.popeen.dsub.util.Util;

public class LoginActivity extends Activity {


	private UserLoginTask mAuthTask = null;

	// UI references.
	private EditText mAddressView;
	private AutoCompleteTextView mUsernameView;
	private EditText mPasswordView;
	private View mProgressView;
	private View mLoginFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// Set up the login form.
		mAddressView = (EditText) findViewById(R.id.address);
		mUsernameView = (AutoCompleteTextView) findViewById(R.id.email);
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

		Button mRegisterButton = (Button) findViewById(R.id.register_button);
		mRegisterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);
	}


	/**
	 * Attempts to sign in
	 * If there are form errors (invalid url, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mAddressView.setError(null);
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String address = mAddressView.getText().toString();
		String username = mUsernameView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid address, if the user entered one.
		if (TextUtils.isEmpty(address) || TextUtils.substring(address, 0, 3).equals("http")) {
			mAddressView.setError("Invalid URL");
			focusView = mAddressView;
			cancel = true;
		}

		// Check for a valid password, if the user entered one.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(username)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mAuthTask = new UserLoginTask(address, username, password);
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress(final boolean show) {
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		mLoginFormView.animate().setDuration(shortAnimTime).alpha(
				show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			}
		});

		mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		mProgressView.animate().setDuration(shortAnimTime).alpha(
				show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		});
	}
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		private final String mAddress;
		private final String mEmail;
		private final String mPassword;

		private Context context;

		UserLoginTask(String address, String email, String password) {
			mAddress = address;
			mEmail = email;
			mPassword = password;
			context = getApplicationContext();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			SharedPreferences prefs = Util.getPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(Constants.PREFERENCES_KEY_OFFLINE, false);

			editor.putString(Constants.PREFERENCES_KEY_SERVER_NAME + 1, "Booksonic");
			editor.putString(Constants.PREFERENCES_KEY_SERVER_URL + 1, mAddress);
			editor.putString(Constants.PREFERENCES_KEY_USERNAME + 1, mEmail);
			editor.putString(Constants.PREFERENCES_KEY_PASSWORD + 1, mPassword);
			editor.putInt(Constants.PREFERENCES_KEY_SERVER_INSTANCE, 1);
			editor.commit();

			MusicService musicService = MusicServiceFactory.getMusicService(context);
			try {
				musicService.setInstance(1);
				musicService.ping(context, null);
				return musicService.isLicenseValid(context, null);
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
			} else {
				SharedPreferences prefs = Util.getPreferences(context);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(Constants.PREFERENCES_KEY_OFFLINE, false);

				editor.putString(Constants.PREFERENCES_KEY_SERVER_NAME + 1, "Booksonic");
				editor.putString(Constants.PREFERENCES_KEY_SERVER_URL + 1, "http://demo.booksonic.org/booksonic/");
				editor.putString(Constants.PREFERENCES_KEY_USERNAME + 1, "demo");
				editor.putString(Constants.PREFERENCES_KEY_PASSWORD + 1, "demo");
				editor.putInt(Constants.PREFERENCES_KEY_SERVER_INSTANCE, 1);
				editor.commit();

				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	@Override
	public void onBackPressed(){
		this.moveTaskToBack(true);
	}
}
