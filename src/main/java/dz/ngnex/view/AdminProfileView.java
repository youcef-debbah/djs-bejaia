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
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.AccessType;
import dz.ngnex.entity.AdminEntity;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ViewModel;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Named("adminProfile")
@ViewModel
public class AdminProfileView implements Serializable {

  private static final long serialVersionUID = -4028015313049346195L;

  @Inject
  private Logger log;

  @EJB
  private PrincipalBean manager;

  @Inject
  private Messages messages;

  @Nullable
  private AdminEntity currentAdmin;

  @Size(max = 45)
  private String password;

  private String username;

  private AccessType type;

  @Inject
  CurrentPrincipal currentPrincipal;

  @Inject
  Meta meta;

  @PostConstruct
  private void init() {
    setCurrentAdmin(null);
  }

  public void setCurrentAdmin(@Nullable AdminEntity currentAdmin) {
    this.currentAdmin = currentAdmin;
    updateViewState(currentAdmin);
  }

  private void updateViewState(@Nullable AdminEntity currentAccount) {
    if (currentAccount != null) {
      password = currentAccount.getPassword();
      username = currentAccount.getName();
      type = AccessType.ofSecurityRole(currentAccount.getRole());
    } else {
      password = "";
      username = "";
      type = AccessType.SPORT_ADMIN;
    }
  }

  public void saveChanges() {
    if (currentAdmin == null || getType() == null) {
      meta.noSelectionError();
      return;
    }

    try {
      currentAdmin.setPassword(getPassword());
      currentAdmin.setRole(getType().getSecurityRole());
      manager.updateAdminInfo(currentAdmin);
      meta.dataUpdatedSuccessfully();
    } catch (Exception e) {
      refresh();
      meta.handleException(e);
    }
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public AccessType getType() {
    return type;
  }

  public void setType(AccessType type) {
    this.type = type;
  }

  @Nullable
  public AdminEntity getCurrentAdmin() {
    return currentAdmin;
  }

  public void deleteCurrentAccount() {
    if (currentAdmin != null) {
      manager.deleteAdmin(currentAdmin.getId());
      setCurrentAdmin(null);
    }
  }

  public void refresh() {
    if (currentAdmin != null) {
      setCurrentAdmin(manager.findAdmin(currentAdmin.getId()));
    } else
      setCurrentAdmin(null);
  }

  public List<AccessType> getAllAdminRoles() {
    return AccessType.allAdminTypes();
  }
}
