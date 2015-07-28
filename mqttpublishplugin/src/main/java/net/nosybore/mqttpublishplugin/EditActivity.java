package net.nosybore.mqttpublishplugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.CheckBox;

public class EditActivity extends Activity {
	
	String mServer, mPort, mUsername, mPassword, mTopic, mPayload;
    Boolean mRetain;
	EditText mServerText, mPortText, mUsernameText, mPasswordText, mTopicText, mPayloadText;
	CheckBox mRetainCheck;
	String[] mExtra;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
		
		setContentView(R.layout.main);
		mServerText = (EditText) findViewById(R.id.broker_ip);
		mPortText = (EditText) findViewById(R.id.broker_port);
		mUsernameText = (EditText) findViewById(R.id.username);
		mPasswordText = (EditText) findViewById(R.id.password);
		mTopicText = (EditText) findViewById(R.id.message_topic);
		mPayloadText = (EditText) findViewById(R.id.message_payload);
		mRetainCheck = (CheckBox) findViewById(R.id.retain);
		
		if (savedInstanceState == null) {
			if (localeBundle != null) {
				mExtra = localeBundle.getStringArray("Extra");
				mServerText.setText(localeBundle.getString(BundleExtraKeys.SERVER));
				mPortText.setText(localeBundle.getString(BundleExtraKeys.PORT));
				mUsernameText.setText(localeBundle.getString(BundleExtraKeys.USERNAME));
				mPasswordText.setText(localeBundle.getString(BundleExtraKeys.PASSWORD));
				mTopicText.setText(localeBundle.getString(BundleExtraKeys.TOPIC));
				mPayloadText.setText(localeBundle.getString(BundleExtraKeys.PAYLOAD));
                mRetainCheck.setChecked(localeBundle.getBoolean(BundleExtraKeys.RETAIN));
			}
		}
		setTitle("Settings");
	}
	
	public void finishActivity(View view) {
		
		// We get the information for each field from the settings screen
		mServer = mServerText.getText().toString();
		mPort = mPortText.getText().toString();
		mUsername = mUsernameText.getText().toString();
		mPassword = mPasswordText.getText().toString();
		mTopic = mTopicText.getText().toString();
		mPayload = mPayloadText.getText().toString();
        mRetain = (mRetainCheck.isChecked());
		
		if (mServer.length() > 0 && mPort.length() > 0 && mTopic.length() > 0 && mPayload.length() > 0) {
			Intent resultIntent = new Intent();
			
			Bundle bundle = new Bundle();
			bundle.putString(BundleExtraKeys.SERVER, mServer);
			bundle.putString(BundleExtraKeys.PORT, mPort);
			bundle.putString(BundleExtraKeys.USERNAME, mUsername);
			bundle.putString(BundleExtraKeys.PASSWORD, mPassword);
			bundle.putString(BundleExtraKeys.TOPIC, mTopic);
			bundle.putString(BundleExtraKeys.PAYLOAD, mPayload);
            bundle.putBoolean(BundleExtraKeys.RETAIN, mRetain);
			resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, bundle);
			
			// Tasker's variable replacement
			if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this))
                TaskerPlugin.Setting.setVariableReplaceKeys( bundle, new String [] { BundleExtraKeys.TOPIC, BundleExtraKeys.PAYLOAD } );
			
			// We define the blurb that will appear in the configuration
			String blurb = mServer + ":" + mPort + " => " + mTopic;
			resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);
			
			setResult(RESULT_OK,resultIntent);
			super.finish();
		}
	}
}

