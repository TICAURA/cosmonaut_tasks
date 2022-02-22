package mx.com.ga.cosmonaut.tasks.scheduler;

import io.micronaut.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;
import mx.com.ga.cosmonaut.common.dto.RespuestaGoogleStorage;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.*;
import mx.com.ga.cosmonaut.common.dto.utilidades.ExtractFilesFromTectelDto;
import mx.com.ga.cosmonaut.common.entity.catalogo.negocio.CatEstatusIdse;
import mx.com.ga.cosmonaut.common.entity.colaborador.NcoBitacoraKardexEstatusIdse;
import mx.com.ga.cosmonaut.common.entity.colaborador.NcoKardexColaborador;
import mx.com.ga.cosmonaut.common.entity.colaborador.NcoKardexEstatusIdse;
import mx.com.ga.cosmonaut.common.exception.ServiceException;
import mx.com.ga.cosmonaut.common.repository.colaborador.NcoBitacoraKardexEstatusIdseRepository;
import mx.com.ga.cosmonaut.common.repository.colaborador.NcoKardexColaboradorRepository;
import mx.com.ga.cosmonaut.common.repository.colaborador.NcoKardexEstatusIdseRepository;
import mx.com.ga.cosmonaut.common.repository.nativo.KardexRepository;
import mx.com.ga.cosmonaut.common.service.GoogleStorageService;
import mx.com.ga.cosmonaut.common.util.Utilidades;
import mx.com.ga.cosmonaut.tasks.service.TectelClientService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Singleton
public class TectelScheduler {

    // TODO EL MENSAJE_EXITOSO/MENSAJE_RECHAZO PODRIA SER REEMPLAZADO POR LAS DISTINTOS CODIGOS DE EXITO DE CADA WS DE TECTEL
    // 0 EXITO WSAfiliaRecepcion, 30 EXITO y 33 RECHAZO WSAfiliaEstado, 40 EXITO WSAfiliaReporteDetallado, 50 EXITO WSMovtosEstado, 60 EXITO WSMovtosReporteDetallado
    private static final String PATH_TECTEL = "tectel/";
    private static final String PDF_EXT = ".pdf";
    private static final String MENSAJE_EXITOSO = "OPERACION REALIZADA EXITOSAMENTE";
    private static final String MENSAJE_RECHAZO = "MOVIMIENTOS RECHAZADOS POR EL IMSS";
    private static final String PATRONAL_LISTO = "3";

    private final String AFILIAESTADO_MOVIMIENTOSPENDIENTES = "31";
    private final String AFILIAESTADO_MOVIMIENTOSRECHAZADOS = "33";
    private final String AFILIAESTADO_MOVIMIENTOSEXITOSOS = "30";
    private final String AFILIAESTADO_MOVIMIENTOSNOENCONTRADOS = "39";


    private final String  AFILIAREPORTEDETALLADO_EXISTOSO = "40";
    private final String  AFILIAREPORTEDETALLADO_ERROR = "49";


    private final String AFILIAMOVESTADO_EXITOSO = "50";
    private final String AFILIAMOVESTADO_ERROR = "59";


    private final String AFILIARMOVREPORTEDETALLADO_EXITODO = "60";
    private final String AFILIARMOVREPORTEDETALLADO_ERROR = "69";


    @Inject
    private TectelClientService tectelClientService;

    @Inject
    private GoogleStorageService googleStorageService;

    @Inject
    private NcoKardexEstatusIdseRepository ncoKardexEstatusIdseRepository;

    @Inject
    private NcoBitacoraKardexEstatusIdseRepository ncoBitacoraKardexEstatusIdseRepository;

    @Inject
    private NcoKardexColaboradorRepository ncoKardexColaboradorRepository;

    @Inject
    private KardexRepository kardexRepository;


