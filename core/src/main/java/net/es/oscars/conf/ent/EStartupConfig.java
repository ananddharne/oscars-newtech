package net.es.oscars.conf.ent;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;

@Data
@Entity
public class EStartupConfig {
    public EStartupConfig() {

    }

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String name;

    @NonNull
    @Lob
    @Column(length = 65535)
    private String configJson;

}
