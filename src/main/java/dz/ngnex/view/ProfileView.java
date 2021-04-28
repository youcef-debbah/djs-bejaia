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

import dz.ngnex.bean.DossierBean;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.DossierServlet;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.BasicAssociationEntity;
import dz.ngnex.entity.Constrains;
import dz.ngnex.entity.DossierInfoEntity;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ViewModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Named("profile")
@ViewModel
public class ProfileView implements Serializable {
  private static final long serialVersionUID = -1465599600432240728L;

  @Inject
  private Logger log;

  @EJB
  private PrincipalBean principalBean;

  @EJB
  private DossierBean dossierBean;

  @Inject
  private Messages messages;

  @Nullable
  private BasicAssociationEntity currentAssociation;

  @NotNull
  private List<DossierInfoEntity> dossierFiles = Collections.emptyList();

  @Inject
  Meta meta;

  @Inject
  CurrentPrincipal currentPrincipal;

  @PostConstruct
  private void init() {
    updateState(null);
  }

  void updateState(@Nullable BasicAssociationEntity currentAssociation) {
    this.currentAssociation = currentAssociation;

    if (currentAssociation != null && currentAssociation.getName() != null)
      dossierFiles = dossierBean.getAll(currentAssociation.getName());
    else
      dossierFiles = Collections.emptyList();
  }

  @Nullable
  public BasicAssociationEntity getCurrentAssociation() {
    return currentAssociation;
  }

  @NotNull
  public List<DossierInfoEntity> getDossierFiles() {
    return dossierFiles;
  }

  void save() {
    BasicAssociationEntity association = getCurrentAssociation();
    if (association != null) {
      association.setLastUpdater(currentPrincipal.getName());
      this.currentAssociation = principalBean.updateAssociationInfo(association);
    }
  }

  public String getEmailRegex() {
    return Constrains.EMAIL_REGEX;
  }

  public String getCurrentDossierUrl() {
    if (currentPrincipal != null && currentPrincipal.getName() != null)
      return DossierServlet.getDossierUrlAsAttachment(currentPrincipal.getName());
    else
      return "#";
  }

  public String getDossierFileUrl(String filename) {
    if (StringUtils.isBlank(filename))
      return "#";
    else
      return DossierServlet.getUrlAsAttachment(filename);
  }

  @Override
  public String toString() {
    return "ProfileView{" +
        "currentAssociation=" + getCurrentAssociation() +
        '}';
  }
}
