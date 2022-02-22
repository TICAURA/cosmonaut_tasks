package mx.com.ga.cosmonaut.tasks.service;

import mx.com.ga.cosmonaut.common.dto.imss.tectel.AfiliaEstadoRequestDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.AfiliaEstadoResponseDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.MvtosEstadoRequestDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.MvtosEstadoResponseDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.MvtosReporteDetalladoRequestDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.MvtosReporteDetalladoResponseDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.ReporteDetalladoRequestDto;
import mx.com.ga.cosmonaut.common.dto.imss.tectel.ReporteDetalladoResponseDto;
import mx.com.ga.cosmonaut.common.exception.ServiceException;

public interface TectelClientService {

    AfiliaEstadoResponseDto afiliaEstado(AfiliaEstadoRequestDto peticion) throws ServiceException;

    ReporteDetalladoResponseDto reporteDetallado(ReporteDetalladoRequestDto peticion) throws ServiceException;

    MvtosEstadoResponseDto mvtosEstado(MvtosEstadoRequestDto peticion) throws ServiceException;

    MvtosReporteDetalladoResponseDto mvtosReporteDetallado(MvtosReporteDetalladoRequestDto peticion) throws ServiceException;

}
