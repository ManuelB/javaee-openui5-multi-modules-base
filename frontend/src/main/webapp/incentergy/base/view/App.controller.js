sap.ui.define(["sap/ui/core/mvc/Controller", "sap/ui/core/ComponentContainer", "sap/ui/core/Component", "sap/m/StandardListItem"], function (Controller, ComponentContainer, Component, StandardListItem) {
	"use strict";

	return Controller.extend("incentergy.base.view.App", {
		onInit : function() {
			this.getOwnerComponent().getRouter().getRoute("module").attachMatched(function (oEvent) {
				var mParameters = oEvent.getParameters();
				var sModuleParameter = mParameters.arguments["module*"];
				this.processModuleParameter(sModuleParameter);
			}.bind(this));
			
			sap.ui.getCore().getEventBus().subscribe("server-event", "menu-update", function (oEvent) {
				this.byId("menu").getBinding("items").refresh();
			}.bind(this));
			
		},
		processModuleParameter: function(sModuleParameter) {
			// if this is loaded in response for an OAuth grand
			if(window.opener != null && sModuleParameter && sModuleParameter.match(/^id_token/)) {
				window.opener.sap.ui.getCore().getEventBus().publish("incentergy.base.oauth", "token", {
					"popup": window,
					"hash": sModuleParameter,
					"type": "id_token"
				});
			} else if (window.parent != null && sModuleParameter && sModuleParameter.match(/^access_token/)) {
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
			
			var sRoute = oItem.getBindingContext("Menu").getProperty("Route");
			
			this.getOwnerComponent().getRouter().navTo("module", {"module*": sRoute});
			
		},
		onMenuButtonPress : function() {
			var toolPage = this.byId("toolPage");
			toolPage.setSideExpanded(!toolPage.getSideExpanded());
		},
		onSearch: function(oEvent) {
			this.byId("searchResult").openBy(oEvent.getSource()._oSearch._getSearchField());
			var oList = this.byId("searchResultList");
			oList.removeAllItems();
			var q = oEvent.getParameter("query");
			var oRouter = this.getOwnerComponent().getRouter();
			Promise.all(this.getOwnerComponent().getOpenSearchUrls().map(function (sUrl) {
				return fetch(sUrl+q, {"headers": {"Accept": "application/xml", "Authorization": "Bearer "+this.getOwnerComponent().getJwtToken()}})
				    .then(response => response.text())
		        	.then(str => (new window.DOMParser()).parseFromString(str, "text/xml")); }.bind(this))
		    ).then(function (aDocuments) {
		        		aDocuments.forEach(function (oDocument) {
		        			Array.from(oDocument.getElementsByTagName('entry')).forEach(function (oEntry) {
		        				var sUrl = oEntry.getElementsByTagName('id')[0].textContent;
		        				var aSummaries = oEntry.getElementsByTagName('summary');
		        				var aTitles =  oEntry.getElementsByTagName('title');
		        				oList.addItem(new StandardListItem({
		        					"title": aTitles.length > 0 ? aTitles[0].textContent : "No Title",
		        					"description": aSummaries.lenght > 0 ? aSummaries[0].textContent : "",
		        					"type": "Active", 
		        					"press": function () {
		        						oRouter.navTo("module", {"module*": sUrl});
		        					}
			        			}));
			        		})
		        	});
		    });
		}
	});

});
