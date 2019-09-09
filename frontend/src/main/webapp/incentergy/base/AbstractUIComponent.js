sap.ui.define(["jquery.sap.global",
	"sap/ui/core/UIComponent",
	"sap/ui/model/json/JSONModel",
	"sap/f/FlexibleColumnLayoutSemanticHelper",
	"sap/ui/model/odata/v2/ODataModel"
	],
function(jQuery, UIComponent, JSONModel, FlexibleColumnLayoutSemanticHelper, ODataModel) {
	"use strict";
	return UIComponent.extend("incentergy.base.AbstractComponent", {

		metadata : {
			manifest: "json"
		},

		init : function() {
			// call the init function of the parent
			UIComponent.prototype.init.apply(this, arguments);
			
			var oModel = new JSONModel();
			this.setModel(oModel, "Layout");

			this._patchProcessChangesOfModelToNotDeleteNavigationProperties(this.getModel());
			
			// create the views based on the url/hash
			this.getRouter().initialize();

		},
		
		_patchProcessChangesOfModelToNotDeleteNavigationProperties: function (oModel) {
            // Do not delete navigation properties
            oModel._processChange = function (sKey, oData, sUpdateMethod) {
                // recover navigation properties
                const oEntityType = this.oMetadata._getEntityTypeByPath(sKey);
                const aNavProps = this.oMetadata._getNavigationPropertyNames(oEntityType);
                const oNavigationProperties = {};

                // do a copy of the payload or the changes will be deleted in the model as well (reference)
                const oPayload = jQuery.sap.extend(true, {}, this._getObject('/' + sKey, true), oData);
                aNavProps.forEach(function (sNavPropName) {
                    if (oPayload[sNavPropName]) {
                        oNavigationProperties[sNavPropName] = oPayload[sNavPropName];
                    }
                });

                const oRequest = ODataModel.prototype._processChange.apply(this, arguments);

                for (const sNavigationProperty in oNavigationProperties) {
                    if ("__deferred" in oNavigationProperties[sNavigationProperty] &&
                        "uri" in oNavigationProperties[sNavigationProperty].__deferred) {
                        const sUri = oNavigationProperties[sNavigationProperty].__deferred.uri;
                        if (!sUri.match(/^https?:\/\//)) {
                            oRequest.data[sNavigationProperty] = oNavigationProperties[sNavigationProperty];
                            delete oRequest.data[sNavigationProperty].__ref;
                        }
                    }
                }

                return oRequest;
            };
        },

		/**
		 * Returns an instance of the semantic helper
		 * @returns {sap.f.FlexibleColumnLayoutSemanticHelper} An instance of the semantic helper
		 */
		getHelper: function () {
			var oFCL = this.getRootControl().byId("fcl"),
				oParams = jQuery.sap.getUriParameters(),
				oSettings = {
					defaultTwoColumnLayoutType: sap.f.LayoutType.TwoColumnsMidExpanded,
					defaultThreeColumnLayoutType: sap.f.LayoutType.ThreeColumnsMidExpanded,
					mode: oParams.get("mode"),
					initialColumnsCount: oParams.get("initial"),
					maxColumnsCount: oParams.get("max")
				};

			return FlexibleColumnLayoutSemanticHelper.getInstanceFor(oFCL, oSettings);
		}
	});
});