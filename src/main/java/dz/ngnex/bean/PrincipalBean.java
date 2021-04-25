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

package dz.ngnex.bean;

import dz.ngnex.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ejb.Local;
import java.util.List;

@Local
public interface PrincipalBean {

  List<AdminEntity> getAllAdmins();

  List<BasicAssociationEntity> getAllAssociations();

  BasicAssociationEntity getSingleAssociation(Integer id);

  List<BasicAssociationEntity> getAllSportAssociations();

  List<BasicAssociationEntity> getAllYouthAssociations();

  BasicPrincipalEntity findLoggedInPrincipalByName(String name);

  BasicPrincipalEntity findPrincipalByName(String name);

  AdminEntity findAdmin(Integer id);

  @Nullable <T extends BasicAssociationEntity> T findSelectedAssociation(EntityReference<T> principalReference);

  Integer findCurrentSeasonID();

  @Nullable <T extends BasicPrincipalEntity> T findPrincipal(EntityReference<T> principalReference);

  @NotNull <T extends BasicPrincipalEntity> T getPrincipal(EntityReference<T> principalReference) throws IntegrityException;

  BasicAssociationEntity updateAssociationInfo(BasicAssociationEntity entity);

  AdminEntity updateAdminInfo(AdminEntity updatedAdmin);

  AdminEntity createAdmin(String name, String password, String role)
      throws IntegrityException;

  SportAssociationEntity createSportAssociation(String name, String password, String description, Integer demand)
      throws IntegrityException;

  YouthAssociationEntity createYouthAssociation(String name, String password, String description, Integer demandID)
      throws IntegrityException;

  void deleteAdmin(Integer adminID);

  void deleteAssociation(EntityReference<? extends BasicAssociationEntity> associationRef);

  void deleteContractInstance(Integer contractID);

  void deleteContractInstance(Integer contractID, EntityReference<? extends BasicAssociationEntity> associationRef);

  <T extends AbstractAssociationEntity> void recalcContractDownloadState(EntityReference<? extends BasicAssociationEntity> associationRef);

  void archiveContractInstance(Integer contractId, EntityReference<? extends BasicAssociationEntity> associationRef);

  void unarchiveContractInstance(Integer contractId, EntityReference<? extends BasicAssociationEntity> associationRef);

  BasicPrincipalEntity findPrincipal(Integer id);

  void deleteTemplate(Integer contractTemplateID);

  Integer addContractInstance(Integer contractTemplateId,
                              EntityReference<? extends BasicAssociationEntity> associationRef);

  List<ContractInstanceEntity> getAllContracts(EntityReference<? extends BasicAssociationEntity> associationRef);

  void clear();

  BasicAssociationEntity findAssociationForContractDownload(Integer accountID);
  BasicAssociationEntity findAssociationForContractView(Integer accountID);
}
