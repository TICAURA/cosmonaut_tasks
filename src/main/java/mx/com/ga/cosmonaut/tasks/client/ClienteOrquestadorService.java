package mx.com.ga.cosmonaut.tasks.client;

import mx.com.ga.cosmonaut.common.exception.ServiceException;
import mx.com.ga.cosmonaut.orquestador.dto.respuesta.NominaRespuesta;

public interface ClienteOrquestadorService {

    Boolean cliente(Object objetoSolicitud, Integer clabe) throws ServiceException;

}
