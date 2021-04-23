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

import dz.ngnex.bean.ContractBean;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.bean.StatisticManager;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.entity.BasicAssociationEntity;
import dz.ngnex.entity.ContractInstanceEntity;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ContractServlet extends HttpServlet {

  private static final long serialVersionUID = 6656271317822524838L;

  public static final String PREVIEW_PARAM = "preview";
  public static final String ACCOUNT_PARAM = "account";
  private static final String CONTRACT_PARAM = "contract";

  @EJB
  StatisticManager statisticManager;

  @EJB
  PrincipalBean principalBean;

  @EJB
  ContractBean contractBean;

  @Inject
  LocaleManager localeManager;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int contractID = Integer.parseInt(request.getParameter(CONTRACT_PARAM));
    ContractInstanceEntity contractInstance = contractBean.findAssignedContract(contractID);

    if (contractInstance == null) {
      response.getWriter().write("contract instance not found (id: " + contractID + ")");
      return;
    }

    boolean preview = Boolean.parseBoolean(request.getParameter(PREVIEW_PARAM));
    if (!preview && !contractInstance.getContractTemplate().canBeDownloadedAt(localeManager.getCurrentEpoch())) {
      response.getWriter().write("you can't download this contract until: "
          + localeManager.formatAsLocalDateTime(contractInstance.getContractTemplate().getDownloadDate()));
      return;
    }

    int accountID = Integer.parseInt(request.getParameter(ACCOUNT_PARAM));

    Contract contract;
    BasicAssociationEntity association = principalBean.findAssociationForContractDownload(accountID);
    if (association != null) {
      contract = new Contract(association, contractInstance, localeManager);
      contract.generate();
    } else {
      response.getWriter().write("association not found (id: " + accountID + ")");
      return;
    }

    InputStream input = contract.asInputStream();

    OutputStream outStream = response.getOutputStream();
    response.setContentType("application/pdf");
    response.setContentLength(contract.getLength());

    if (!preview) {
      response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", contract.getName()));
      statisticManager.countDownload(contractInstance.getId());
    }

    byte[] buffer = new byte[4096];
    int bytesRead;

    while ((bytesRead = input.read(buffer)) != -1)
      outStream.write(buffer, 0, bytesRead);

    input.close();
    outStream.close();
  }
}
