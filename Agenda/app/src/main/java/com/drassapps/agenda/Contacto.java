package com.drassapps.agenda;

// Clase que permite gestionar el Adapter de contactos, es decir, inicializa unos valores que se
// le pasaran a la lista

public class Contacto {
    private int id;
    private String nombre, direccion, movil, mail;

    public Contacto(int id, String n, String d, String m, String mm) {
        this.id = id;
        this.nombre = n;
        this.direccion = d;
        this.movil = m;
        this.mail = mm;
    }

    public int getID() {
        return id;
    }
    public void setID(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String n) {
        this.nombre = n;
    }

    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String d) {
        this.direccion = d;
    }

    public String getMovil() {
        return movil;
    }
    public void setMovil(String m) {
        this.movil= m;
    }

    public String getMail() {
        return mail;
    }
    public void setMail(String m) {
        this.mail = m;
    }
}
