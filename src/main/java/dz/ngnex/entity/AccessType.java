package dz.ngnex.entity;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum AccessType {
  SUPER_ADMIN("super_admin", null, false, true),
  SPORT_ADMIN("sport_admin", Service.SPORT_SERVICE, false, true),
  YOUTH_ADMIN("youth_admin", Service.YOUTH_SERVICE, false, true),
  SPORT_ASSOCIATION("sport_asso", Service.SPORT_SERVICE, true, false),
  YOUTH_ASSOCIATION("youth_asso", Service.YOUTH_SERVICE, true, false),
  GUEST(null, null, false, false);

  private final String securityRole;
  private final Service service;
  private final boolean isAssociation;
  private final boolean isAdmin;

  AccessType(String securityRole, Service service, boolean isAssociation, boolean isAdmin) {
    this.securityRole = securityRole;
    this.service = service;
    this.isAssociation = isAssociation;
    this.isAdmin = isAdmin;
  }

  public static List<AccessType> allAdminTypes() {
    AccessType[] allTypes = values();
    ArrayList<AccessType> result = new ArrayList<>(allTypes.length);
    for (AccessType type : allTypes)
      if (type.isAdmin)
        result.add(type);

    result.trimToSize();
    return result;
  }

  public static AccessType ofSecurityRole(String role) {
    Objects.requireNonNull(role);
    for (AccessType type : values())
      if (role.equals(type.securityRole))
        return type;

    return null;
  }

  @Nullable
  public String getSecurityRole() {
    return securityRole;
  }

  @Nullable
  public Service getService() {
    return service;
  }

  public boolean isAssociation() {
    return isAssociation;
  }

  public boolean isAdmin() {
    return isAdmin;
  }
}
