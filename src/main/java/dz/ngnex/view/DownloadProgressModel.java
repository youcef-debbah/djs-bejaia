package dz.ngnex.view;

import dz.ngnex.bean.Progress;
import dz.ngnex.bean.StatisticManager;
import dz.ngnex.control.Meta;
import dz.ngnex.util.ViewModel;

import javax.ejb.EJB;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

@ViewModel
public class DownloadProgressModel extends LazyEnabledModel {
  private static final long serialVersionUID = -7382453502718840427L;

  @EJB
  private StatisticManager statisticManager;

  @Inject
  private Meta meta;

  private Collection<Progress> sportDownloadsProgress;
  private Collection<Progress> youthDownloadsProgress;

  public Collection<Progress> getSportDownloadsProgress() {
    if (sportDownloadsProgress == null)
      try {
        sportDownloadsProgress = statisticManager.getDownloadsProgressForSportAssociations();
      } catch (Exception e) {
        meta.handleException(e);
        return Collections.emptyList();
      }
    return sportDownloadsProgress;
  }

  public Collection<Progress> getYouthDownloadsProgress() {
    if (youthDownloadsProgress == null)
      try {
        youthDownloadsProgress = statisticManager.getDownloadsProgressForYouthAssociations();
      } catch (Exception e) {
        meta.handleException(e);
        return Collections.emptyList();
      }
    return youthDownloadsProgress;
  }
}
