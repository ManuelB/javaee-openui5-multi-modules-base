sap.ui.define([
	"sap/ui/core/Core",
	"sap/ui/core/library",
	"./Connection"
],function(Core, Library, Connection) {
	"use strict";

	sap.ui.getCore().initLibrary({
		name : "incentergy.base.websocket",
		noLibraryCSS: true,
		dependencies : [
			"sap.ui.core"
		],
		types: ["incentergy.base.websocket.Connection"],
		interfaces: [],
		controls: [],
		elements: [],
		version: "1.0.0"
	});
	
	return incentergy.base.websocket;

}, /* bExport= */ false);