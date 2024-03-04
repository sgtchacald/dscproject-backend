package br.com.dscproject.enums;

public enum Perfis {

    ADMIN("ADMIN"),
    USER("USER");

    private String perfil;

    Perfis(String perfil){
        this.perfil  = perfil;
    }

    public String getRole(){
        return perfil;
    }

}
