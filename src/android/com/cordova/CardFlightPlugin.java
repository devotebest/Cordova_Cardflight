package com.cordova.cardflightplugin;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.*; // Cordova 3.x

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import android.content.Context;

import com.getcardflight.interfaces.*;
import com.getcardflight.models.Card;
import com.getcardflight.models.CardFlight;
import com.getcardflight.models.Charge;
import com.getcardflight.models.Reader;
import com.getcardflight.views.PaymentView;


public class CardFlightPlugin extends CordovaPlugin implements Events.Required {
    
	Hapi api;
    Device device;
    @SuppressWarnings("unused")
	private CallbackContext callbackContext;
    private CallbackContext List_callbackContext;
    
    // Debugging
    private static final String TAG = "HandPoint";
    private static final boolean D = true;
    
    private String[] ConnectioMethod = {"USB", "SERIAL", "BLUETOOTH", "HTTPS", "WIFI", "ETHERNET", "SIMULATOR"};
    

    //Receiving a list of connectable devices
    List<Device> myListOfDevices;
    
    private String sharedSecret_key;
    private String deviceName;
    private Context appContext;
    private boolean done;
    
    public CardFlightPlugin() {
    	
    }
    
    public CardFlightPlugin(Context context){
        //initApi(context);
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    	 LOG.d(TAG, "action = " + action);
    	 
    	 appContext = this.cordova.getActivity().getApplicationContext();
         
    	 this.callbackContext = callbackContext;
         
         boolean retValue = true;
         if (action.equals("init")) {
             //Initialize  API
        	 initApi(appContext, callbackContext); 
        	 retValue = true;
        	 
         } else if (action.equals("pay")) {
        	 //Pay
        	 pay(args, callbackContext);
        	 retValue = true;
        	 
         } else if (action.equals("connect")) {
             //Connect Device
        	 connect(args, callbackContext);
        	 retValue = true;
        	 
         } else if (action.equals("connectWithCurrentDevice")) {
             //Connect Device
        	 connect(callbackContext);
        	 retValue = true;
        	 
         } else if (action.equals("ListDevices")) {
             //Connect Device
        
			ListDevices(callbackContext);
			
        	 retValue = true;
        	 
         } else if (action.equals("SetMerchantKey")) {
             //Connect Device
        	 SetMerchantKey(args, callbackContext);
        	 retValue = true;
        	 
         } else if (action.equals("SetDeviceName")) {
             //Connect Device
        	 SetDeviceName(args, callbackContext);
        	 retValue = true;
        	 
         }   else {
             retValue = false;
             
         }

         return retValue;
    }

    public void initApi(Context context, CallbackContext callbackContext){
      
        this.api = HapiFactory.getAsyncInterface(this, context).defaultSharedSecret(sharedSecret_key);
      //Register a listener for required events
        this.api.addRequiredEventHandler(this);
        
        //The api is now initialized. Yay! we've even set a default shared secret!
        //But we need to connect to a device to start taking payments.
        //Let's search for them:
        this.api.listDevices(ConnectionMethod.BLUETOOTH);
        
        //This triggers the search, you should expect the results in the listener defined above
        callbackContext.success();
    }
    
