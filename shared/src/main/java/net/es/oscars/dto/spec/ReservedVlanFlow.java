package net.es.oscars.dto.spec;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservedVlanFlow {
    private Long id;


    private Set<ReservedVlanJunction> junctions;

    private Set<ReservedEthPipe> ethPipes;

    private Set<ReservedMplsPipe> mplsPipes;


}