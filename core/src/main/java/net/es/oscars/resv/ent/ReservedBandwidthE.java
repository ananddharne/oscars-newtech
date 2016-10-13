package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReservedBandwidthE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String urn;

    //private Integer bandwidth;

    private Integer inBandwidth;

    private Integer egBandwidth;


    private Instant beginning;

    private Instant ending;


}
