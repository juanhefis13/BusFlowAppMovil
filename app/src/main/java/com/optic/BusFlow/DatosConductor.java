package com.optic.BusFlow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatosConductor {
    private String company;
    private String email;
    private String emergencyPhone;
    private String fullName;
    private String horario_fin;
    private String horario_inicio;
    private String patentNumber;
    private String phone;
    private Long timestamp;
    private String ruta;
    private String ubicacion_destino;
    private String ubicacion_inicio;
    private String tipoReporte;
    private String fechaHoraReporte;

    // Constructor vacío necesario para Firebase
    public DatosConductor() {
    }

    // Constructor con todos los parámetros
    public DatosConductor(String company, String email, String emergencyPhone, String fullName,
                          String horario_fin, String horario_inicio, String patentNumber,
                          String phone, String ruta, String ubicacion_destino, String ubicacion_inicio) {
        this.company = company;
        this.email = email;
        this.emergencyPhone = emergencyPhone;
        this.fullName = fullName;
        this.horario_fin = horario_fin;
        this.horario_inicio = horario_inicio;
        this.patentNumber = patentNumber;
        this.phone = phone;
        this.ruta = ruta;
        this.ubicacion_destino = ubicacion_destino;
        this.ubicacion_inicio = ubicacion_inicio;
    }

    // Getters y setters para cada campo
    // Por ejemplo:
    // Getters y setters
    public String getFechaHoraReporte() {
        return fechaHoraReporte;
    }

    public void setFechaHoraReporte(String fechaHoraReporte) {
        this.fechaHoraReporte = fechaHoraReporte;
    }
    public Long getTimestamp() {
        return timestamp;
    }


    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    // Método para actualizar la fecha y hora del reporte al momento actual
    public void actualizarFechaHoraReporte() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        this.fechaHoraReporte = dateFormat.format(new Date()); // Asigna la fecha y hora actual formateada
    }
    public String getCompany() {
        return company;
    }

    public String getTipoReporte() {
        return tipoReporte;
    }
    public void setTipoReporte(String tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHorario_fin() {
        return horario_fin;
    }

    public void setHorario_fin(String horario_fin) {
        this.horario_fin = horario_fin;
    }

    public String getHorario_inicio() {
        return horario_inicio;
    }

    public void setHorario_inicio(String horario_inicio) {
        this.horario_inicio = horario_inicio;
    }

    public String getPatentNumber() {
        return patentNumber;
    }

    public void setPatentNumber(String patentNumber) {
        this.patentNumber = patentNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getUbicacion_destino() {
        return ubicacion_destino;
    }

    public void setUbicacion_destino(String ubicacion_destino) {
        this.ubicacion_destino = ubicacion_destino;
    }

    public String getUbicacion_inicio() {
        return ubicacion_inicio;
    }

    public void setUbicacion_inicio(String ubicacion_inicio) {
        this.ubicacion_inicio = ubicacion_inicio;
    }
}

