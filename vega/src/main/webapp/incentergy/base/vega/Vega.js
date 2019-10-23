sap.ui.define(["sap/ui/core/Control", "sap/ui/dom/includeScript", "sap/base/Log"],
	function (Control, includeScript, Log) {
		"use strict";

		
		var pVegaLibsLoaded = includeScript({"url": "https://cdn.jsdelivr.net/npm/vega@5.6.0"}).then(function () {		
			return includeScript({"url": "https://cdn.jsdelivr.net/npm/vega-lite@4.0.0-beta.8"}).then(function () {				
				return includeScript({"url": "https://cdn.jsdelivr.net/npm/vega-embed@5.1.3"});
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
						vegaEmbed('#'+this.getId(), oContent, {
							loader: vega.loader({
								"http": {
									"headers": {
								      "Authorization": "Bearer "+sap.ui.getCore().getComponent(this._sOwnerId).getJwtToken(),
								      "Accept": "application/json"
								    }
								}
							})
						}).then(function(result) {
						    // Access the Vega view instance (https://vega.github.io/vega/docs/api/view/) as result.view
							
						  }).catch(console.error);
					} catch(e) {
						Log.error("Could not render vega card: "+e);
					}
				}.bind(this));
			}
		});
		return Vega;
});