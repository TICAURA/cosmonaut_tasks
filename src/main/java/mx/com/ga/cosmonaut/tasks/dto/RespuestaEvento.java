package mx.com.ga.cosmonaut.tasks.dto;

import lombok.Data;

@Data
public class RespuestaEvento {

    private String mensaje;
    private Integer nominaXperiodoId;
    private Integer centroClienteId;
    private Boolean exito;

    public RespuestaEvento(String mensaje, Integer nominaXperiodoId, Integer centroClienteId, boolean exito) {
        this.mensaje = mensaje;
        this.nominaXperiodoId = nominaXperiodoId;
        this.centroClienteId = centroClienteId;
        this.exito = exito;
    }

}
