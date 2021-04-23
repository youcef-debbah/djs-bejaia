package dz.ngnex.view;

import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.AdminEntity;
import dz.ngnex.util.ViewModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@ViewModel
public class InfoView implements Serializable {
  private static final long serialVersionUID = -4861823205981373316L;

  @EJB
  private PrincipalBean principalBean;

  private List<AdminEntity> admins;

  @Inject
  private Meta meta;

  @PostConstruct
  private void init() {
    try {
      admins = principalBean.getAllAdmins();
    } catch (Exception e) {
      admins = Collections.emptyList();
      meta.handleException(e);
    }
  }

  public List<AdminEntity> getAdmins() {
    return admins;
  }
}
