package dz.ngnex.entity;

import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BasicPrincipalEntity implements DatabaseEntity {
    private static final long serialVersionUID = 4082553957962970139L;

    private Integer id;
    private String name;
    private String password;
    private String creationDate = "21-07-2019";
    private Long lastLogin;
    private Integer tutorials = 0;
    private Integer version = 0;

    protected BasicPrincipalEntity() {
    }

    protected BasicPrincipalEntity(String name, String password) {
        setName(name);
        setPassword(password);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @SuppressWarnings("JpaDataSourceORMInspection") // this id is declared only to get inherited by subclasses
    @Column(name = "principal_id", updatable = false, nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @NotNull
    @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
    @Column(updatable = false, unique = true, nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
    @Column(nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @NotNull
    @Size(max = Constrains.MAX_DATE_LENGTH)
    @Column(updatable = false, nullable = false, length = Constrains.MAX_DATE_LENGTH)
    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

    @NotNull
    @Column(nullable = false)
    public Integer getTutorials() {
        return tutorials;
    }

    public void setTutorials(Integer tutorials) {
        this.tutorials = tutorials;
    }

    @Nullable
    @Transient
    public Date getLastLoginDate() {
        if (lastLogin != null)
            return new Date(lastLogin);
        else
            return null;
    }

    @Transient
    public abstract AccessType getAccessType();

    @Transient
    public abstract EntityReference<? extends BasicPrincipalEntity> getReference();
}
