package mx.com.ga.cosmonaut.tasks;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.*;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

import javax.inject.Singleton;
import java.util.TimeZone;

@OpenAPIDefinition(
        info = @Info(
                title = "${cosmonaut.aplicacion.nombre}",
                version = "${cosmonaut.aplicacion.version}",
                description = "${cosmonaut.aplicacion.descripcion}",
                license = @License(name = "${cosmonaut.aplicacion.license}", url = "${cosmonaut.aplicacion.license.url}"),
                contact = @Contact(url = "${cosmonaut.aplicacion.contact.url}",
                        name = "${cosmonaut.aplicacion.contacto.nombre}", email = "${cosmonaut.aplicacion.contacto.email}")
        ),
        servers = {
                @Server(url = "${cosmonaut.servidor.desarrollo}", description = "${cosmonaut.servidor.desarrollo.descripcion}"),
                @Server(url = "${cosmonaut.servidor.local}", description = "${cosmonaut.servidor.local.descripcion}")
        }
)
@Introspected(packages = "mx.com.ga.cosmonaut.commons",
        includedAnnotations = {MappedEntity.class, Id.class, MappedProperty.class,
                GeneratedValue.class, Introspected.class, AutoPopulated.class, Singleton.class,
                EmbeddedId.class, Embeddable.class} )
public class Application {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Micronaut.run(Application.class, args);
    }
}
