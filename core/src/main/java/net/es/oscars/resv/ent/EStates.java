package net.es.oscars.resv.ent;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class EStates {

    private String resv;

    private String prov;

    private String oper;

    public EStates() {

    }
}