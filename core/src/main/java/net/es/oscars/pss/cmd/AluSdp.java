package net.es.oscars.pss.cmd;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;


@Data
@Builder
public class AluSdp {

    @NonNull
    private Integer sdpId;

    @NonNull
    private String lspName;

    @NonNull
    private String description;

    @NonNull
    private String farEnd;
}
