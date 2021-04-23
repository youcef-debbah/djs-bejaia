package dz.ngnex.entity;

import dz.ngnex.bean.ContractBean;
import dz.ngnex.bean.IntegrityException;
import dz.ngnex.testkit.DatabaseTest;
import org.junit.Test;

import javax.ejb.EJB;

import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class BasicContractBeanTest extends DatabaseTest {

  @EJB
  private ContractBean contractBean;

  @Test
  public void newTemplateShouldHaveNonNullName() {
    assertThrows(IllegalArgumentException.class, () -> contractBean.addNewTemplate(null, null));
  }

  @Test
  public void newTemplateShouldHaveNonNullSeason() {
    ContractBean bean = spy(contractBean);
    when(bean.getCurrentSeason()).thenReturn(null);

    assertThrows(IntegrityException.class, () -> bean.addNewTemplate("template_without_season", null));
  }

  @Test
  public void getTemplatesDetails() {
    List<TemplateDetails> templatesDetails = contractBean.getTemplatesDetails();
  }
}
