sap.ui.define([ "sap/ui/core/Component", "sap/ui/core/UIComponent", "sap/ui/core/mvc/XMLView", "./login/XMLHttpRequestModifier", "sap/base/Log",
	"sap/ui/model/json/JSONModel", 
	"sap/ui/base/ManagedObject"],
function(Component, UIComponent, XMLView, XMLHttpRequestModifier, Log, JSONModel, ManagedObject) {
	"use strict";
	return UIComponent.extend("incentergy.base.Component", {

		metadata : {
			manifest: "json"
		},
		init : function() {
			this.pModulesLoaded = new Promise(function (fnResolve) {				
				this._fnModulesLoadedResolved = fnResolve;
			}.bind(this));
			
			// call the init function of the parent
			UIComponent.prototype.init.apply(this, arguments);
			
			// This map contains ui extension names
			// to a list of views that should be loaded
			// as part of the view
			// {
			// "incentergy.employee.detail": [ "incentergy.performance.employee.goals" ]
			// }
			this._aUiExtensions = {};
			
			this._aOpenSearchUrls = [];
			
			var oCardsModel = new JSONModel();
			this.setModel(oCardsModel, "Cards");
			
			var oJwtModel = new JSONModel();
			this.setModel(oJwtModel, "JWT");
			
			sap.ui.getCore().setModel(oJwtModel,"JWT");
			
			this._initModules();
			this._initLogin();
			
			this.modulesLoaded().then(function () {				
				// create the views based on the url/hash
				this.getRouter().initialize();
			}.bind(this));

		},
		_initModules: function() {
			var me = this;
			this.getModel().read("/Modules", {
				"success": function(oData) {
					Promise.all(oData.results.map(function (oModule) {
						var sModuleName = oModule.Name;
						
						if(oModule.Type === "Component") {								
							var sPackageName = sModuleName.replace(/-/g, '');
							// Register component path
							// TODO: replace with https://sapui5.hana.ondemand.com/#/api/sap.ui.loader/methods/sap.ui.loader.config
							jQuery.sap.registerModulePath("incentergy."+sPackageName, "./"+sModuleName+"-frontend/incentergy/"+sPackageName);
							
							var sManifestUrl = "./"+sModuleName+"-frontend/incentergy/"+sPackageName+"/manifest.json";
							return fetch(sManifestUrl, {
								credentials: 'include'
							}).then(function(oResponse) {
								
								if(!oResponse.ok) {
									Log.error(sManifestUrl+" "+oResponse.statusCode+" "+oResponse.statusText);
									return Promise.resolve({});
								} else {									
									return oResponse.json();
								}
							}).catch(function(oError) {
								Log.error("Url: "+sManifestUrl+" Error: "+oError);
								return Promise.resolve({});
							});
						} else if(oModule.Type === "Library") {
							var sPackageName = sModuleName.replace(/-/g, '.');
							if(sPackageName == "base.openui5ol") {
								sPackageName = "ol";
								// TODO: replace with https://sapui5.hana.ondemand.com/#/api/sap.ui.loader/methods/sap.ui.loader.config
								jQuery.sap.registerModulePath("ol", "https://cdnjs.cloudflare.com/ajax/libs/ol3/4.6.5/ol");
								
							}
							// Register library path
							jQuery.sap.registerModulePath("incentergy."+sPackageName, "./"+sModuleName+"-lib/incentergy/"+sPackageName.replace(".", "/"));
							if(sPackageName == "base.websocket") {
								sap.ui.require(["incentergy/base/websocket/Connection"], function (Connection) {
									new Connection();
								});
							}
							return Promise.resolve({});
						} else {
							Log.error("Found unsupported Module Type: "+oModule.Type);
							return Promise.resolve({});
						}
						
					})).then(function (aManifests) {
						var aVegaCards = [];
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
							if("incentergy.base.opensearch" in oManifest && "url" in oManifest["incentergy.base.opensearch"]) {
								this._aOpenSearchUrls.push(oManifest["incentergy.base.opensearch"]["url"]);
							}
							if("incentergy.base.vega" in oManifest) {
								aVegaCards = aVegaCards.concat(oManifest["incentergy.base.vega"].map(function (oJson) {
									oJson.content = JSON.stringify(oJson.content); 
									return oJson;
								}));
							}
							if("incentergy.base.event" in oManifest) {
								for(let oEvent of oManifest["incentergy.base.event"]) {
									sap.ui.getCore().getEventBus().subscribe(oEvent.channel, oEvent.eventId, function (sChannelId, sEventId, oEventData) {
										Component.load({"name": oManifest["sap.app"]["id"]}).then((oComponentClass) => {
											new oComponentClass()[oEvent.listenerFunctionOnComponent](sChannelId, sEventId, oEventData);
										});
									});
								}
							}
						}.bind(this));
						this.getModel("Cards").setData(aVegaCards);
						this._fnModulesLoadedResolved();
					}.bind(this));
				}.bind(this),
				"error": function() {
					
				}
			});
			
		},
		createContent: function() {
			var aComponentArguments = arguments;
			// wait until the modules are loaded, then init the content of
			// this component
			this.modulesLoaded().then(function () {
				this.runAsOwner(function() {
					var oPreprocessors = {};

					// when auto prefixing is enabled we add the prefix
					if (this.getAutoPrefixId()) {
						oPreprocessors.id = function(sId) {
							return this.createId(sId);
						}.bind(this);
					}
					
					ManagedObject.runWithPreprocessors(function() {
						var oRootControl = UIComponent.prototype.createContent.apply(this, aComponentArguments);
						this.setAggregation("rootControl", oRootControl);
						
						var oRoutingManifestEntry = this._getManifestEntry("/sap.ui5/routing", true) || {},
							oRoutingConfig = oRoutingManifestEntry.config || {};

						
						if (oRoutingConfig.targetParent === undefined) {
							oRoutingConfig.targetParent = oRootControl.getId();
						}
						if (this._oTargets) {
							this._oTargets._setRootViewId(oRootControl.getId());
						}
						this.getUIArea().invalidate();
					}.bind(this), oPreprocessors);
				}.bind(this));
			}.bind(this));
			return null;
		},
		_initLogin: function() {
			this._xMLHttpRequestModifier = new XMLHttpRequestModifier(XMLHttpRequest, this);
		},
		getJwtToken: function() {
			return this._xMLHttpRequestModifier.getJwtToken();
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
		getOpenSearchUrls: function() {
			return this._aOpenSearchUrls;
		},
		modulesLoaded : function () {
			return this.pModulesLoaded;
		}
	});
});