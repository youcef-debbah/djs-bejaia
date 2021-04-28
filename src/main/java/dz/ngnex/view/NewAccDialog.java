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

import dz.ngnex.bean.AccountDemandBean;
import dz.ngnex.bean.IntegrityException;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.*;
import dz.ngnex.util.ViewModel;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Named
@ViewModel
public class NewAccDialog implements Serializable {
  private static final long serialVersionUID = -1132974646032454006L;
  public static final String DEMAND_ID = "demand";

  @Inject
  private Logger log;

  @EJB
  private PrincipalBean manager;

  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  private String newUsername;

  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  private String newPassword;

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  private String newDescription = "";

  private AccessType accountType;

  private AccessType[] availableAccountTypes;

  @Inject
  private Event<RefreshAccountEvent> refreshEvent;

  @Inject
  UsersView usersView;

  @Inject
  AdminsView adminsView;

  @EJB
  PrincipalBean accountsManager;

  @Inject
  CurrentPrincipal currentPrincipal;

  @Inject
  Meta meta;

  @EJB
  private AccountDemandBean accountDemandBean;

  @PostConstruct
  public void init() {
    initAvailableAccountTypes();
    resetInput();
  }

  private void initAvailableAccountTypes() {
    AccessType[] types = calcAvailableAccountTypes();
    if (types.length > 0)
      accountType = types[0];
    else
      accountType = null;

    availableAccountTypes = types;
  }

  private AccessType[] calcAvailableAccountTypes() {
    switch (currentPrincipal.getAccessType()) {
      case SPORT_ADMIN:
        return new AccessType[]{AccessType.SPORT_ASSOCIATION, AccessType.SPORT_ADMIN};
      case YOUTH_ADMIN:
        return new AccessType[]{AccessType.YOUTH_ASSOCIATION, AccessType.YOUTH_ADMIN};
      case SUPER_ADMIN:
        return new AccessType[]{AccessType.SPORT_ASSOCIATION, AccessType.YOUTH_ASSOCIATION};
      default:
        return new AccessType[0];
    }
  }

  public String getNewUsername() {
    return newUsername;
  }

  public void setNewUsername(String newUsername) {
    this.newUsername = newUsername;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public AccessType getAccountType() {
    return accountType;
  }

  public void setAccountType(AccessType accountType) {
    this.accountType = accountType;
  }

  public String getNewDescription() {
    return newDescription;
  }

  public void setNewDescription(String newDescription) {
    this.newDescription = newDescription;
  }

  public void newUser() {
    newUser(null);
  }

  public void newUser(Integer demandID) {
    try {
      AccessType newAccountType = this.accountType;
      if (newAccountType != null) {
        if (newAccountType.isAdmin()) {
          AdminEntity newAdmin = manager.createAdmin(getNewUsername(), getNewPassword(), newAccountType.getSecurityRole());
          adminsView.refresh(newAdmin);
        } else if (newAccountType == AccessType.SPORT_ASSOCIATION) {
          SportAssociationEntity newAsso = manager.createSportAssociation(getNewUsername(), getNewPassword(), getNewDescription(), demandID, currentPrincipal.getName());
          refreshEvent.fire(new RefreshAccountEvent(newAsso));
        } else if (newAccountType == AccessType.YOUTH_ASSOCIATION) {
          YouthAssociationEntity newAsso = manager.createYouthAssociation(getNewUsername(), getNewPassword(), getNewDescription(), demandID, currentPrincipal.getName());
          refreshEvent.fire(new RefreshAccountEvent(newAsso));
        } else {
          throw new IntegrityException("unexpected new account type: " + newAccountType, "accessDenied");
        }

        resetInput();
        meta.addGlobalMessage("dataUpdated", "newAccAdded");
        PrimeFaces.current().executeScript(ClientScripts.PF_MAIN_DIALOG_HIDE);
      }
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  private void resetInput() {
    setNewUsername(null);
    setNewPassword(null);
    setNewDescription("");
  }

  public AccessType[] getAvailableAccountTypes() {
    return availableAccountTypes;
  }
}
