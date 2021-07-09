package dz.ngnex.view;

import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.entity.Tutorial;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class TutorialsView implements Serializable {
    private static final long serialVersionUID = -2075181126515083360L;

    @Inject
    private CurrentPrincipal currentPrincipal;

    @EJB
    private PrincipalBean principalBean;

    private int tutorialsDone;

    @PostConstruct
    private synchronized void init() {
        Integer tutorials = principalBean.getTutorials(currentPrincipal.getReference());
        this.tutorialsDone = tutorials == null ? 0 : tutorials;
    }

    public synchronized boolean markDossierTutorialDone() {
        if (!isOpenDossierDone()) {
            int tutorials = Tutorial.OPEN_DOSSIER_UPLOADER.addTo(this.tutorialsDone);
            principalBean.setTutorial(currentPrincipal.getReference(), tutorials);
            this.tutorialsDone = tutorials;
            return true;
        } else
            return false;
    }

    public synchronized boolean isOpenDossierDone() {
        return Tutorial.OPEN_DOSSIER_UPLOADER.isIn(tutorialsDone);
    }

    public synchronized boolean isProfileMenuNeeded() {
        return !isOpenDossierDone();
    }
}
