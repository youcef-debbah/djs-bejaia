package dz.ngnex.entity;

import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author youcef debbah
 */
@Entity
@Table(name = Picture2InfoEntity.TABLE, indexes = {
        @Index(columnList = "uploadTime", name = "index_" + Picture2InfoEntity.TABLE + "_upload_time"),
        @Index(columnList = "type,uploadTime", name = "index_" + Picture2InfoEntity.TABLE + "_type_and_upload_time")
})
public class Picture2InfoEntity extends BinaryFileEntity {
    private static final long serialVersionUID = 7909145091763030038L;
    public static final String TABLE = "picture2";

    private ImageType type;

    protected Picture2InfoEntity() {
    }

    public Picture2InfoEntity(String contentType, String name, String uploader) {
        super(contentType, name, uploader);
    }

    @NotNull
    @Enumerated
    public ImageType getType() {
        if (type == null)
            return ImageType.GENERAL_PURPOSE;
        else
            return type;
    }

    public void setType(ImageType type) {
        this.type = type;
    }

    @Override
    public final boolean equals(Object other) {
        return isEqualsById(other, ImageInfoEntity.class);
    }

    @Override
    public final int hashCode() {
        return getIdHashcode();
    }
}
