package dz.ngnex.entity;

import dz.ngnex.security.ReadAccess;
import dz.ngnex.testkit.TestConfig;
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
@Table(name = Post1Entity.TABLE, indexes = {
        // this index will work on selects with:
        // "order by access" or "order access, lastUpdate" but not "order by lastUpdate"
        // that's why order is important when declaring indexes
        @Index(columnList = "access,lastUpdate", name = "index_" + Post1Entity.TABLE + "_last_update_date")
})
public class Post1Entity implements DatabaseEntity {
    private static final long serialVersionUID = 7692977591331556298L;

    public static final String TABLE = "post1";
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

    private Set<Picture1InfoEntity> images = new HashSet<>(2);

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

    // I don't like cascades in general, but with @manyToMany never use CascadeType.REMOVE
    // eager fetch is a bad idea too, but in some rare cases it make sense to use it,
    // instead of eager association call a query with join fetch, something like: em.createQuery("SELECT a FROM PostEntity a JOIN FETCH a.pictures WHERE a.id ...
    // if you don't want to use a join fetch then use FetchType.LAZY (it's the default for ManyToMany anyways)
    // with the right FetchMode (annotation bellow) after you get an entity use:
    // Hibernate.initialize(post.getImages()) or post.getImages().something() to trigger the actual fetch
    @ManyToMany(fetch = FetchType.LAZY)

    // this is a hibernate annotation that config what you want
    // FetchMode.SELECT is the default one that make hibernate issue a select for every row, this can work only you are are sure
    // that you have only 0/1/2 parent entity with huge number of children, like a 3 blogs each with 300+ comments  
    // FetchMode.JOIN is what you wanted, a join fetch in the same query but it work only when you have a oneToMany with JoinColumn (yes you need to add JoinColumn annotation
    // to tell hibernate which column to join with) but in ManyToMany there is not such column (there is a table)
    // so instead of doing two joins (on asso table then on child table) in a single query it will work as the first one (FetchType.SELECT)
    // FetchMode.SUBSELECT is a cool one that I really like, the one I told you about you first fetch only parent entities
    // then when you call getPictures().something() on ANY owner entity ALL children are selected in a second (and single) select then affected when needed
    // to the right parent)
    // note that FetchMode don't work with EAGER fetch since when since EAGER fetch will always use FetchMode.SELECT
    @Fetch(FetchMode.SELECT)
    // - when using SUBSELECT with an owner entity that have many children selection all children of all parents will result
    // in simple query with huge result set that make it slow and take tons of memory, of course you can limit the count of owners in the first query but you
    // can't limit the count for the second query (children select), and magin that each parent have a huge number of childen,
    // that's what BatchSize will help us with, it tells hibernate how many children to fetch with a single select
    // each time, by using @FetchFetch(FetchMode.JOIN) + @BatchSize(...)
    // if we have 12 post and each post have 2 pics then @FetchFetch(FetchMode.JOIN) + @BatchSize(size = 10) will result in 4 selects: select all posts, select 10 pics, select 10 pics, select 4 pics
    // SUBSELECT will always fetch all childs regardless of BatchSize
    // SELECT works like JOIN with BatchSize except one optimization the first time you access the parent entties
    // if you only access first entities in the first batch (say getResultList().get(0) to getResultList(9) for example)
    // then only the first batch will get selected, if you try to access the first parent/owner entity in the second batch
    // the rest of batches will get fetch ALL AT ONCE, this may be helpful to keep in mind
    // so in the end I think that FetchMode.JOIN  BatchSize is the best
    // but to prove my point I created a second copy of this class and it's child (Picture entity)
    // the second copy is configured without BatchSize then I will use an HQL to select with join fetch
    // the Posts and their pictures in a single pictures, then I will compare the performance of
    // your single-big-query idea and my multi-small-queries, I'm talking from my experience but
    // I still didn't run the test at the time of writing this...I will include an HTML page with
    // the benchmark result
//    @BatchSize(size = TestConfig.BATCH_SIZE)
    @JoinTable(name = "asso_table_post1_image", // I don't have a single asso table in my app but this is how you name it in hibernate (dont use the default name it's a bad habit)
            joinColumns = @JoinColumn(name = ID),
            inverseJoinColumns = @JoinColumn(name = BinaryFileEntity.FILE_ID)
    )

    @NotNull
    public Set<Picture1InfoEntity> getImages() {
        return images;
    }

    public void setImages(Set<Picture1InfoEntity> images) {
        this.images = images;
    }

    @Transient
    public void addPicture(@NotNull Picture1InfoEntity picture) {
        picture.getPosts().add(this);
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