  //You should populate this method as is you see fit.
  //Here we assume the name of a device and use it.
  //Connection to the device is handled automatically in the API
  @Override
  public void deviceDiscoveryFinished(List<Device> devices){

	  myListOfDevices = devices;
      for(Device device : devices){
    	  if(device.getName().equals(deviceName)){
    		  //We'll remember the device for this session, but is cool that you do too
              this.device = device;
              this.api.useDevice(this.device);
          }
      }
      
      
      JSONArray deviceList = new JSONArray();
	  	
	  	for(Device device : myListOfDevices){
	  		 JSONObject json = new JSONObject();
            try {
				json.put("name", device.getName());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            try {
				json.put("address", device.getAddress());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            try {
				json.put("port", device.getPort());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            deviceList.put(json);
	      }
  	
	  	List_callbackContext.success(deviceList);
  }
    
    public void connect(JSONArray args, CallbackContext callbackContext) throws JSONException {
    	JSONObject obj = args.optJSONObject(0);
    	String name = obj.optString("name");
        String address = obj.optString("address");
    	String port = obj.optString("port");
    	String connectionMethod = obj.optString("method");
    	ConnectionMethod method;
    	
    	method = ConnectionMethod.BLUETOOTH;
    	if(connectionMethod == "USB") {
    		method = ConnectionMethod.USB;
    	}
    	if(connectionMethod == "SERIAL") {
    		method = ConnectionMethod.SERIAL;
    	}
    	if(connectionMethod == "BLUETOOTH") {
    		method = ConnectionMethod.BLUETOOTH;
    	}
    	if(connectionMethod == "HTTPS") {
    		method = ConnectionMethod.HTTPS;
    	}
    	if(connectionMethod == "WIFI") {
    		method = ConnectionMethod.WIFI;
    	}
    	if(connectionMethod == "ETHERNET") {
    		method = ConnectionMethod.ETHERNET;
    	}
    	if(connectionMethod == "SIMULATOR") {
    		method = ConnectionMethod.SIMULATOR;
    	}
    	
    	Device device = new Device(name, address, port, method);

    	Hapi bFlag = api.useDevice(device);
    	
    	callbackContext.success();
    }
    
    
    public void connect(CallbackContext callbackContext) throws JSONException {
    	for(Device device : myListOfDevices){
      	  if(device.getName().equals(deviceName)){
      		  //We'll remember the device for this session, but is cool that you do too
                this.device = device;
                this.api.useDevice(this.device);
            }
        }

    	Hapi bFlag = api.useDevice(device);
    	
    	callbackContext.success();
    }
    
    public void disconnect() throws JSONException {
    	//Disconnect from current device
    	api.disconnect();
    }
    
    
    public boolean pay(JSONArray args, CallbackContext callbackContext) throws JSONException{
    	String price;
    	String currency;
    	JSONObject obj = args.optJSONObject(0);
    	
    	price = obj.optString("price");
        currency = obj.optString("currency");
    	
    	Currency _currency;
    	_currency = Currency.GBP;
    	if(currency == "GBP") {
    		_currency = Currency.GBP;
    	}
    	if(currency == "ZAR") {
    		_currency = Currency.ZAR;
    	}
    	if(currency == "USD") {
    		_currency = Currency.USD;
    	}
    	if(currency == "EUR") {
    		_currency = Currency.EUR;
    	}
    	if(currency == "CNY") {
    		_currency = Currency.CNY;
    	}
    	if(currency == "EGP") {
    		_currency = Currency.EGP;
    	}
    	if(currency == "INR") {
    		_currency = Currency.INR;
    	}
    	if(currency == "UAH") {
    		_currency = Currency.UAH;
    	}
    	if(currency == "TWD") {
    		_currency = Currency.TWD;
    	}
    	if(currency == "AUD") {
    		_currency = Currency.AUD;
    	}
    	if(currency == "CAD") {
    		_currency = Currency.CAD;
    	}
    	if(currency == "SGD") {
    		_currency = Currency.SGD;
    	}
    	if(currency == "CHF") {
    		_currency = Currency.CHF;
    	}
    	if(currency == "MYR") {
    		_currency = Currency.MYR;
    	}
    	if(currency == "JPY") {
    		_currency = Currency.JPY;
    	}
    	
    	boolean bReturn =  this.api.sale(new BigInteger(price), _currency);
    	if(bReturn == true ){
    		callbackContext.success("success");
    	}else{
    		callbackContext.error("fail");
    	}
    	
        return this.api.sale(new BigInteger(price), _currency);
    }
    
    
    public void SetMerchantKey(JSONArray args, CallbackContext callbackContext) throws JSONException {
    	String key;
    	JSONObject obj = args.optJSONObject(0);
    	
    	key = obj.optString("key");
    	sharedSecret_key = key;
    }
    
    public void SetDeviceName(JSONArray args, CallbackContext callbackContext) throws JSONException {
    	String name;
    	JSONObject obj = args.optJSONObject(0);
    	
    	name = obj.optString("name");
    	deviceName = name;
    }

   
	public void ListDevices(CallbackContext callbackContext) {

    	List_callbackContext = callbackContext; 
    	
    	//callbackContext.success(message);
    }
    
    @Override
    public void signatureRequired(SignatureRequest signatureRequest, Device device){
        //You'll be notified here if a sale process needs signature verification
        //See documentation
    }

    @Override
    public void endOfTransaction(TransactionResult transactionResult, Device device){
	        if(transactionResult.getFinStatus() == FinancialStatus.AUTHORISED){
	            //...
	        }
    }
    
}
