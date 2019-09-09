sap.ui.define([ "sap/ui/core/UIComponent", "sap/ui/core/mvc/XMLView", "./login/XMLHttpRequestModifier", "sap/base/Log"],
function(UIComponent, XMLView, XMLHttpRequestModifier, Log) {
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
			
			this._initModules();
			this._initLogin();
			

			// create the views based on the url/hash
			this.getRouter().initialize();
			

		},
		_initModules: function() {
			var me = this;
			this.pModulesLoaded = new Promise(function (fnResolve) {				
				this.getModel().read("/Modules", {
					"success": function(oData) {
						Promise.all(oData.results.map(function (oModule) {
							var sModuleName = oModule.Name;
							
							if(oModule.Type === "Component") {								
								var sPackageName = sModuleName.replace(/-/g, '');
								// Register component path
								jQuery.sap.registerModulePath("incentergy."+sPackageName, "./"+sModuleName+"-frontend/incentergy/"+sPackageName);
								
								var sManifestUrl = "./"+sModuleName+"-frontend/incentergy/"+sPackageName+"/manifest.json";
								return fetch(sManifestUrl, {
									credentials: 'include'
								}).then(function(oResponse) {
									if(!oResponse.ok) {
										Log.error(oResponse.statusCode+" "+oResponse.statusText);
										return Promise.resolve({});
									} else {									
										return oResponse.json();
									}
								}).catch(function(oError) {
									Log.error(oError);
									return Promise.resolve({});
								});
							} else if(oModule.Type === "Library") {
								var sPackageName = sModuleName.replace(/-/g, '.');
								// Register library path
								jQuery.sap.registerModulePath("incentergy."+sPackageName, "./"+sModuleName+"-lib/incentergy/"+sPackageName.replace(".", "/"));
								return Promise.resolve({});
							} else {
								Log.error("Found unsupported Module Type: "+oModule.Type);
								return Promise.resolve({});
							}
							
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
								fnResolve();
							}.bind(this));
						}.bind(this));
					}.bind(this),
					"error": function() {
						
					}
				});
			}.bind(this));
		},
		_initLogin: function() {
			new XMLHttpRequestModifier(XMLHttpRequest, this);
		},
		_subscribeToExtensionMessages : function (sUiExtension) {
			var oEventBus = sap.ui.getCore().getEventBus();
			oEventBus.subscribe("incentergy.base.uiExtensions", sUiExtension, function (sChannel, sEventId, oReplyTo) {
				if(!(sUiExtension in this._aUiExtensions)) {
					fnResolve([]);
				}
				Promise.all(this._aUiExtensions[sEventId].map(function (sView) {
					return XMLView.create({"viewName": sView});
				})).then(function (aViews) {
					oEventBus.publish("incentergy.base.uiExtensions", oReplyTo.replyTo, aViews);
				})
			}.bind(this));
		},
		modulesLoaded : function () {
			return this.pModulesLoaded;
		}
	});
});