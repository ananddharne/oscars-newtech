package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESpecification {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private Integer version;

    @NonNull
    @Column(unique = true)
    private String specificationId;

    @NonNull
    private Date submitted;

    @NonNull
    private Date notBefore;

    @NonNull
    private Date notAfter;

    @NonNull
    private Long durationMinutes;

    @NonNull
    private String username;

    @OneToOne
    private EBlueprint blueprint;


}
