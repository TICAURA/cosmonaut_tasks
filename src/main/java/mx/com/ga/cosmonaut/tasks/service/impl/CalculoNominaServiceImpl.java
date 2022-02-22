package mx.com.ga.cosmonaut.tasks.service.impl;

import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.context.scope.ThreadLocal;
import lombok.SneakyThrows;
import mx.com.ga.cosmonaut.common.dto.RespuestaGenerica;
import mx.com.ga.cosmonaut.common.entity.calculo.NcrNominaXperiodo;
import mx.com.ga.cosmonaut.common.entity.calculo.NcrProcesoNomina;
import mx.com.ga.cosmonaut.common.entity.catalogo.negocio.CatEstadoProcesoNomina;
import mx.com.ga.cosmonaut.common.exception.ServiceException;
import mx.com.ga.cosmonaut.common.repository.calculo.NcrNominaXperiodoRepository;
import mx.com.ga.cosmonaut.common.repository.calculo.NcrProcesoNominaRepository;
import mx.com.ga.cosmonaut.common.util.Constantes;
import mx.com.ga.cosmonaut.orquestador.service.CalculaNominaLiquidacionService;
import mx.com.ga.cosmonaut.orquestador.service.CalculaNominaPtuService;
import mx.com.ga.cosmonaut.orquestador.service.CalculoNominaAguinaldoService;
import mx.com.ga.cosmonaut.orquestador.service.CalculoNominaOrdinariaServices;
import mx.com.ga.cosmonaut.tasks.client.ClienteOrquestadorService;
import mx.com.ga.cosmonaut.tasks.dto.NominaSolicitud;
import mx.com.ga.cosmonaut.tasks.dto.RespuestaEvento;
import mx.com.ga.cosmonaut.tasks.service.CalculoNominaService;
import mx.com.ga.cosmonaut.tasks.util.Eventos;

import javax.inject.Inject;

@ThreadLocal
public class CalculoNominaServiceImpl implements CalculoNominaService {

    @Inject
    private NcrProcesoNominaRepository ncrProcesoNominaRepository;

    @Inject
    private NcrNominaXperiodoRepository ncrNominaXperiodoRepository;

    @Inject
    private CalculoNominaOrdinariaServices calculoNominaOrdinariaServices;

    @Inject
    private CalculoNominaAguinaldoService calculoNominaAguinaldoService;

    @Inject
    private CalculaNominaLiquidacionService calculaNominaLiquidacionService;

    @Inject
    private CalculaNominaPtuService calculaNominaPtuService;

    @Inject
    private ClienteOrquestadorService clienteOrquestadorService;

    @Value("${programador.nomina.hilos}")
    private Integer hilos;

    @Override
    @SneakyThrows
    public void iniciar(NcrProcesoNomina procesoNomina) {
        try {
            /**long contador = ncrProcesoNominaRepository.countByEstadoProcesoNominaIdEstadoProcesoNominaId(2);
            NcrNominaXperiodo nomina = ncrNominaXperiodoRepository.
                    findById(procesoNomina.getNominaXperiodoId().getNominaXperiodoId())
                    .orElseThrow(() -> new ServiceException(Constantes.ERROR_OBTENER_NOMINA));*/

            procesoNomina.setEstadoProcesoNominaId(new CatEstadoProcesoNomina());
            procesoNomina.getEstadoProcesoNominaId().setEstadoProcesoNominaId(2);
            ncrProcesoNominaRepository.update(procesoNomina);

            NominaSolicitud nominaSolicitud = new NominaSolicitud();
            nominaSolicitud.setNominaXperiodoId(procesoNomina.getNominaXperiodoId().getNominaXperiodoId());
            clienteOrquestadorService.cliente(nominaSolicitud, 16);
            /**RespuestaGenerica respuesta;
            switch (nomina.getTipoNominaId()){
                case 1:
                    respuesta = calculoNominaOrdinariaServices.reCalculoNomina(procesoNomina.getNominaXperiodoId().getNominaXperiodoId());
                    this.actualizaProcesoNomina(respuesta,procesoNomina,nomina);
                    break;
                case 2:
                    respuesta = calculoNominaAguinaldoService.reCalculoNomina(procesoNomina.getNominaXperiodoId().getNominaXperiodoId());
                    this.actualizaProcesoNomina(respuesta,procesoNomina,nomina);
                    break;
                case 3:
                    respuesta = calculaNominaLiquidacionService.reCalculoNomina(procesoNomina.getNominaXperiodoId().getNominaXperiodoId());
                    this.actualizaProcesoNomina(respuesta,procesoNomina,nomina);
                    break;
                case 5:
                    respuesta = calculaNominaPtuService.reCalculoNomina(procesoNomina.getNominaXperiodoId().getNominaXperiodoId());
                    this.actualizaProcesoNomina(respuesta,procesoNomina,nomina);
                    break;
                default:
                    new ServiceException("Tipo de nomina no desarrollado");
                    break;
            }*/
        }catch (Exception e){
            procesoNomina.setEstadoProcesoNominaId(new CatEstadoProcesoNomina());
            procesoNomina.getEstadoProcesoNominaId().setEstadoProcesoNominaId(4);
            procesoNomina.setObservaciones(Constantes.ERROR);
            ncrProcesoNominaRepository.update(procesoNomina);
            throw new ServiceException(Constantes.ERROR_CLASE + this.getClass().getSimpleName()
                    + Constantes.ERROR_METODO + " iniciar " + Constantes.ERROR_EXCEPCION, e);
        }
    }

    /** private void actualizaProcesoNomina(RespuestaGenerica respuesta,NcrProcesoNomina procesoNomina,NcrNominaXperiodo nomina){
        if (respuesta.isResultado()){
            procesoNomina.setEstadoProcesoNominaId(new CatEstadoProcesoNomina());
            procesoNomina.getEstadoProcesoNominaId().setEstadoProcesoNominaId(3);
        }else {
            procesoNomina.setEstadoProcesoNominaId(new CatEstadoProcesoNomina());
            procesoNomina.getEstadoProcesoNominaId().setEstadoProcesoNominaId(4);
        }
        procesoNomina.setObservaciones(respuesta.getMensaje());
        ncrProcesoNominaRepository.update(procesoNomina);*/
        /**Eventos.enviar(new RespuestaEvento(nomina.getNombreNomina() + "\n" + respuesta.getMensaje(),nomina.getNominaXperiodoId(),nomina.getCentrocClienteId(), respuesta.isResultado()));*/
    /**}*/

}
