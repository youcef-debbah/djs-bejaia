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
import dz.ngnex.entity.BasicAssociationEntity;
import dz.ngnex.entity.ContractInstanceEntity;
import dz.ngnex.entity.SectionEntity;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ViewModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author youcef debbah
 */
@ViewModel
public class SingleUserView implements Serializable {

    private static final long serialVersionUID = -7529359910608973878L;

    private static final Logger log = LogManager.getLogger(SingleUserView.class);

    @EJB
    PrincipalBean principalBean;

    @Inject
    ProfileView profileView;

    @Inject
    SectionsView sectionsView;

    @Inject
    PropertiesView propertiesView;

    @Inject
    private Messages messages;

    @Inject
    private Meta meta;

    @Inject
    private CurrentPrincipal currentPrincipal;

    @PostConstruct
    public void init() {
        handleRefresh(RefreshAccountEvent.refreshAll());
    }

    public void handleRefresh(@Observes(notifyObserver = Reception.IF_EXISTS) RefreshAccountEvent event) {
        if (event != null) {
            if (currentPrincipal.isAssociation()) {
                setCurrentAccount(event.getAssociation(() -> principalBean.findPrincipal(currentPrincipal.getAssociationReference())),
                        event.getContractTemplateID(),
                        event.getSectionID());
            } else
                setCurrentAccount(null, null, null);
        }
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

    public BasicAssociationEntity getCurrentAccount() {
        return sectionsView.getCurrentAssociation();
    }

    public void setCurrentAccount(BasicAssociationEntity updatedAccount, Integer contractTemplateID, Integer sectionID) {
        if (updatedAccount != null) {
            sectionsView.updateState(updatedAccount, contractTemplateID, sectionID);
        } else
            sectionsView.updateState(null, null, null);

        syncSectionsView();
    }

    public ContractInstanceEntity getCurrentContract() {
        return sectionsView.getCurrentContract();
    }

    public void setCurrentContract(ContractInstanceEntity newContract) {
        Integer contractID = newContract != null ? newContract.getContractTemplate().getId() : null;
        sectionsView.updateState(sectionsView.getCurrentAssociation(), contractID, sectionsView.getCurrentSectionID());
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
                BasicAssociationEntity updatedAssociation = profileView.save();
                sectionsView.save(updatedAssociation);
                propertiesView.save(updatedAssociation);
                meta.dataUpdatedSuccessfully();

                handleRefresh(new RefreshAccountEvent(currentAccount.getReference(), sectionsView.getCurrentContractTemplateID(), sectionsView.getCurrentSectionID()));
            } catch (Exception e) {
                meta.handleException(e);
            }
    }

    @Override
    public String toString() {
        return "SingleUserView{" +
                "profileView=" + profileView +
                ", sectionsView=" + sectionsView +
                ", propertiesView=" + propertiesView +
                '}';
    }
}
