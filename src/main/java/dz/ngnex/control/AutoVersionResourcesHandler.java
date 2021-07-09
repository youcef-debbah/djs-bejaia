package dz.ngnex.control;

import dz.ngnex.util.WebKit;
import org.omnifaces.resourcehandler.DefaultResourceHandler;
import org.omnifaces.util.Lazy;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceWrapper;

import static org.omnifaces.util.Faces.evaluateExpressionGet;
import static org.omnifaces.util.Faces.getInitParameter;
import static org.omnifaces.util.Utils.encodeURL;
import static org.omnifaces.util.Utils.isBlank;

public class AutoVersionResourcesHandler extends DefaultResourceHandler {

    /**
     * The context parameter name to specify value of the version to be appended to the resource URL.
     */
    public static final String PARAM_NAME_VERSION = "org.omnifaces.VERSIONED_RESOURCE_HANDLER_VERSION";

    private static final String VERSION_SUFFIX = "v=";
    private final Lazy<String> buildEpoch;
    private final Lazy<String> staticResourcesVersion;

    public AutoVersionResourcesHandler(ResourceHandler wrapped) {
        super(wrapped);
        staticResourcesVersion = new Lazy<>(() -> encodeURL(evaluateExpressionGet(getInitParameter(PARAM_NAME_VERSION))));
        buildEpoch = new Lazy<>(() -> encodeURL(String.valueOf(WebKit.getBuildEpoch())));
    }

    @Override
    public Resource decorateResource(Resource resource) {
        if (resource == null || isBlank(staticResourcesVersion.get())) {
            return resource;
        }

        String requestPath = resource.getRequestPath();

        if (requestPath.contains('&' + VERSION_SUFFIX) || requestPath.contains('?' + VERSION_SUFFIX)) {
            // ignore already-versioned resources
            return resource;
        } else if (requestPath.contains("layout-djs.css"))
            return new VersionedResource(resource, buildEpoch);
        else
            return new VersionedResource(resource, staticResourcesVersion);
    }

    private static class VersionedResource extends ResourceWrapper {
        private final Lazy<String> version;

        public VersionedResource(Resource wrapped, Lazy<String> version) {
            super(wrapped);
            this.version = version;
        }

        @Override
        public String getRequestPath() {
            String requestPath = getWrapped().getRequestPath();

            if (!requestPath.contains(ResourceHandler.RESOURCE_IDENTIFIER)) {
                // do not touch CDN resources
                return requestPath;
            }

            return requestPath + (requestPath.contains("?") ? '&' : '?') + VERSION_SUFFIX + version.get();
        }
    }
}