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

/*
 * Copyright (c) 2016 youcef debbah (youcef-kun@hotmail.fr)
 *
 * This file is part of cervex.
 *
 * cervex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * cervex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cervex.  If not, see <http://www.gnu.org/licenses/>.
 *
 * created on 2017/01/06
 * @header
 */

package dz.ngnex.view;

import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.*;
import dz.ngnex.util.Config;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ViewModel;
import dz.ngnex.util.WebKit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author youcef debbah
 */
@ViewModel
public class UsersView implements Serializable {

  private static final long serialVersionUID = -7529359910608973878L;

  private static final Logger log = LogManager.getLogger(UsersView.class);
  private static final String ASSOCIATION_ID = "asso_id";

  @EJB
  PrincipalBean principalBean;

  @Inject
  ProfileView profileView;

  @Inject
  SectionsView sectionsView;

  @Inject
  PropertiesView propertiesView;

  private List<BasicAssociationEntity> filteredAccounts;

  private List<String> excludedTemplates;

  @Inject
  private Messages messages;

  private List<BasicAssociationEntity> accounts;

  private Service service;

  @Inject
  private Meta meta;

  @Inject
  private CurrentPrincipal currentPrincipal;
  private BasicAssociationEntity targetAssociation;

  @PostConstruct
  public void init() {
    service = currentPrincipal.getService();
    refreshAll();

    Integer currentAssociationID = WebKit.getRequestParamAsInt(ASSOCIATION_ID);
    targetAssociation = principalBean.getSingleAssociation(currentAssociationID);
    setCurrentAccount(targetAssociation);
  }

  public BasicAssociationEntity getTargetAssociation() {
    return targetAssociation;
  }

  public void refreshAll() {
    handleRefresh(RefreshAccountEvent.refreshAll());
  }

  public void handleRefresh(@Observes(notifyObserver = Reception.IF_EXISTS) RefreshAccountEvent event) {
    if (event != null) {
      EntityReference<? extends BasicAssociationEntity> accountReference = event.getAssociationReference();
      int index = DatabaseEntity.indexOf(accountReference, accounts);
      if (index >= 0)
        try {
          int indexInFilter = DatabaseEntity.indexOf(accountReference, filteredAccounts);
          BasicAssociationEntity updatedAccount = event.getAssociation(() -> principalBean.findSelectedAssociation(accountReference));

          if (updatedAccount != null) {
            accounts.set(index, updatedAccount);
            if (indexInFilter >= 0)
              filteredAccounts.set(indexInFilter, updatedAccount);
          } else {
            accounts.remove(index);
            if (indexInFilter >= 0)
              filteredAccounts.remove(indexInFilter);
          }

          BasicAssociationEntity currentAssociation = sectionsView.getCurrentAssociation();
          if (DatabaseEntity.equalsID(updatedAccount, currentAssociation)) {
            sectionsView.setState(updatedAccount, event.getContractTemplateID(), event.getSectionID());
            syncSectionsView();
          } else
            updateAccount(currentAssociation, event.getContractTemplateID(), event.getSectionID());


        } catch (Exception e) {
          meta.handleException(e);
          fetchAllAccounts(null, null);
        }
      else
        fetchAllAccounts(event.getContractTemplateID(), event.getSectionID());
    }
  }

  private void updateAccount(BasicAssociationEntity updatedAccount, Integer contractTemplateID, Integer sectionID) {
    if (updatedAccount != null)
      sectionsView.updateState(updatedAccount, contractTemplateID, sectionID);
    else
      sectionsView.updateState(null, null, null);

    syncSectionsView();
  }

  private void updateContract(ContractInstanceEntity contractInstance) {
    Integer contractID = contractInstance != null ? contractInstance.getContractTemplate().getId() : null;
    sectionsView.updateState(sectionsView.getCurrentAssociation(), contractID, sectionsView.getCurrentSectionID());
    syncSectionsView();
  }

  private void syncSectionsView() {
    BasicAssociationEntity currentAssociation = sectionsView.getCurrentAssociation();
    ContractInstanceEntity currentContract = sectionsView.getCurrentContract();
    propertiesView.updateState(currentAssociation, currentContract);
    profileView.updateState(currentAssociation);
  }

