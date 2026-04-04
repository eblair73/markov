package org.openmarkov.dbgenerator.localize;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.annotation_processing.localization_bindings.BindLocalizations;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

//@BindLocalizations(filePath = "dbgenerator/localize/DBGenerator_en.xml", fileIsDirectoryChild = true)
public class DBGeneratorResourceBundleProvider  implements LocalizeResourcesProvider {
	
	@Override
	public @NotNull String getRootOfResources() {
		return "/dbgenerator";
	}
	
}
