package dz.ngnex.entity;

import dz.ngnex.security.ReadAccess;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.faces.context.FacesContext;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = Post2Entity.TABLE, indexes = {
        // this index will work on selects with:
        // "order by access" or "order access, lastUpdate" but not "order by lastUpdate"
        // that's why order is important when declaring indexes
        @Index(columnList = "access,lastUpdate", name = "index_" + Post2Entity.TABLE + "_last_update_date")
})
public class Post2Entity implements DatabaseEntity {
    private static final long serialVersionUID = 7692977591331556298L;

    public static final String TABLE = "post2";
    public static final String ID = "post_id";

    private Integer id;
    private Long lastUpdate;
    private Long created;
    private ReadAccess access = ReadAccess.ANYONE;
    private String author;

    private String thumbnail;

    private String title_en;
    private String title_fr;

    private String summary_en;
    private String summary_fr;

    private String content_en;
    private String content_fr;

    private Set<Picture2InfoEntity> images = new HashSet<>(4);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID, updatable = false, nullable = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @NotNull
    @Column(nullable = false)
    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @NotNull
    @Column(nullable = false)
    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    @Enumerated
    @Column(nullable = false)
    public ReadAccess getAccess() {
        return access;
    }

    public void setAccess(ReadAccess access) {
        this.access = access;
    }

    @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
    @Column(length = Constrains.MAX_IDENTIFIER_SIZE)
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Column(columnDefinition = "TEXT", length = Constrains.MAX_URL_LENGTH)
    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnails) {
        this.thumbnail = thumbnails;
    }

    @Column(length = Constrains.MAX_PHRASE_LENGTH)
    public String getTitle_en() {
        if (title_en != null && !title_en.isEmpty())
            return title_en;
        else
            return title_fr;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    @Column(length = Constrains.MAX_PHRASE_LENGTH)
    public String getTitle_fr() {
        return title_fr;
    }

    public void setTitle_fr(String title_fr) {
        this.title_fr = title_fr;
    }

    @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
    public String getSummary_en() {
        if (summary_en != null && !summary_en.isEmpty())
            return summary_en;
        else
            return summary_fr;
    }

    public void setSummary_en(String summary_en) {
        this.summary_en = summary_en;
    }

    @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
    public String getSummary_fr() {
        return summary_fr;
    }

    public void setSummary_fr(String summary_fr) {
        this.summary_fr = summary_fr;
    }

    @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
    public String getContent_en() {
        if (content_en != null && !content_en.isEmpty())
            return content_en;
        else
            return content_fr;
    }

    public void setContent_en(String content_en) {
        this.content_en = content_en;
    }

    @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH)
    public String getContent_fr() {
        return content_fr;
    }

    public void setContent_fr(String content_fr) {
        this.content_fr = content_fr;
    }

    private String currentLanguage() {
        return FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
    }

    @Transient
    public String getTitle() {
        return "fr".equalsIgnoreCase(currentLanguage()) ? getTitle_fr() : getTitle_en();
    }

    @Transient
    public String getSummary() {
        return "fr".equalsIgnoreCase(currentLanguage()) ? getSummary_fr() : getSummary_en();
    }

    @Transient
    public String getContent() {
        return "fr".equalsIgnoreCase(currentLanguage()) ? getContent_fr() : getContent_en();
    }

    @OneToMany
    @NotNull
    public Set<Picture2InfoEntity> getImages() {
        return images;
    }

    public void setImages(Set<Picture2InfoEntity> images) {
        this.images = images;
    }

    @Transient
    public void addPicture(@NotNull Picture2InfoEntity picture) {
        getImages().add(picture);
    }

    @Override
    public String toString() {
        return "ArticleEntity{" +
                "id=" + getId() +
                ", author='" + getAuthor() + '\'' +
                ", title_en='" + getTitle_en() + '\'' +
                '}';
    }

    @Override
    public final boolean equals(Object other) {
        return isEqualsById(other, ArticleEntity.class);
    }

    @Override
    public final int hashCode() {
        return getIdHashcode();
    }
}
