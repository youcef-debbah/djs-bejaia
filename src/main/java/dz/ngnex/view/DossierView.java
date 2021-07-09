package dz.ngnex.view;

import dz.ngnex.bean.DossierBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.DossierInfoEntity;
import dz.ngnex.entity.FilesStatistics;
import dz.ngnex.util.ViewModel;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ViewModel
public class DossierView implements Serializable {
    private static final long serialVersionUID = 8260722795234178216L;

    @EJB
    private DossierBean dossierBean;

    private List<DossierInfoEntity> files;
    private FilesStatistics statistics;

    @Inject
    private Meta meta;

    @Inject
    private CurrentPrincipal currentPrincipal;

    @Inject
    private TutorialsView tutorialsView;

    private boolean firstVisit;

    @PostConstruct
    private void init() {
        refresh();
        this.firstVisit = tutorialsView.markDossierTutorialDone();
    }

    public boolean isFirstVisit() {
        return firstVisit;
    }

    public List<DossierInfoEntity> getFiles() {
        return files;
    }

    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile uploadedFile = event.getFile();
        if (uploadedFile != null) {
            try {
                dossierBean.add(uploadedFile, currentPrincipal.getName());
                meta.dataUpdated("fileUploaded");
                refresh();
            } catch (Exception e) {
                meta.handleException(e);
            }
        }
    }

    public void delete(Integer id) {
        try {
            dossierBean.delete(id);
            meta.dataUpdated("fileDeleted");
            refresh();
        } catch (Exception e) {
            meta.handleException(e);
        }
    }

    private void refresh() {
        String name = currentPrincipal.getName();
        files = dossierBean.getAll(name);
        statistics = dossierBean.getStatistics(name);
    }

    public int getMaxFileSize() {
        return DossierBean.MAX_FILE_SIZE;
    }

    public long getTotalFilesCount() {
        return statistics.getCount();
    }

    public String getFormattedTotalFilesSize() {
        return statistics.getFormattedTotalFilesSize();
    }
}
