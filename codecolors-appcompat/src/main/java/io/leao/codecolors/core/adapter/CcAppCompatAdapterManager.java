package io.leao.codecolors.core.adapter;

public class CcAppCompatAdapterManager extends CcAdapterManager {
    @Override
    protected AdapterDefStyleHandler onCreateDefStyleAdaptersHandler() {
        return new AppCompatAdapterDefStyleHandler();
    }
}
