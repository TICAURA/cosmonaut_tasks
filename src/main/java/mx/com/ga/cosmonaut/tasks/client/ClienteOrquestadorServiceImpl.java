package mx.com.ga.cosmonaut.tasks.client;

import io.micronaut.context.annotation.Value;
import mx.com.ga.cosmonaut.common.exception.ServiceException;
import mx.com.ga.cosmonaut.common.util.Cliente;
import mx.com.ga.cosmonaut.common.util.Constantes;
import okhttp3.*;
import org.json.JSONObject;

public class ClienteOrquestadorServiceImpl implements ClienteOrquestadorService {

    @Value("${servicio.orquestador.host}")
    private String host;

    @Value("${servicio.orquestador.path}")
    private String contexto;

    private static final String CLABE = "?cve=";

    @Override
    public Boolean cliente(Object objetoSolicitud, Integer clabe) throws ServiceException {
        try{
            final MediaType media = MediaType.get("application/json; charset=utf-8");
            OkHttpClient cliente = Cliente.obtenOkHttpCliente();
            cliente.sslSocketFactory();
            JSONObject json = new JSONObject(objetoSolicitud);
            RequestBody cuerpoSolicitud = RequestBody.create(json.toString(),media);
            Request solicitud = new Request.Builder()
                    .url(host + contexto + CLABE + clabe)
                    .post(cuerpoSolicitud)
                    .build();
            Call llamada = cliente.newCall(solicitud);
            Response respuesta = llamada.execute();
            return respuesta.isSuccessful();
        }catch (Exception e){
            throw new ServiceException(Constantes.ERROR_CLASE + this.getClass().getSimpleName()
                    + Constantes.ERROR_METODO + " clienteOrquestador " + Constantes.ERROR_EXCEPCION, e);
        }
    }


}
