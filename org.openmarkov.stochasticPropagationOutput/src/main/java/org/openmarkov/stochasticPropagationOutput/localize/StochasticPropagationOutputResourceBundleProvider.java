package org.openmarkov.stochasticPropagationOutput.localize;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.spi.LocalizeResourcesProvider;

//@BindLocalizations(filePath = "stochasticpropagationoutput/localize/stochasticPropagationOutput_en.xml", fileIsDirectoryChild = true)
public class StochasticPropagationOutputResourceBundleProvider implements LocalizeResourcesProvider {
    
    @Override
    public @NotNull String getRootOfResources() {
        return "/stochasticpropagationoutput";
    }
}
