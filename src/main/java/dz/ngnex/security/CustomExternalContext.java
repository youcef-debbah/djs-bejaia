package dz.ngnex.security;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;
import javax.faces.context.ExternalContextWrapper;

public class CustomExternalContext extends ExternalContextWrapper {

    private final boolean securedWebsockets;

    public CustomExternalContext(ExternalContext wrapped) {
        super(wrapped);
        securedWebsockets = "Production".equals(wrapped.getInitParameter("javax.faces.PROJECT_STAGE"));
        System.out.println("CustomExternalContext initialized (securedWebsockets: " + securedWebsockets + ")");
    }

    @Override
    public String encodeWebsocketURL(String url) {
        String encodedUrl = super.encodeWebsocketURL(url);
        if (encodedUrl != null && securedWebsockets)
            encodedUrl = encodedUrl.replaceFirst("ws://", "wss://");
        return encodedUrl;
    }

    public static class Factory extends ExternalContextFactory {
        public Factory(ExternalContextFactory wrapped) {
            super(wrapped);
        }

        @Override
        public ExternalContext getExternalContext(Object context, Object request, Object response) throws FacesException {
            return new CustomExternalContext(getWrapped().getExternalContext(context, request, response));
        }
    }
}