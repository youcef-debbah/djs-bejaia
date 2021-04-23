package dz.ngnex.view;

import dz.ngnex.util.ViewModel;
import org.primefaces.model.DefaultOrganigramNode;
import org.primefaces.model.OrganigramNode;

import javax.annotation.PostConstruct;
import java.io.Serializable;

@ViewModel
public class OrganisationView implements Serializable {
  private static final long serialVersionUID = 596553947064860015L;
  public static final String DESKTOP = "desktop";
  public static final String SERVICE = "service";

  private OrganigramNode root;

  @PostConstruct
  private void init() {
    DefaultOrganigramNode director = new DefaultOrganigramNode(DESKTOP, "director", null);
    director.setCollapsible(false);

    new DefaultOrganigramNode(DESKTOP, "inspection", director);
    new DefaultOrganigramNode(DESKTOP, "generalSecretary", director);
    addService(director, "educationService", "physicalOffice", "detectionOffice", "sportOffice");
    addService(director, "activitiesService", "communicationOffice", "socioOffice", "youthOffice");
    addService(director, "investService", "infrastructureOffice", "standardOffice", "statsOffice");
    addService(director, "trainingService", "personnelService", "budgetBureau", "meansOffice");

    this.root = director;
  }

  protected void addService(OrganigramNode parent, String name, String... desktops) {
    OrganigramNode serviceNode = new DefaultOrganigramNode(SERVICE, name, parent);

    if (desktops != null) {
      for (String employee : desktops) {
        new DefaultOrganigramNode(DESKTOP, employee, serviceNode);
      }
    }
  }

  public OrganigramNode getRoot() {
    return root;
  }
}
