sap.ui.define(["sap/base/Log"], function(Log) {
	"use strict";
	return function () {
		var sWebSocketUrl = "ws"+(window.location.protocol == 'https' ? 's' : '')+"://"+window.location.host+"/base-websocket-backend/client";
		Log.info("[constructor] WebSocket to: "+sWebSocketUrl);
	    var oWebsocket = new WebSocket(sWebSocketUrl);

		oWebsocket.onopen = function(e) {
		  Log.info("[open] Connection established");
		};

		oWebsocket.onmessage = function(oEvent) {
		  try {
			var oPayload = JSON.parse(oEvent.data);
		    sap.ui.getCore().getEventBus().publish(oPayload.channelId, oPayload.eventId, oPayload.data);
		  } catch(e) {
			  Log.warning(e); 
		  }
		};

		oWebsocket.onclose = function(oEvent) {
		  if (event.wasClean) {
			  Log.info("[close] Connection closed cleanly, code="+oEvent.code+" reason="+oEvent.reason);
		  } else {
		    // e.g. server process killed or network down
		    // event.code is usually 1006 in this case
			Log.warning('[close] Connection died');
		  }
		};

		oWebsocket.onerror = function(oError) {
			Log.warning(oError.message);
		};
	};
}, /* bExport= */false);