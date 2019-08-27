sap.ui.define([ "sap/ui/core/UIComponent", "sap/ui/core/mvc/XMLView"],
function(UIComponent, XMLView) {
	"use strict";
	return UIComponent.extend("incentergy.base.Component", {

		metadata : {
			manifest: "json"
		},

		init : function() {
			// call the init function of the parent
			UIComponent.prototype.init.apply(this, arguments);
			
			// This map contains ui extension names
			// to a list of views that should be loaded
			// as part of the view
			// {
			// "incentergy.employee.detail": [ "incentergy.performance.employee.goals" ]
			// }
			this._aUiExtensions = {};
			
			// create the views based on the url/hash
			this.getRouter().initialize();
			
			this._initModules();

		},
		_initModules: function() {
			var me = this;
			this.getModel().read("/Modules", {
				"success": function(oData) {
					Promise.all(oData.results.map(function (oModule) {
						var sModuleName = oModule.Name;
						var sPackageName = sModuleName.replace(/-/g, '');
						// Register module path
						jQuery.sap.registerModulePath("incentergy."+sPackageName, "./"+sModuleName+"-frontend/incentergy/"+sPackageName);
						
						var sManifestUrl = "./"+sModuleName+"-frontend/incentergy/"+sPackageName+"/manifest.json";
						return fetch(sManifestUrl, {
							credentials: 'include'
						}).then(function(response) {
						    return response.json();
						});
						
					})).then(function (aManifests) {
						aManifests.forEach(function (oManifest) {
							if("incentergy.base" in oManifest && "uiExtensions" in oManifest["incentergy.base"]) {
								for(var sUiExtension in oManifest["incentergy.base"]["uiExtensions"]) {
									if(!(sUiExtension in this._aUiExtensions)) {
										this._aUiExtensions[sUiExtension] = [];
										this._subscribeToExtensionMessages(sUiExtension);
									}
									this._aUiExtensions[sUiExtension] = this._aUiExtensions[sUiExtension].concat(oManifest["incentergy.base"]["uiExtensions"][sUiExtension]);
								}
							}
						}.bind(this));
					}.bind(this));
				}.bind(this),
				"error": function() {
					
				}
			});
		},
		_subscribeToExtensionMessages : function (sUiExtension) {
			var oEventBus = sap.ui.getCore().getEventBus();
			oEventBus.subscribe("incentergy.base.uiExtensions", sUiExtension, function (sChannel, sEventId, sReplyTo) {
				if(!(sUiExtension in this._aUiExtensions)) {
					fnResolve([]);
				}
				Promise.all(this._aUiExtensions[sEventId].map(function (sView) {
					return XMLView.create({"viewName": sView});
				})).then(function (aViews) {
					oEventBus.publish("incentergy.base.uiExtensions", sReplyTo, aViews);
				})
			}.bind(this));
		}
	});
});