    @Scheduled(fixedDelay = "${programador.tectel.cron.fijo}", initialDelay = "${programador.tectel.cron.inicial}")
    void movimientosPendientesImms() {

        log.info("Empieza job: {}", System.nanoTime());

        try {
            // Consultar estados en proceso
            Set<String> procesosPendientes =
                    ncoKardexEstatusIdseRepository.findIdProcesoByEstatusIdseIdEstatusIdseId(1L);

            for (String idProceso : procesosPendientes) {
                AfiliaEstadoRequestDto afiliaEstadoReq = new AfiliaEstadoRequestDto();
                afiliaEstadoReq.setIdProceso(idProceso);
                AfiliaEstadoResponseDto afiliaEstadoRes = tectelClientService.afiliaEstado(afiliaEstadoReq);
                if (afiliaEstadoRes.getEstado().equals(this.AFILIAESTADO_MOVIMIENTOSEXITOSOS)) {


                    String numeroLote = afiliaEstadoRes.getNumeroLote();

                    ReporteDetalladoRequestDto reporteDetalladoReq = new ReporteDetalladoRequestDto();
                    reporteDetalladoReq.setNumeroLote(numeroLote);
                    ReporteDetalladoResponseDto reporteDetalladoRes =
                            tectelClientService.reporteDetallado(reporteDetalladoReq);
                    if (reporteDetalladoRes.getEstado().equals(AFILIAREPORTEDETALLADO_EXISTOSO)) {
                        // TODO PROBAR LOGICA QUE OBTIENE EL ACUSE
                        String acuse = extraerGuardarPdfImss(reporteDetalladoRes.getArchivoPdf(), true);
                        if (acuse != null) {
                           List<NcoKardexEstatusIdse> listaByProceso =  ncoKardexEstatusIdseRepository.findByIdProceso(idProceso);
                            for (NcoKardexEstatusIdse movimientoPendiente
                                    : listaByProceso) {
                                // Actualiza estatus y guarda en bitacora
                                movimientoPendiente.setNumeroLote(numeroLote);
                                movimientoPendiente.setEstatusIdseId(new CatEstatusIdse(2L));
                                movimientoPendiente.setReferenciaAcuse(acuse);
                                guardarKardexIdseAndGenerarBitacora(movimientoPendiente);
                            }
                        }
                    }
                } else if (afiliaEstadoRes.getEstado().equals(AFILIAESTADO_MOVIMIENTOSRECHAZADOS)) {
                    for (NcoKardexEstatusIdse movimientoPendiente
                            : ncoKardexEstatusIdseRepository.findByIdProceso(idProceso)) {
                        // Actualiza estatus y guarda en bitacora
                        movimientoPendiente.setEstatusIdseId(new CatEstatusIdse(4L));
                        guardarKardexIdseAndGenerarBitacora(movimientoPendiente);
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Sucedio algo inesperado: {}", ex.getMessage());
        }
    }


    @Scheduled(fixedDelay = "${programador.tectel.cron.fijo}", initialDelay = "${programador.tectel.cron.inicial}")
    void otrometodo(){

        try{
            Set<String> lotesImssEnviados =
                    ncoKardexEstatusIdseRepository.findNumeroLoteByEstatusIdseIdEstatusIdseId(2L);
            for (String loteimss : lotesImssEnviados) {
                MvtosEstadoRequestDto mvtosEstadoReq = new MvtosEstadoRequestDto();
                mvtosEstadoReq.setNumeroLote(loteimss);
                MvtosEstadoResponseDto mvtosEstadoRes = tectelClientService.mvtosEstado(mvtosEstadoReq);
                // TODO REVISAR EL XML DE RESPUESTA PARA ESTATUS 3 INDICATIVO EXITO EN IMSS
                if (mvtosEstadoRes.getEstado().equals(this.AFILIAMOVESTADO_EXITOSO)
                        && yaHayRespuestaPatronal(mvtosEstadoRes.getArchivo())) {
                    MvtosReporteDetalladoRequestDto mvtosReporteDetalladoReq = new MvtosReporteDetalladoRequestDto();
                    mvtosReporteDetalladoReq.setNumeroLote(loteimss);
                    MvtosReporteDetalladoResponseDto mvtosReporteDetalladoRes =
                            tectelClientService.mvtosReporteDetallado(mvtosReporteDetalladoReq);
                    if (mvtosReporteDetalladoRes.getMensaje().equals(MENSAJE_EXITOSO)) {
                        // Obtiene el pdf respuesta patronal
                        // TODO PROBAR LOGICA QUE OBTIENE EL ACUSE
                        String patronal = extraerGuardarPdfImss(
                                mvtosReporteDetalladoRes.getArchivoImssRespuestaPdf(), false);
                        if (mvtosReporteDetalladoRes.getArchivoImssRespuestaDetalle() != null) {
                            // Lista de movimientos totales respecto al numero de lote
                            List<NcoKardexEstatusIdse> movimientosRechazados = ncoKardexEstatusIdseRepository
                                    .findByNumeroLote(loteimss);
                            // TODO REVISAR LOGICA QUE OBTIENE EL ARCHIVO SOBRE TODO TAG Y ATRIBUTO DEL JSON
                            // Obtiene xml de movimientos y busca operados
                            Document doc = Jsoup.parse(new String(Utilidades.decodeContent(
                                            mvtosReporteDetalladoRes.getArchivoImssRespuestaDetalle())),
                                    "",
                                    Parser.xmlParser());
                            // Busca en el xml del imss los Aceptados
                            for (Element e : doc.select("movimientoOperado")) {
                                String nss = e.getElementsByTag("nss").text();
                                String claveMovimiento = e.getElementsByTag("tipo_mov").text();
                                String nombreAsegurado = e.getElementsByTag("nombreAsegurado").text();


                                // Busca el movimiento en kardex para actualizarlo
                                List<PersonaAfiliadaKardex> listaPersonaAfiliado = this.kardexRepository.getPersonaAfiliadasKardex(nss,nombreAsegurado,loteimss);
                                if (!listaPersonaAfiliado.isEmpty()) {
                                    PersonaAfiliadaKardex persona = listaPersonaAfiliado.get(0);
                                    // Comprueba que el movimiento existe en la tabla de estatus para actualizar
                                    Optional<NcoKardexEstatusIdse> kardexAceptado = ncoKardexEstatusIdseRepository
                                            .findById(Long.valueOf(persona.getKardexColaboradorId()));
                                    if (kardexAceptado.isPresent()) {
                                        NcoKardexEstatusIdse movimientoAceptado = kardexAceptado.get();
                                        movimientoAceptado.setEstatusIdseId(new CatEstatusIdse(3L));
                                        movimientoAceptado.setReferenciaPatronal(patronal);
                                        guardarKardexIdseAndGenerarBitacora(movimientoAceptado);

                                        // Remueve de la lista total los operados para dejar rechazados
                                        movimientosRechazados.removeIf(mov -> mov.getKardexColaboradorId()
                                                .equals(Long.valueOf(persona.getKardexColaboradorId())));
                                    }
                                }
                            }
                            // Rechazados
                            for (NcoKardexEstatusIdse movimientoRechazado : movimientosRechazados) {
                                movimientoRechazado.setEstatusIdseId(new CatEstatusIdse(4L));
                                guardarKardexIdseAndGenerarBitacora(movimientoRechazado);
                            }
                        }
                    }
                }
            }
        }  catch (Exception ex) {
        log.error("Sucedio algo inesperado: {}", ex.getMessage());
    }


    }

    private String extraerGuardarPdfImss(String soapBase64, boolean isAcuse) {
        try {
            List<ExtractFilesFromTectelDto> files;
            if (isAcuse) {
                files = Utilidades.extractFilesBase64FromTectelResponse(soapBase64, "ArchivoReciboDispmag");
            } else {
                files = Utilidades.extractFilesBase64FromTectelResponse(soapBase64, "ArchivoReporteDetallado");
            }

            if (files.size() > 0) {
                return guardarAcuse(files.get(0).getArchivo());
            } else {
                return null;
            }
        } catch (IOException ex) {
            log.error("Error al extraer archivo del SOAP Base64: {}", ex.getMessage());
            return null;
        }
    }

    private String guardarAcuse(byte[] archivo) {
        try {
            String referencia = PATH_TECTEL + Utilidades.generateObjectId() + PDF_EXT;
            RespuestaGoogleStorage respuestaGoogleStorage = googleStorageService.subirArchivo(archivo, referencia);
            if (respuestaGoogleStorage.isRespuesta()) {
                return referencia;
            } else {
                return null;
            }
        } catch (ServiceException ex) {
            log.error("Error al guardar acuse en Google Storage: {}", ex.getMessage());
            return null;
        }
    }

    private void guardarKardexIdseAndGenerarBitacora(NcoKardexEstatusIdse kardexEstatusIdse) {
        kardexEstatusIdse.setFechaEstatus(Utilidades.obtenerFechaSystema());

            ncoKardexEstatusIdseRepository.update(kardexEstatusIdse);

        NcoBitacoraKardexEstatusIdse ncoBitacoraKardexEstatusIdse = new NcoBitacoraKardexEstatusIdse();
        ncoBitacoraKardexEstatusIdse.setKardexColaboradorId(kardexEstatusIdse.getKardexColaboradorId());
        ncoBitacoraKardexEstatusIdse.setIdProceso(kardexEstatusIdse.getIdProceso());
        ncoBitacoraKardexEstatusIdse.setEstatusIdseId(kardexEstatusIdse.getEstatusIdseId().getEstatusIdseId());
        ncoBitacoraKardexEstatusIdse.setFechaEstatus(kardexEstatusIdse.getFechaEstatus());
        if (kardexEstatusIdse.getNumeroLote() != null) {
            ncoBitacoraKardexEstatusIdse.setNumeroLote(kardexEstatusIdse.getNumeroLote());
        }
        if (kardexEstatusIdse.getReferenciaAcuse() != null) {
            ncoBitacoraKardexEstatusIdse.setReferenciaAcuse(kardexEstatusIdse.getReferenciaAcuse());
        }
        if (kardexEstatusIdse.getReferenciaPatronal() != null) {
            ncoBitacoraKardexEstatusIdse.setReferenciaPatronal(kardexEstatusIdse.getReferenciaPatronal());
        }
        ncoBitacoraKardexEstatusIdseRepository.save(ncoBitacoraKardexEstatusIdse);
    }

    private boolean yaHayRespuestaPatronal(String archivoBase64) {
        // Obtiene xml de movimientos y busca estatus
        String estatus = null;
        Document doc = Jsoup.parse(new String(Utilidades.decodeContent(archivoBase64)), "", Parser.xmlParser());
        for (Element e : doc.select("loteStatus")) {
            estatus = e.ownText();
        }
        return estatus != null && estatus.equals(PATRONAL_LISTO);
    }

}