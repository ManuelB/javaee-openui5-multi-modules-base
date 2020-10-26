sap.ui.define(["sap/ui/core/Control", "sap/ui/dom/includeScript", "sap/base/Log"],
	function (Control, includeScript, Log) {
		"use strict";

		
		var pVegaLibsLoaded = includeScript({"url": "https://cdn.jsdelivr.net/npm/vega@5.17.0"}).then(function () {		
			return includeScript({"url": "https://cdn.jsdelivr.net/npm/vega-lite@4.17.0"}).then(function () {				
				return includeScript({"url": "https://cdn.jsdelivr.net/npm/vega-embed@6.12.2"});
			});
		});

		var Vega = Control.extend("incentergy.base.vega.Vega", {
			metadata: {
				library: "incentergy.base.vega",
				properties: {
					content: {
						type: "string",
						defaulValue: ""
					}
				}
			},
		    renderer: function(oRm,oControl){
		    	//first up, render a div for the ShadowBox
                oRm.write("<div");
     
                //next, render the control information, this handles your sId (you must do this for your control to be properly tracked by ui5).
                oRm.writeControlData(oControl);
                oRm.writeClasses();
                oRm.write(">");
                
                // and obviously, close off our div
                oRm.write("</div>")
		    },
			onAfterRendering: function() {
				pVegaLibsLoaded.then(function () {
					if(!this.getContent()) {
						return;
					}
					try {
						var oContent = JSON.parse(this.getContent());
						if(document.getElementById(this.getId())) {
							let incentergyLoader = vega.loader({
								"http": {
									"headers": {
								      "Authorization": "Bearer "+sap.ui.getCore().getComponent(this._sOwnerId).getJwtToken(),
								      "Accept": "application/json"
								    }
								}
							});
							let oldLoad = incentergyLoader.load;
							incentergyLoader.load = function(uri) {
								if(uri.startsWith("indexeddb://")) {
									let matches = uri.match(/indexeddb:\/\/([^\/]+)\/([^\/]+)/);
									if(matches) {
										let sDb = matches[1];
										let sObjectStore = matches[2];
										return new Promise(function (fnResolve) {
											let request = indexedDB.open(sDb);
											request.onsuccess = function (event) {
												let db = event.target.result;
												if(db.objectStoreNames.contains(sObjectStore)) {
													db.transaction(sObjectStore, "readonly").objectStore(sObjectStore).getAll().onsuccess = function(event) {											
														fnResolve(event.target.result);
													};
												} else {
													Log.warning(uri+" does not exists.");
													fnResolve([]);
												}
											}
										});
									} else {
										alert(" should be e.g. indexeddb://database/store but was: "+uri);
									}
								} else {
									return oldLoad.apply(this, arguments);
								}
							};
							
							vegaEmbed('#'+this.getId(), oContent, {
								loader: incentergyLoader
						    }).then(function(result) {
						      // Access the Vega view instance (https://vega.github.io/vega/docs/api/view/) as result.view
							
						    }).catch(console.error);
					    }
					} catch(e) {
						Log.error("Could not render vega card: "+e);
					}
				}.bind(this));
			}
		});
		return Vega;
});