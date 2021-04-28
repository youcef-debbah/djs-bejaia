package dz.ngnex.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.regex.Pattern;

@MappedSuperclass
public abstract class AbstractAssociationEntity extends BasicPrincipalEntity {
  private static final long serialVersionUID = 4328659431329312767L;

  private static final Pattern VALID_AGREMENT = Pattern.compile("\\d+/\\d+");
  private static final Pattern LEADING_ZEROS = Pattern.compile("^0+");
  private static final Pattern TRAILING_ZEROS = Pattern.compile("0+$");

  private String description = "";
  private String agrement = "";
  private String adresse = "";
  private String president = "";
  private String phone = "";
  private String compte = "";
  private String banque = "";
  private String agence = "";
  private String email = "";
  private Long lastUpdate;
  private String lastUpdater;

  private Integer uploadedFilesCount;
  private Integer unreadMessagesCount;
  private Integer downloadedContractsCount;
  private Integer contractsCount;

  @Transient
  private ContractDownloadState contractDownloadState;

  @Transient
  private String canonicAgrement;

  protected AbstractAssociationEntity() {
  }

  protected AbstractAssociationEntity(String name, String password, String description) {
    super(name, password);
    setDescription(description);
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getAgrement() {
    return agrement;
  }

  public void setAgrement(String agrement) {
    this.agrement = agrement;

    if (agrement == null)
      canonicAgrement = null;
    else {
      String trimmed = agrement.trim();
      trimmed = LEADING_ZEROS.matcher(trimmed).replaceAll("");
      trimmed = TRAILING_ZEROS.matcher(trimmed).replaceAll("");
      canonicAgrement = trimmed;
    }
  }

  @Transient
  public String getCanonicAgrement() {
    return canonicAgrement;
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getAdresse() {
    return adresse;
  }

  public void setAdresse(String adresse) {
    this.adresse = adresse;
  }

  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getPresident() {
    return president;
  }

  public void setPresident(String president) {
    this.president = president;
  }

  @Size(max = Constrains.MAX_PHONE_NUMBER_SIZE)
  @Column(length = Constrains.MAX_PHONE_NUMBER_SIZE)
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getCompte() {
    return compte;
  }

  public void setCompte(String compte) {
    this.compte = compte;
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getBanque() {
    return banque;
  }

  public void setBanque(String banque) {
    this.banque = banque;
  }

  @Size(max = Constrains.MAX_PHRASE_LENGTH)
  @Column(length = Constrains.MAX_PHRASE_LENGTH)
  public String getAgence() {
    return agence;
  }

  public void setAgence(String agence) {
    this.agence = agence;
  }

  @Size(max = Constrains.MAX_EMAIL_LENGTH)
  @Column(length = Constrains.MAX_EMAIL_LENGTH)
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Transient
  public boolean getValid() {
    return true;
  }

  public Long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(Long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public String getLastUpdater() {
    return lastUpdater;
  }

  public void setLastUpdater(String lastUpdater) {
    this.lastUpdater = lastUpdater;
  }

  public Integer getUploadedFilesCount() {
    Integer uploadedFilesCount = this.uploadedFilesCount;
    return uploadedFilesCount == null ? 0 : uploadedFilesCount;
  }

  public void setUploadedFilesCount(Integer uploadedFilesCount) {
    this.uploadedFilesCount = uploadedFilesCount;
  }

  public Integer getUnreadMessagesCount() {
    Integer unreadMessagesCount = this.unreadMessagesCount;
    return unreadMessagesCount == null ? 0 : unreadMessagesCount;
  }

  public void setUnreadMessagesCount(Integer unreadMessagesCount) {
    this.unreadMessagesCount = unreadMessagesCount;
  }

  public Integer getDownloadedContractsCount() {
    Integer downloadedContractsCount = this.downloadedContractsCount;
    return downloadedContractsCount == null ? 0 : downloadedContractsCount;
  }

  public void setDownloadedContractsCount(Integer downloadedContractsCount) {
    this.downloadedContractsCount = downloadedContractsCount;
  }

  public Integer getContractsCount() {
    Integer contractsCount = this.contractsCount;
    return contractsCount == null ? 0 : contractsCount;
  }

  public void setContractsCount(Integer contractsCount) {
    this.contractsCount = contractsCount;
  }

  @Transient
  public boolean isHasDossier() {
    return getUploadedFilesCount() > 0;
  }

  @Transient
  public boolean isHasMessage() {
    return getUnreadMessagesCount() > 0;
  }

  @Transient
  public ContractDownloadState getContractDownloadState() {
    if (contractDownloadState == null)
      contractDownloadState = calcContractDownloadState();

    return contractDownloadState;
  }

  @NotNull
  private ContractDownloadState calcContractDownloadState() {
    Integer contractsCount = getContractsCount();
    if (contractsCount == 0)
      return ContractDownloadState.NO_CONTRACT_ASSIGNED_YET;
    else {
      Integer downloadedContracts = getDownloadedContractsCount();
      if (downloadedContracts == 0)
        return ContractDownloadState.NO_ASSIGNED_CONTRACT_DOWNLOADED;
      else if (contractsCount.equals(downloadedContractsCount))
        return ContractDownloadState.ALL_ASSIGNED_CONTRACTS_DOWNLOADED;
      else
        return ContractDownloadState.ASSIGNED_CONTRACTS_PARTIALLY_DOWNLOADED;
    }
  }

  @Nullable
  @Transient
  public Date getLastUpdateDate() {
    if (lastUpdate != null)
      return new Date(lastUpdate);
    else
      return null;
  }

  @Transient
  public boolean getValidRegistrationNumber() {
    if (getAgrement() != null)
      return VALID_AGREMENT.matcher(getAgrement()).matches();
    else
      return false;
  }

  @Transient
  public abstract EntityReference<? extends AbstractAssociationEntity> getReference();

  @Transient
  public abstract Class<? extends AbstractAssociationEntity> getType();
}
