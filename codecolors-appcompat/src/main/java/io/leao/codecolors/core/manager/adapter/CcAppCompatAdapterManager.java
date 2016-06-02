package io.leao.codecolors.core.manager.adapter;

public class CcAppCompatAdapterManager extends CcAdapterManager {
    @Override
    protected AdapterDefStyleHandler onCreateDefStyleAdaptersHandler() {
        return new AppCompatAdapterDefStyleHandler();
    }
}
