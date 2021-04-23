package dz.ngnex.bean;

import dz.ngnex.common.TransactionalArquillian;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class IntegrationTests extends TransactionalArquillian {

  @Deployment
  public static WebArchive createDeployment() {
    return createTestWar();
  }

//  @EJB
//  private PrincipalBean principalBean;
//
//  @Test
//  public void syncPrincipals() {
//    List<BasicAssociationEntity> associations = principalBean.getAllAssociations();
//    for (BasicAssociationEntity association : associations) {
//      association.recalcContractDownloadState();
//      principalBean.updateAssociationInfo(association);
//    }
//  }

  @Test
  public void shouldSelectLiteral() {
    Object result = em.createNativeQuery("select 1").getSingleResult();
    assertThat(result, equalTo(1));
  }
}
