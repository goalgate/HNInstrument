package com.hninstrument.Tools;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.ByteArrayOutputStream;

/**
 * Created by zbsz on 2017/9/21.
 */
@Entity
public class UnUploadPackage {

    @Id(autoincrement = true)
    private Long id;

    private String uri;

    private byte[] bout;

    @Generated(hash = 1412368010)
    public UnUploadPackage(Long id, String uri, byte[] bout) {
        this.id = id;
        this.uri = uri;
        this.bout = bout;
    }

    @Generated(hash = 1137104450)
    public UnUploadPackage() {
    }


    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUri() {
        return this.uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public byte[] getBout() {
        return this.bout;
    }
    public void setBout(byte[] bout) {
        this.bout = bout;
    }

}
