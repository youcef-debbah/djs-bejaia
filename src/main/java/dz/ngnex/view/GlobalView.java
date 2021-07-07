package dz.ngnex.view;

import dz.ngnex.bean.ExtraBean;
import dz.ngnex.control.Meta;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ViewModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@ViewModel
public class GlobalView implements Serializable {
    private static final long serialVersionUID = 5396040060608077227L;

    @Inject
    @Push
    private PushContext globalAdvertisements;

    @Inject
    private Meta meta;

    @Inject
    private Messages messages;

    @EJB
    private ExtraBean extraBean;

    private Map<Integer, String> advertisements;

    private static final AtomicBoolean init = new AtomicBoolean(false);

    @PostConstruct
    private void init() {
        if (!init.getAndSet(true))
            removeAllExtras();
    }

    private void removeAllExtras() {
        extraBean.removeAll(ExtraBean.ADVERTISEMENTS_CATEGORY);
    }

    public void refreshAdvertisements() {
        try {
            advertisements = extraBean.getAll(ExtraBean.ADVERTISEMENTS_CATEGORY);
        } catch (Exception e) {
            meta.handleException(e);
            advertisements = Collections.emptyMap();
        }
    }

    public Collection<String> getAdvertisements() {
        if (advertisements == null)
            refreshAdvertisements();
        return advertisements.values();
    }

    public void issueRestartWarning() {
        System.out.println("########## issueRestartWarning");
        publishAdvertisement(ExtraBean.RESTART_WARNING_ID, messages.get("restart_warning"));
    }

    public void issueShutdownWarning() {
        System.out.println("########## issueShutdownWarning");
        publishAdvertisement(ExtraBean.SHUTDOWN_WARNING_ID, messages.get("shutdown_warning"));
    }

    public void clearWarnings() {
        try {
            removeAllExtras();
            meta.dataUpdatedSuccessfully();
            globalAdvertisements.send("refresh");
        } catch (Exception e) {
            meta.handleException(e);
        }
    }

    private void publishAdvertisement(int id, String value) {
        try {
            extraBean.put(id, value, ExtraBean.ADVERTISEMENTS_CATEGORY);
            meta.dataUpdatedSuccessfully();
            globalAdvertisements.send("refresh");
        } catch (Exception e) {
            meta.handleException(e);
        }
    }
}
