package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.resv.ResourceType;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReservedResourceE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @ElementCollection
    private List<String> urns;

    @NonNull
    private ResourceType resourceType;

    private Integer resource;

    private Instant beginning;

    private Instant ending;


}
