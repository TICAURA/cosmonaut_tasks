package mx.com.ga.cosmonaut.tasks.service.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.micronaut.context.annotation.Value;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.AfiliaEstadoRequestDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.AfiliaEstadoResponseDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.MvtosEstadoRequestDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.MvtosEstadoResponseDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.MvtosReporteDetalladoRequestDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.MvtosReporteDetalladoResponseDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.ReporteDetalladoRequestDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.ReporteDetalladoResponseDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.TectelGenericResponseDto;
import mx.com.ga.cosmonaut.common.exception.ServiceException;
import mx.com.ga.cosmonaut.common.util.Constantes;
import mx.com.ga.cosmonaut.common.util.ObjetoMapper;
import mx.com.ga.cosmonaut.tasks.service.TectelClientService;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Objects;

@Singleton
public class TectelClientClientServiceImpl implements TectelClientService {

    @Value("${servicio.imss.host}")
    private String hostImss;

    @Value("${servicio.imss.path}")
    private String rutaImss;

    public AfiliaEstadoResponseDto afiliaEstado(AfiliaEstadoRequestDto peticion) throws ServiceException {
        try {
            peticion.setServicio(Constantes.TECTEL_SRV);

            TectelGenericResponseDto tectelGenericResponseDto =
                    enviarPeticionGCPTectel(Constantes.TECTEL_OP_AFILIA_ESTADO, peticion);
            return ObjetoMapper.map(tectelGenericResponseDto.getOutput(), AfiliaEstadoResponseDto.class);
        } catch (Exception e) {
            throw new ServiceException(Constantes.ERROR_CLASE + this.getClass().getSimpleName()
                    + Constantes.ERROR_METODO  + Constantes.ERROR_EXCEPCION, e);
        }
    }

    public ReporteDetalladoResponseDto reporteDetallado(ReporteDetalladoRequestDto peticion)
            throws ServiceException {
        try {
            peticion.setServicio(Constantes.TECTEL_SRV);

            TectelGenericResponseDto tectelGenericResponseDto =
                    enviarPeticionGCPTectel(Constantes.TECTEL_OP_REPORTE_DETALLADO, peticion);
            return ObjetoMapper.map(tectelGenericResponseDto.getOutput(), ReporteDetalladoResponseDto.class);
        } catch (Exception e) {
            throw new ServiceException(Constantes.ERROR_CLASE + this.getClass().getSimpleName()
                    + Constantes.ERROR_METODO  + Constantes.ERROR_EXCEPCION, e);
        }
    }

    public MvtosEstadoResponseDto mvtosEstado(MvtosEstadoRequestDto peticion)
            throws ServiceException {
        try {
            peticion.setServicio(Constantes.TECTEL_SRV);

            TectelGenericResponseDto tectelGenericResponseDto =
                    enviarPeticionGCPTectel(Constantes.TECTEL_OP_MVTOS_ESTADO, peticion);
            return ObjetoMapper.map(tectelGenericResponseDto.getOutput(), MvtosEstadoResponseDto.class);
        } catch (Exception e) {
            throw new ServiceException(Constantes.ERROR_CLASE + this.getClass().getSimpleName()
                    + Constantes.ERROR_METODO  + Constantes.ERROR_EXCEPCION, e);
        }
    }

    public MvtosReporteDetalladoResponseDto mvtosReporteDetallado(MvtosReporteDetalladoRequestDto peticion)
            throws ServiceException {
        try {
            peticion.setServicio(Constantes.TECTEL_SRV);

            TectelGenericResponseDto tectelGenericResponseDto =
                    enviarPeticionGCPTectel(Constantes.TECTEL_OP_MVTOS_REPORTE_DETALLADO, peticion);
            return ObjetoMapper.map(tectelGenericResponseDto.getOutput(), MvtosReporteDetalladoResponseDto.class);
        } catch (Exception e) {
            throw new ServiceException(Constantes.ERROR_CLASE + this.getClass().getSimpleName()
                    + Constantes.ERROR_METODO  + Constantes.ERROR_EXCEPCION, e);
        }
    }

    private TectelGenericResponseDto enviarPeticionGCPTectel(String operacion, Object peticion)
            throws IOException, ServiceException {
        ObjectMapper mapper = new ObjectMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = RequestBody.create(writer.writeValueAsString(peticion), null);

        Request request = new Request.Builder()
                .url(hostImss + rutaImss + operacion)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            return mapper
                    .readValue(Objects.requireNonNull(response.body()).string(), TectelGenericResponseDto.class);
        } else {
            throw new ServiceException("Servicios GCP/Tectel no arrojaron un 200.");
        }
    }

}
