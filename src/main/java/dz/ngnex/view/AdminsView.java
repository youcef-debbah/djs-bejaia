/*
 * Handcrafted with love by Youcef DEBBAH
 * Copyright 2019 youcef-debbah@hotmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dz.ngnex.view;

import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.AdminEntity;
import dz.ngnex.entity.DatabaseEntity;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ViewModel;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

import static dz.ngnex.util.Config.GLOBAL_MSG;

@ViewModel
public class AdminsView implements Serializable {
  private static final long serialVersionUID = -567649281187666643L;

  @Inject
  private Logger log;

  @EJB
  PrincipalBean accountsManager;

  @Inject
  private Messages messages;

  private List<AdminEntity> admins;

  @Inject
  private AdminProfileView adminProfileView;

  @Inject
  private Meta meta;

  @PostConstruct
  public void init() {
    admins = fetchAdmins();
  }

  private List<AdminEntity> fetchAdmins() {
    return accountsManager.getAllAdmins();
  }

  public List<AdminEntity> getAdmins() {
    return admins;
  }

  public void deleteCurrentAccount() {
    adminProfileView.deleteCurrentAccount();
    admins = fetchAdmins();
  }

  public void saveChanges() {
    try {
      adminProfileView.saveChanges();
      FacesContext.getCurrentInstance().addMessage(GLOBAL_MSG,
          new FacesMessage(messages.getString("dataUpdated"), messages.getString("changesSaved")));
      admins = fetchAdmins();
      adminProfileView.refresh();
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public void refresh(AdminEntity newAdmin) {
    DatabaseEntity.requireID(newAdmin);
    try {
      admins.add(newAdmin);
      adminProfileView.setCurrentAdmin(newAdmin);
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public void refresh() {
    try {
      admins = fetchAdmins();
      adminProfileView.refresh();
    } catch (Exception e) {
      meta.handleException(e);
    }
  }
}
