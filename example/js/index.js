/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
    	alert("device Ready");
    	
    	HandPointPlugin.SetMerchantKey("0102030405060708091011121314151617181920212223242526272829303132");
    	HandPointPlugin.init();
    	HandPointPlugin.ListDevices(app.getDeviceList);
    	
        app.receivedEvent('deviceready');
    },
    getDeviceList : function(deviceList) {
    	alert("founded device count is " + deviceList.length);
    	for(i=0;i<deviceList.length;i++) {
    		alert("name is " + deviceList[i].name);
    		alert("address is " + deviceList[i].address);
    		alert("port is " + deviceList[i].port);
    	}
    	
    	HandPointPlugin.SetDeviceName("Windows Phone");
    	HandPointPlugin.connectWithCurrentDevice(app.connectSuccess, app.connectFail);
    	//HandPointPlugin.connect("CardReader7", "08:00:69:02:01:FC", "1", "BLUETOOTH", app.connectSuccess, app.connectFail);
    },
    connectSuccess : function() {
    	alert("connectSuccess");
    	
    	HandPointPlugin.pay("1000","GBP",app.paySuccess, app.payFail);
    },
    connectFail : function() {
    	alert("connectFail");
    },
    paySuccess : function() {
    	alert("PaySuccess");
    },
    payFail : function() {
    	alert("PayFail");
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    }
};

app.initialize();