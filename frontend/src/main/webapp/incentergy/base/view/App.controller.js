sap.ui.define(["sap/ui/core/mvc/Controller", "sap/ui/core/ComponentContainer", "sap/ui/core/Component"], function (Controller, ComponentContainer, Component) {
	"use strict";

	return Controller.extend("incentergy.base.view.App", {
		onInit : function() {
			this.getOwnerComponent().getRouter().getRoute("module").attachMatched(function (oEvent) {
				var mParameters = oEvent.getParameters();
				var sModuleParameter = mParameters.arguments["module*"];
				this.processModuleParameter(sModuleParameter);
				
			}.bind(this));
		},
		processModuleParameter: function(sModuleParameter) {
			// if this is loaded in response for an OAuth grand
			if(window.opener != null && sModuleParameter.match(/^id_token/)) {
				window.opener.sap.ui.getCore().getEventBus().publish("incentergy.base.oauth", "token", {
					"popup": window,
					"hash": sModuleParameter,
					"type": "id_token"
				});
			} else if (window.parent != null && sModuleParameter.match(/^access_token/)) {
				window.parent.sap.ui.getCore().getEventBus().publish("incentergy.base.oauth", "token", {
					"popup": window,
					"hash": sModuleParameter,
					"type": "access_token"
				});
			} else {
				this.getOwnerComponent().modulesLoaded().then(function () {					
					this.loadComponent(sModuleParameter);
				}.bind(this));
			}
		},
		loadComponent: function(sModuleParameter) {
			if(sModuleParameter) {
				var sPackageName = sModuleParameter.split(/\//)[0];
				if(sPackageName !== this._sCurrentModule) {
					this._sCurrentModule = sPackageName;
					var oNavContainer = this.byId("navContainer");
					oNavContainer.removeAllPages();
					
					var sComponentName = "incentergy."+sPackageName;
					
					if(this._oldComponentContainer) {
						this._oldComponentContainer.destroy();
					}
					
					// var oComponent = Component.get(sComponentName);
					// if(oComponent) {
					//	oComponent.destroy();
					// }
					
					var oContainer = new ComponentContainer({
						name: sComponentName,
						manifest: true,
						async: true,
						height: "100%",
						width: "100%",
						settings: {
							id: "incentergy."+sPackageName
						}
					});
					this._oldComponentContainer = oContainer;
					oNavContainer.addPage(oContainer);
					oNavContainer.to(oContainer);
				}
			}
		},
		onItemSelect : function(oEvent) {
			var oItem = oEvent.getParameter('item');
			
			var sModuleName = oItem.getBindingContext().getProperty("Name");
			var sPackageName = sModuleName.replace(/-/g, '');
			
			this.getOwnerComponent().getRouter().navTo("module", {"module*": sPackageName});
			
		},

		onMenuButtonPress : function() {
			var toolPage = this.byId("toolPage");
			toolPage.setSideExpanded(!toolPage.getSideExpanded());
		}
	});

});
