package dz.ngnex.view;

import dz.ngnex.util.WebKit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class ApplicationInfo {

    public static final long STARTUP_EPOCH = System.currentTimeMillis();
    private volatile long buildEpoch;

    @PostConstruct
    private synchronized void init() {
        buildEpoch = WebKit.getBuildEpoch();
    }

    public long getBuildEpoch() {
        return buildEpoch;
    }
}
