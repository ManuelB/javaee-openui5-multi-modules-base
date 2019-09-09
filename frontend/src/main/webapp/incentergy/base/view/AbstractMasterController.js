sap.ui.define([
	"sap/ui/model/json/JSONModel",
	"./AbstractController",
	"sap/ui/model/Filter",
	"sap/ui/model/FilterOperator",
	'sap/ui/model/Sorter',
	'sap/m/MessageBox',
	'sap/m/MessageToast',
	"sap/ui/core/Fragment"
], function (JSONModel, AbstractController, Filter, FilterOperator, Sorter, MessageBox, MessageToast, Fragment) {
	"use strict";

	return AbstractController.extend("incentergy.base.view.AbstractMasterController", {
		onInit: function () {
			this.oRouter = this.getOwnerComponent().getRouter();
			this._bDescendingSort = false;
		},
		onListItemPress: function (oEvent) {
			var oNextUIState = this.getOwnerComponent().getHelper().getNextUIState(1),
				entityPath = oEvent.getSource().getBindingContext().getPath(),
				entity = entityPath.split("/").slice(-1).pop();

			var oParams = {layout: oNextUIState.layout};
			oParams[this.getEntityName()] = entity;
			this.oRouter.navTo("detail", oParams);
		},
		onSearch: function (oEvent) {
			var oTableSearchState = [],
				sQuery = oEvent.getParameter("query");

			if (sQuery && sQuery.length > 0) {
				oTableSearchState = [new Filter(this.getSortField(), FilterOperator.Contains, sQuery)];
			}

			this.getView().byId(this.getEntityName()+"Table").getBinding("items").filter(oTableSearchState, "Application");
		},

		onAdd: function (oEvent) {
			var oView = this.getView();

			// create dialog lazily
			if (!this.byId("createAndEditDialog")) {
				// load asynchronous XML fragment
				Fragment.load({
					id: oView.getId(),
					name: "incentergy."+this.getPackageName()+".view.master.CreateAndEditDialog",
					controller: this
				}).then(function (oDialog) {
					// connect dialog to the root view of this component (models, lifecycle)
					oView.addDependent(oDialog);
					this._openCreateDialog(oDialog);
				}.bind(this));
			} else {
				this._openCreateDialog(this.byId("createAndEditDialog"));
			}
		},
		getPackageName: function() {
			return this.getEntityName();
		},
		getEntityName : function () {
			throw new Error("getEntityName must be implemented by derived class");
		},
		_openCreateDialog: function (oDialog) {
			oDialog.open();
			
			var sEntityName = this.getEntityName();
			sEntityName = sEntityName[0].toUpperCase() + sEntityName.slice(1);
			
			var oContext = this._createContextFromModel();
			oDialog.setBindingContext(oContext);
		},
		_createContextFromModel: function (sEntityName) {
			return this.getOwnerComponent().getModel().createEntry("/"+sEntityName+"s")
		},
		onSave: function (oEvent) {
			this.getOwnerComponent().getModel().submitChanges({
				"success": function (oData) {
					if("__batchResponses" in oData) {
						var aErrors = oData.__batchResponses.filter(function (oResponse) {
							return "message" in oResponse;
						})
						if(aErrors.length > 0) {
							MessageBox.error(this.translate("ErrorDuringCreating_"+this.getEntityName(), [aErrors[0].response.statusText, aErrors[0].response.body]));
						} else {
							MessageToast.show(this.translate("SuccessfullyCreated_"+this.getEntityName()));
							this.byId("createAndEditDialog").close();
						}
					} else {						
						MessageToast.show(this.translate("SuccessfullyCreated_"+this.getEntityName()));
						this.byId("createAndEditDialog").close();
					}
				}.bind(this),
				"error": function (oError) {
					MessageBox.error(this.translate("ErrorDuringCreating_"+this.getEntityName(), [oError]));
				}.bind(this)
			});
		},
		onCancel: function(oEvent) {
			this.getOwnerComponent().getModel().resetChanges();
			this.byId("createAndEditDialog").close();
		},
		onSort: function (oEvent) {
			this._bDescendingSort = !this._bDescendingSort;
			var oView = this.getView(),
				oTable = oView.byId(this.getEntityName()+"Table"),
				oBinding = oTable.getBinding("items"),
				oSorter = new Sorter(this.getSortField(), this._bDescendingSort);

			oBinding.sort(oSorter);
		},
		getSortField: function () {
			return "Name";
		}
	});
}, true);
