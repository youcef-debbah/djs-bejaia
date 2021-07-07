package dz.ngnex.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "extra")
public class ExtraEntity implements DatabaseEntity {
    private static final long serialVersionUID = -7640987079157085391L;

    private Integer id;
    private String value;
    private Integer category;
    private Integer version = 0;

    public ExtraEntity() {
    }

    public ExtraEntity(Integer id, String value, Integer category) {
        this.id = id;
        this.value = value;
        this.category = category;
    }

    @Id
    @Column(name = "extra_id", updatable = false, nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @NotNull
    @Size(max = Constrains.MAX_PHRASE_LENGTH)
    @Column(nullable = false, length = Constrains.MAX_PHRASE_LENGTH)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public final boolean equals(Object other) {
        return isEqualsById(other, SectionTemplateEntity.class);
    }

    @Override
    public final int hashCode() {
        return getIdHashcode();
    }

    @Override
    public String toString() {
        return "ExtraEntity{" +
                "id=" + id +
                ", value='" + value + '\'' +
                ", category=" + category +
                ", version=" + version +
                '}';
    }

    public static String getValue(ExtraEntity entity) {
        return entity != null? entity.value : null;
    }
}
