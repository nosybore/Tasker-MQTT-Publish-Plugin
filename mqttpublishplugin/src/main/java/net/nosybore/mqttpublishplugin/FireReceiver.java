package net.nosybore.mqttpublishplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.android.gms.security.ProviderInstaller;

import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public final class FireReceiver extends BroadcastReceiver {
	
	String mServer, mPort, mClientId, mUsername, mPassword, mTopic, mPayload, mProtocol;
    Boolean mSSL, mRetain;
	private MqttClient client;
    private int mQoS;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        	mServer = intent.getStringExtra("Server");
            mPort = intent.getStringExtra("Port");
            mClientId = intent.getStringExtra("ClientID");
            mSSL = intent.getBooleanExtra("SSL", false);
        	mUsername = intent.getStringExtra("Username");
        	mPassword = intent.getStringExtra("Password");
        	mTopic = intent.getStringExtra("Topic");
        	mPayload = intent.getStringExtra("Payload");
            mRetain = intent.getBooleanExtra("Retain", false);
            mQoS = intent.getIntExtra(BundleExtraKeys.QOS, 0);

            // select the protocol
            if (mSSL) {
                mProtocol = "ssl://";
            } else {
                mProtocol = "tcp://";
            }
            final String BROKER_URL = mProtocol + mServer + ":" + mPort;

            // set a proper client id if we have none
            if (mClientId == null || mClientId.trim().equals("")) {
                mClientId = MqttClient.generateClientId();
            }

            try {
                client = new MqttClient(BROKER_URL, mClientId, new MemoryPersistence());
            } catch (MqttException e) {
                e.printStackTrace();
                System.exit(1);
            }

            new SendMqttMessage(context).execute();
    }
	
    class SendMqttMessage extends AsyncTask<Void, Void, Void> {

        // we need the app context in here
        private Context appContext;

        public SendMqttMessage (Context context) {
            appContext = context;
        }

	    @Override
		protected Void doInBackground(Void... v) {

            try {

                final MqttTopic messageTopic = client.getTopic(mTopic);
                final MqttMessage message = new MqttMessage(String.valueOf(mPayload).getBytes());
                final MqttConnectOptions options = new MqttConnectOptions();
                message.setRetained(mRetain);
                switch (mQoS){
                    case R.id.rbQoS0:
                    default:
                        message.setQos(0);
                        break;
                    case R.id.rbQoS1:
                        message.setQos(1);
                        break;
                    case R.id.rbQoS2:
                        message.setQos(2);
                        break;
                }
                if(mUsername != null && !mUsername.trim().equals("")) {
                    options.setUserName(mUsername);
                    options.setPassword(mPassword.toCharArray());
                }

                // do we need a SSL connection?
                if (mSSL) {
                    // Create a TrustManager that trusts the CAs in our KeyStore
                    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                    tmf.init((KeyStore) null);

                    // install the required providers if needed
                    ProviderInstaller.installIfNeeded(this.appContext);

                    // Create an SSLContext that uses our TrustManager
                    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                    sslContext.init(null, tmf.getTrustManagers(), null);

                    // set the SSL options
                    options.setSocketFactory(sslContext.getSocketFactory());
                }

                client.connect(options);
                messageTopic.publish(message);

                Log.d("Receiver", "Published data. Topic: " + messageTopic.getName() + " Retain Flag: " + message.isRetained() + "  Message: " + message + " QoS:" + message.getQos());

                client.disconnect();

                return null;

            } catch (MqttException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            return null;
	    }

	    @Override
		protected void onPostExecute(Void v) { }
	}
}
