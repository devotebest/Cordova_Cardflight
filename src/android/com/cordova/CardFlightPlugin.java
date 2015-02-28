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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;


public class CardFlightPlugin extends CordovaPlugin implements Events.Required {
    
	Hapi api;
    Device device;
    @SuppressWarnings("unused")
	private CallbackContext callbackContext;
    private CallbackContext List_callbackContext;

    public static final String API_TOKEN = "YOUR_API_TOKEN";
    public static final String ACCOUNT_TOKEN = "YOUR_ACCOUNT_TOKEN";

    public string api_token;
    public string account_token;
    

    private Context mContext;

    private boolean readerIsConnected;
    private boolean readerFailed;
    private boolean swipedCard;


    private Reader reader = null;
    private Card mCard = null;
    private Charge mCharge = null;
    private OnCardKeyedListener onCardKeyedListener;
    private OnFieldResetListener onFieldResetListener;
    private PaymentView mFieldHolder;


    /**
    * Gets the application context from cordova's main activity.
    * @return the application context
    */
    private Context getApplicationContext() {
        return this.cordova.getActivity().getApplicationContext();
    }

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
             
         }else  if (action.equals("setApiTokens")) {
             //Set API Tokens
        	 setApiTokens(args, callbackContext); 
        	 retValue = true;
        	 
         } else if (action.equals("swipecard")) {
        	 //Pay
        	 swipe(args, callbackContext);
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

    //Set API Tokens;
    public void setApiTokens(JSONArray args, CallbackContext callbackContext) throws JSONException {      
        JSONObject obj = args.optJSONObject(0);
        String api_token = obj.optString("API_TOKEN");
        String account_token = obj.optString("ACCOUNT_TOKEN");

        this.api_token = api_token;
        this.account_token = account_token;
        //This triggers the search, you should expect the results in the listener defined above
        callbackContext.success();
    }

    //swipe card
    public void swipecard(JSONArray args, CallbackContext callbackContext) throws JSONException {      
        JSONObject obj = args.optJSONObject(0);
        String api_token = obj.optString("API_TOKEN");
        String account_token = obj.optString("ACCOUNT_TOKEN");

        this.api_token = api_token;
        this.account_token = account_token;
        //This triggers the search, you should expect the results in the listener defined above
        callbackContext.success();
    }


    public void initApi(Context context, CallbackContext callbackContext){
      
        // Instantiate CardFlight Instance
        CardFlight.getInstance().setApiTokenAndAccountToken(this.api_token, this.account_token);
        CardFlight.getInstance().setLogging(true);
        

        // Create a new Reader object with AutoConfig handler
        reader = new Reader(getApplicationContext(), new CardFlightDeviceHandler() {

            @Override
            public void readerIsConnecting() {
                Toast.makeText(getApplicationContext(),
                        "Device connecting", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void readerIsAttached() {
                readerFailed = false;
                Toast.makeText(getApplicationContext(),
                        "Device connected", Toast.LENGTH_SHORT).show();

                readerConnected();
            }

            @Override
            public void readerIsDisconnected() {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(),
                        "Device disconnected", Toast.LENGTH_SHORT)
                        .show();

                readerDisconnected();
            }

            @Override
            public void deviceBeginSwipe() {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(),
                        "Device begin swipe", Toast.LENGTH_SHORT)
                        .show();

            }

            @Override
            public void readerCardResponse(Card card) {
                // TODO Auto-generated method stub

                Toast.makeText(getApplicationContext(),
                        "Device swipe completed", Toast.LENGTH_SHORT)
                        .show();

                mCard = card;

                fillFieldsWithData(card);
            }

            @Override
            public void deviceSwipeFailed() {
                readerFailed = true;
                Toast.makeText(getApplicationContext(),
                        "Device swipe failed", Toast.LENGTH_SHORT)
                        .show();

            }

            @Override
            public void deviceSwipeTimeout() {
                readerFailed = true;
                Toast.makeText(getApplicationContext(),
                        "Device swipe time out", Toast.LENGTH_SHORT)
                        .show();

            }

            @Override
            public void deviceNotSupported() {
                readerFailed = true;
                Toast.makeText(getApplicationContext(),
                        "Device not supported", Toast.LENGTH_SHORT)
                        .show();

                enableAutoconfigButton();
            }

            @Override
            public void readerTimeout() {
                readerFailed = true;
                Toast.makeText(getApplicationContext(),
                        "Reader has timed out", Toast.LENGTH_SHORT)
                        .show();

                enableAutoconfigButton();
            }

        }, new CardFlightAutoConfigHandler() {
            @Override
            public void autoConfigProgressUpdate(int i) {
                Log.i(TAG, "AutoConfig progress %" + i);
                readerStatus.setText("AutoConfig Progress %" + i);
            }

            @Override
            public void autoConfigFinished() {
                Log.i(TAG, "AutoConfig finished");
            }

            @Override
            public void autoConfigFailed() {
                Log.i(TAG, "AutoConfig failed");
                readerStatus.setText("AutoConfig failed -- device is not supported");
            }
        });

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
