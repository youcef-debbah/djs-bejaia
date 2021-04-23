package dz.ngnex.control;

import dz.ngnex.bean.AvatarBean;
import dz.ngnex.bean.IntegrityException;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.entity.*;
import dz.ngnex.security.ReadAccess;
import dz.ngnex.security.ReadableResource;
import dz.ngnex.security.WritableResource;
import dz.ngnex.security.WriteAccess;
import dz.ngnex.util.InjectableByTests;
import dz.ngnex.util.WebKit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.primefaces.model.UploadedFile;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

@Named
@SessionScoped
@InjectableByTests
public class CurrentPrincipal implements Serializable {
  private static final long serialVersionUID = 401407644891967961L;

  @EJB
  PrincipalBean principalBean;

  @EJB
  AvatarBean avatarBean;

  private AccessType type;
  private EntityReference<? extends BasicPrincipalEntity> reference;
  private EntityReference<? extends BasicAssociationEntity> associationReference;
  private AvatarInfoEntity avatar;

  public CurrentPrincipal() {
    reset();
  }

  @PostConstruct
  private void init() {
    refreshState();
  }

  public void refreshState() {
    HttpServletRequest request = WebKit.getFacesRequest();
    Principal userPrincipal = request.getUserPrincipal();
    fetchPrincipalData(userPrincipal, request);
  }

  public void refreshState(Principal userPrincipal, HttpServletRequest request) {
    fetchPrincipalData(userPrincipal, request);
  }

  private synchronized void fetchPrincipalData(Principal userPrincipal, HttpServletRequest request) {
    if (userPrincipal != null && userPrincipal.getName() != null && !Objects.equals(userPrincipal.getName(), getName())) {
      BasicPrincipalEntity principal = principalBean.findLoggedInPrincipalByName(userPrincipal.getName());
      if (principal != null) {
        type = getPrincipalType(request);
        avatar = avatarBean.getInfoByUploader(principal.getName());
        reference = Objects.requireNonNull(principal.getReference());
        if (BasicAssociationEntity.class.isAssignableFrom(reference.getType()))
          //noinspection unchecked
          associationReference = (EntityReference<BasicAssociationEntity>) reference;
        return;
      }
    }

    reset();
  }

  @NotNull
  public static AccessType getPrincipalType(HttpServletRequest request) {
    for (AccessType type : AccessType.values()) {
      String securityRole = type.getSecurityRole();
      if (securityRole != null && request.isUserInRole(securityRole))
        return type;
    }

    return AccessType.GUEST;
  }

  private synchronized void reset() {
    reference = null;
    associationReference = null;
    type = AccessType.GUEST;
  }

  @NotNull
  public synchronized AccessType getAccessType() {
    return type;
  }

  @Nullable
  public synchronized Integer getId() {
    return (reference != null) ? reference.getId() : null;
  }

  @Nullable
  public synchronized String getName() {
    return (reference != null) ? reference.getName() : null;
  }

  @Nullable
  public synchronized EntityReference<? extends BasicPrincipalEntity> getReference() {
    return reference;
  }

  @Nullable
  public synchronized EntityReference<? extends BasicAssociationEntity> getAssociationReference() {
    return associationReference;
  }

  @Nullable
  public synchronized AvatarInfoEntity getAvatar() {
    return avatar;
  }

  public synchronized void setAvatar(UploadedFile file) throws IntegrityException {
    String name = getName();
    if (name != null) {
      if (file != null)
        avatarBean.add(file, name);
      else
        avatarBean.deleteByUploader(name);

      avatar = avatarBean.getInfoByUploader(name);
    }
  }

  public synchronized boolean isSuperAdmin() {
    return isAdmin() && getService() == null;
  }

  public synchronized boolean isBasicAdmin() {
    return isAdmin() && getService() != null;
  }

  public boolean isGuest() {
    return getAccessType() == AccessType.GUEST;
  }

  public boolean isAssociation() {
    return getAccessType().isAssociation();
  }

  public boolean isAdmin() {
    return getAccessType().isAdmin();
  }

  public boolean isLoggedIn() {
    return getName() != null;
  }

  public boolean isLoggedOut() {
    return getName() == null;
  }

  @Nullable
  public String getSecurityRole() {
    return getAccessType().getSecurityRole();
  }

  @Nullable
  public Service getService() {
    return getAccessType().getService();
  }

  public synchronized boolean hasAccess(WritableResource resource, Service service) {
    if (resource != null) {
      WriteAccess access = resource.getAccess();
      switch (access) {
        case SPORT_ASSO_ONLY:
          return service == Service.SPORT_SERVICE && isAssociation();
        case YOUTH_ASSO_ONLY:
          return service == Service.YOUTH_SERVICE && isAssociation();
        case SPORT_ADMIN_ONLY:
          return service == Service.SPORT_SERVICE && isAdmin();
        case YOUTH_ADMIN_ONLY:
          return service == Service.YOUTH_SERVICE && isAdmin();
        case SPORT_ADMIN_AND_ASSO:
          return service == Service.SPORT_SERVICE && (isAdmin() || isAssociation());
        case YOUTH_ADMIN_AND_ASSO:
          return service == Service.YOUTH_SERVICE && (isAdmin() || isAssociation());
      }
    }
    return false;
  }

  public synchronized boolean hasAccess(ReadableResource resource) {
    if (resource != null) {
      ReadAccess access = resource.getAccess();
      switch (access) {
        case ANYONE:
          return true;
        case ADMINS_ONLY:
          return getAccessType().isAdmin();
        case ADMINS_AND_ASSOCIATIONS:
          return getAccessType().isAdmin() || getAccessType().isAssociation();
      }
    }

    return false;
  }

  public synchronized ReadAccess getHighestReadAccess() {
    if (isAdmin())
      return ReadAccess.ADMINS_ONLY;

    if (isAssociation())
      return ReadAccess.ADMINS_AND_ASSOCIATIONS;

    return ReadAccess.ANYONE;
  }

  @Override
  public String toString() {
    return "CurrentPrincipal{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        ", type=" + getAccessType() +
        '}';
  }
}