  private void fetchAllAccounts(Integer nextTemplateID, Integer nextSectionID) {
    if (service == null)
      accounts = principalBean.getAllAssociations();
    else if (service == Service.SPORT_SERVICE)
      accounts = principalBean.getAllSportAssociations();
    else if (service == Service.YOUTH_SERVICE)
      accounts = principalBean.getAllYouthAssociations();
    else
      accounts = null;

    filteredAccounts = null;

    BasicAssociationEntity currentAssociation = sectionsView.getCurrentAssociation();
    if (currentAssociation != null)
      updateAccount(DatabaseEntity.get(currentAssociation.getId(), accounts), nextTemplateID, nextSectionID);
    else
      updateAccount(null, null, null);
  }

  public List<BasicAssociationEntity> getAccounts() {
    return accounts;
  }

  public List<BasicAssociationEntity> getFilteredAccounts() {
    return filteredAccounts;
  }

  public void setFilteredAccounts(List<BasicAssociationEntity> filteredAccounts) {
    this.filteredAccounts = filteredAccounts;
  }

  public List<String> getExcludedTemplates() {
    return excludedTemplates;
  }

  public void setExcludedTemplates(List<String> excludedTemplates) {
    this.excludedTemplates = excludedTemplates;
  }

  public BasicAssociationEntity getCurrentAccount() {
    return sectionsView.getCurrentAssociation();
  }

  public void setCurrentAccount(BasicAssociationEntity currentAccount) {
    if (DatabaseEntity.notEqualsID(getCurrentAccount(), currentAccount))
      updateAccount(currentAccount, null, null);
  }

  public ContractInstanceEntity getCurrentContract() {
    return sectionsView.getCurrentContract();
  }

  public void setCurrentContract(ContractInstanceEntity newContract) {
    if (DatabaseEntity.equalsID(getCurrentContract(), newContract))
      return;

    Integer contractID = newContract != null ? newContract.getContractTemplate().getId() : null;
    try {
      sectionsView.updateState(sectionsView.getCurrentAssociation(), contractID, sectionsView.getCurrentSectionID());
    } catch (Exception e) {
      meta.handleException(e);
      sectionsView.updateState(sectionsView.getCurrentAssociation(), null, null);
    }
    syncSectionsView();
  }

  public SectionEntity getCurrentSection() {
    return sectionsView.getCurrentSection();
  }

  public void setCurrentSection(SectionEntity newSection) {
    Integer sectionID = newSection != null ? newSection.getId() : null;
    sectionsView.updateState(sectionsView.getCurrentAssociation(), sectionsView.getCurrentContractTemplateID(), sectionID);
    syncSectionsView();
  }

  public void saveChanges() {
    BasicAssociationEntity currentAccount = getCurrentAccount();
    if (currentAccount == null)
      meta.noSelectionError();
    else
      try {
        profileView.save();
        sectionsView.save();
        propertiesView.save();
        meta.dataUpdatedSuccessfully();

        handleRefresh(new RefreshAccountEvent(currentAccount.getReference(), sectionsView.getCurrentContractTemplateID(), sectionsView.getCurrentSectionID()));
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void deleteCurrentAccount() {
    BasicAssociationEntity targetAccount = getCurrentAccount();
    if (targetAccount == null)
      meta.noSelectionError();
    else
      try {
        principalBean.deleteAssociation(targetAccount.getReference());
        meta.addGlobalMessage("dataUpdated", "accDeleted");
        handleRefresh(new RefreshAccountEvent(targetAccount.getReference(), null, null));
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void archiveContractInstance(Integer contractID) {
    BasicAssociationEntity currentAccount = getCurrentAccount();
    if (currentAccount == null || contractID == null) {
      meta.noSelectionError();
    } else
      try {
        EntityReference<? extends BasicAssociationEntity> currentAccountRef = currentAccount.getReference();
        principalBean.archiveContractInstance(contractID, currentAccountRef);
        meta.workDoneSuccessfully("archivingDone");
        handleRefresh(new RefreshAccountEvent(currentAccountRef, null, null));
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

//  public long getValidAccountsCount() {
//    final List<AccountEntity> accounts = this.accounts;
//    if (accounts == null || accounts.isEmpty())
//      return 0;
//    else
//      return accounts.stream()
//          .map(AccountEntity::getAssociation)
//          .filter(Objects::nonNull)
//          .filter(AssociationEntity::getValid)
//          .count();
//  }

  public void chatWithCurrentAssociation() {
    BasicAssociationEntity currentAccount = getCurrentAccount();
    if (currentAccount != null)
      WebKit.redirect(Config.ADMIN_CHAT_PAGE + "?id=" + currentAccount.getId());
  }

  @Override
  public String toString() {
    return "AccountsView{" +
        "profileView=" + profileView +
        ", sectionsView=" + sectionsView +
        ", propertiesView=" + propertiesView +
        '}';
  }
}
