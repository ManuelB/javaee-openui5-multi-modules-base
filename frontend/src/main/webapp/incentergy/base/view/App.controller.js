sap.ui.define(["sap/ui/core/mvc/Controller", "sap/ui/core/ComponentContainer"], function (Controller, ComponentContainer) {
	"use strict";

	return Controller.extend("incentergy.base.view.App", {
		onItemSelect : function(oEvent) {
			var oItem = oEvent.getParameter('item');
			
			var sModuleName = oItem.getBindingContext().getProperty("Name");
			
			var sPackageName = sModuleName.replace(/-/g, '');
			
			jQuery.sap.registerModulePath("incentergy."+sPackageName, "./"+sModuleName+"-frontend/incentergy/"+sPackageName)

			var oNavContainer = this.byId("navContainer");
			oNavContainer.removeAllPages();
			var oContainer = new ComponentContainer({
				name: "incentergy."+sPackageName,
				manifest: true,
				async: true,
				height: "100%",
				width: "100%"
			});
			oNavContainer.addPage(oContainer);
			oNavContainer.to(oContainer);
		},

		onMenuButtonPress : function() {
			var toolPage = this.byId("toolPage");
			toolPage.setSideExpanded(!toolPage.getSideExpanded());
		}
	});

});
