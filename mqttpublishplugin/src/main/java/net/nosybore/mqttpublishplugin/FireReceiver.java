package net.nosybore.mqttpublishplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public final class FireReceiver extends BroadcastReceiver {
	
	private MqttClient client;
	String mServer, mPort, mUsername, mPassword, mTopic, mPayload;
	
	class SendMqttMessage extends AsyncTask<Void, Void, Void> {

	    @Override
		protected Void doInBackground(Void... v) {

            try {
                client.connect();

                final MqttTopic messageTopic = client.getTopic(mTopic);
                final MqttMessage message = new MqttMessage(String.valueOf(mPayload).getBytes());
                
                messageTopic.publish(message);
                
                Log.d("Receiver", "Published data. Topic: " + messageTopic.getName() + "  Message: " + message);

                client.disconnect();
                
                return null;

            } catch (MqttException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return null;
	    }

	    @Override
		protected void onPostExecute(Void v) { }
	}
	
    @Override
    public void onReceive(final Context context, final Intent intent) {    
    	
        	mServer = intent.getStringExtra("Server");
        	mPort = intent.getStringExtra("Port");
        	mUsername = intent.getStringExtra("Username");
        	mPassword = intent.getStringExtra("Password");
        	mTopic = intent.getStringExtra("Topic");
        	mPayload = intent.getStringExtra("Payload");
        	       	
        	final String BROKER_URL = "tcp://"+mServer+":"+mPort;
            
            try {
                client = new MqttClient(BROKER_URL, MqttClient.generateClientId(), new MemoryPersistence());
            } catch (MqttException e) {
                e.printStackTrace();
                System.exit(1);
            }
            
            new SendMqttMessage().execute();  
    }
}
