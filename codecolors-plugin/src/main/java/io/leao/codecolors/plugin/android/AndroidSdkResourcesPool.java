package io.leao.codecolors.plugin.android;

import io.leao.codecolors.plugin.res.Resource;

public class AndroidSdkResourcesPool extends Resource.Pool {
    /**
     * <p>Convert default drawable, color, and attr resources, to Android resources.
     * <p>Their default public state is {@code false}, as they have to be included in
     * the {@code public.xml} file to be publicly visible.
     */
    @Override
    public Resource getOrCreateResource(String name, Resource.Type type) {
        switch (type) {
            case DRAWABLE:
                type = Resource.Type.ANDROID_DRAWABLE;
                break;
            case COLOR:
                type = Resource.Type.ANDROID_COLOR;
                break;
            case ATTR:
                type = Resource.Type.ANDROID_ATTR;
                break;
        }
        // Name, type.
        return super.getOrCreateResource(name, type);
    }

    /**
     * By default, we consider that SDK Resources are not public.
     * <p>
     * We will parse the public resources later and set the public ones.
     */
    @Override
    protected Resource.Visibility getDefaultVisibility() {
        return Resource.Visibility.PRIVATE;
    }
}