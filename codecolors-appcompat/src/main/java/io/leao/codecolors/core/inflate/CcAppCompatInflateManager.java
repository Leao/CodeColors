package io.leao.codecolors.core.inflate;

public class CcAppCompatInflateManager extends CcInflateManager {
    @Override
    protected DefStyleHandler onCreateDefStyleAdaptersHandler() {
        return new AppCompatDefStyleHandler();
    }
}
