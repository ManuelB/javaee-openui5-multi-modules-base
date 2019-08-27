sap.ui.define([ "sap/ui/core/Control" ], function(Control) {
	"use strict";
	return Control.extend("incentergy.base.UIExtension", {
		metadata : {
			properties : {
				name: 	{type : "string"}
			},
			aggregations : {
				extensions : {type : "sap.ui.core.Control", singular: "extension", multiple: true},
			},
			defaultAggregation: "extensions"
		},
		constructor: function(oParameters) {
			Control.apply(this, arguments);
			var oEventBus = sap.ui.getCore().getEventBus();
			var sReplyTo = this.getName()+"_"+this.getId();
			oEventBus.subscribe("incentergy.base.uiExtensions", sReplyTo, function (sChannelId, sEventId, aViews) {
				if(aViews.length > 0) {
					aViews.forEach(function (oView) {
						this.addExtension(oView);
					}.bind(this));
				}
			}.bind(this));
			
			oEventBus.publish("incentergy.base.uiExtensions", this.getName(), sReplyTo);
			
		},
		renderer : function(oRM, oControl) {
			oRM.write("<div");
			oRM.writeControlData(oControl);
			oRM.writeClasses();
			oRM.write(">");
			oControl.getExtensions().forEach(function (oView) {				
				oRM.renderControl(oView);
			});
			oRM.write("</div>");
		}
	});
});