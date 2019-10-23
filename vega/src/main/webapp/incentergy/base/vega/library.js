sap.ui.define([
	"sap/ui/core/Core",
	"sap/ui/core/library"
],function(Core, Library) {
	"use strict";

	sap.ui.getCore().initLibrary({
		name : "incentergy.base.vega",
		noLibraryCSS: true,
		dependencies : [
			"sap.ui.core", "sap.f"
		],
		types: [],
		interfaces: [],
		controls: ["incentergy.base.vega.Vega"],
		elements: [],
		version: "1.0.0"
	});

	return incentergy.base.vega;

}, /* bExport= */ false);