sap.ui.define([
	"sap/ui/base/ManagedObject",
	"sap/ui/core/Fragment"
], function (ManagedObject, Fragment) {
		"use strict";
		var XMLHttpRequestModifier = ManagedObject.extend("incentergy.base.login.XMLHttpRequestModifier", {
			metadata: {
				events: {
					"loginSuccess": {
						parameters: {}
					}
				}
			},
			constructor: function (XMLHttpRequest, oComponent) {
				ManagedObject.apply(this, {});
				var me = this;
				
				this._sJwtToken = window.localStorage._sJwtToken;
				
				if(this._sJwtToken) {
					this.doLogin(this._sJwtToken);
				}
				
				this.oComponent = oComponent;

				var fnOpen = XMLHttpRequest.prototype.open;
				XMLHttpRequest.prototype.open = function (sMethod, sUrl, bAsync) {
					this._httpMethod = sMethod;
					this._httpUrl = sUrl;
					this._httpAsync = bAsync;
					if (typeof fnOpen === "function") {
						return fnOpen.apply(this, arguments);
					}
				};

				var fnSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;
				XMLHttpRequest.prototype.setRequestHeader = function (header, value) {
					if (!this._httpRequestHeader) {
						this._httpRequestHeader = {};
					}
					this._httpRequestHeader[header] = value;
					if (typeof fnSetRequestHeader === "function") {
						return fnSetRequestHeader.apply(this, arguments);
					}
				};

				var fnSend = XMLHttpRequest.prototype.send;
				XMLHttpRequest.prototype.send = function (body) {
					this._httpRequestBody = body;
					
					if(me._sJwtToken && me.isLocalModel(this._httpUrl)) {
						this.setRequestHeader("Authorization", "Bearer "+me._sJwtToken);
					}

					var fnOnreadystatechange = this.onreadystatechange;
					this.onreadystatechange = function () {
						if (this.readyState == 4 && this.status == 401) {
							me._handleUnauthorizedRequest(this);
						} else if (typeof fnOnreadystatechange === "function") {
							return fnOnreadystatechange.apply(this, arguments);
						}

						return this;
					};
					return fnSend.apply(this,
						arguments);
				};
			}

		});
		
		XMLHttpRequestModifier.prototype.getJwtToken = function() {			
			return this._sJwtToken;
		};
		
		XMLHttpRequestModifier.prototype.isLocalModel = function(sUrl) {
			return sUrl.indexOf(".svc") != -1 || sUrl.indexOf("opensearch") != -1 ;
		};

		/**
		 * Handles unauthorized (401) XMLHttpRequest
		 *
		 * @param {XMLHttpRequest}
		 *            xhr the failing XMLHttpRequest
		 */
		XMLHttpRequestModifier.prototype._handleUnauthorizedRequest = function (xhr) {
			this.attachEventOnce("loginSuccess", function () {
				xhr.open(xhr._httpMethod, xhr._httpUrl, xhr._httpAsync);
				for (var header in xhr._httpRequestHeader) {
					xhr.setRequestHeader(header, xhr._httpRequestHeader[header]);
				}
				xhr.send(xhr._httpRequestBody);
			});

			this._showLoginDialogOnce();
		};

		/**
		 * Shows a login dialog once and throws event "loginSuccess" on successful login
		 *
		 */
		XMLHttpRequestModifier.prototype._showLoginDialogOnce = function () {
			var me = this;

			if (!this._oLoginDialog) {
				Fragment.load({
					"name": "incentergy.base.login.LoginDialog",
					"controller": this
				}).then(function (oLoginDialog) {					
					this._oLoginDialog = oLoginDialog;
					this.oComponent.getRootControl().addDependent(this._oLoginDialog);
					this._oLoginDialog.open();
				}.bind(this));
			} else {				
				this._oLoginDialog.getContent()[0].setVisible(false);
				this._oLoginDialog.open();
			}
		};
		
		XMLHttpRequestModifier.prototype.onLoginButtonPressed = function(oEvent) {
			var sUsernameInput = this._oLoginDialog.getContent()[1].getContent()[1].getValue();
			var sPasswordInput = this._oLoginDialog.getContent()[1].getContent()[3].getValue();
			var bSaveLoginData = this._oLoginDialog.getContent()[1].getContent()[5].getSelected();
			
			fetch("/jwt-auth-backend/Services/auth/jwt", {
				headers: {
					"Authorization": "Basic "+btoa(sUsernameInput+":"+sPasswordInput)
				}
			}).then(function (oResponse) {
				if(!oResponse.ok) {
					throw new Error(oResponse.statusText);
				} else {
					return oResponse.text();
				}
			}.bind(this))
			.then(function (sJwtToken) {
				this._sJwtToken = sJwtToken;
				if(bSaveLoginData) {
					window.localStorage._sJwtToken = sJwtToken;
				}
				this.doLogin(sJwtToken);
				this._oLoginDialog.close();
			}.bind(this))
			.catch(function (sError) {
				var oMessageStrip = this._oLoginDialog.getContent()[0];
				oMessageStrip.setVisible(true);
				oMessageStrip.setText(sError);
			}.bind(this));
		};
		
		XMLHttpRequestModifier.prototype.doLogin = function (sJwtToken) {
			// eyJpc3MiOiJodHRwczovL2xvY2FsaG9zdCIsImF1ZCI6IiouaW5jZW50ZXJneS5kZSIsImV4cCI6MTU3MjE2NzEyNywianRpIjoiQUZrSDJpMnZpSVRFc0QxVXNZMXZjQSIsImlhdCI6MTU2OTU3NTEyNywibmJmIjoxNTY5NTc1MDA3LCJzdWIiOiI3NjA3NTc2YS0wN2VlLTQzZmUtYTUzYi1kN2I2ZDg0YTVkY2UiLCJlbWFpbCI6ImNocmlzdGlhbkBleGFtcGxlLmNvbSIsImxvZ2luTmFtZSI6ImNocmlzdGlhbiIsImdyb3VwcyI6WyJFbXBsb3llZSJdfQ
			// {"iss":"https://localhost","aud":"*.incentergy.de","exp":1572167127,"jti":"AFkH2i2viITEsD1UsY1vcA","iat":1569575127,"nbf":1569575007,"sub":"7607576a-07ee-43fe-a53b-d7b6d84a5dce","email":"christian@example.com","loginName":"christian","groups":["Employee"]}
			var oJwtToken = JSON.parse(atob(sJwtToken.match(/\.(.*)\./)[1]));
			sap.ui.getCore().getEventBus().publish("incentergy.base.login.XMLHttpRequestModifier", "loginSuccess", oJwtToken);
			sap.ui.getCore().getModel("JWT").setData(oJwtToken, "JWT");
			
			this.fireLoginSuccess();
		};

		XMLHttpRequestModifier.prototype.exit = function () {
			if (this._oLoginDialog) {
				this._oLoginDialog.destroy();
				this._oLoginDialog = null;
			}
		};

		return XMLHttpRequestModifier;
	}, true);