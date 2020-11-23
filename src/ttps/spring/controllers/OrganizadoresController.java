package ttps.spring.controllers;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ttps.spring.config.PersistenceConfig;
import ttps.spring.interfacesDAO.FoodTruckerDAO;
import ttps.spring.interfacesDAO.OrganizadorEventosDAO;
import ttps.spring.model.FoodTrucker;
import ttps.spring.model.OrganizadorEventos;

@RestController
@RequestMapping(value = "/Organizador", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganizadoresController {
	
	@Autowired
	OrganizadorEventosDAO organizadorDAO;
	
	private void onStartup() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(PersistenceConfig.class);
		ctx.scan("ttps.spring.clasesDAO");
		ctx.refresh();
		organizadorDAO = ctx.getBean(OrganizadorEventosDAO.class);
		ctx.close();
	}
	
	
	@GetMapping("/listAllOrganizadores")
	public ResponseEntity<List<OrganizadorEventos>> listAllOrganizadores(){
		List<OrganizadorEventos> organizadores = organizadorDAO.recuperarTodos("email");
		if (organizadores.isEmpty()) {
			return new ResponseEntity<List<OrganizadorEventos>>(HttpStatus.NO_CONTENT);
		}
		Hibernate.initialize(organizadores);
		System.out.println(Hibernate.isInitialized(organizadores));
		return new ResponseEntity<List<OrganizadorEventos>>(organizadores,HttpStatus.OK);
	}
	
	@PostMapping("/createUser")
	public ResponseEntity<OrganizadorEventos> createUser(@RequestBody OrganizadorEventos organizador){
		if (organizadorDAO.ConEmail(organizador.getEmail()) == null) {
			organizadorDAO.persistir(organizador);
			return new ResponseEntity<OrganizadorEventos>(organizador,HttpStatus.CREATED);
		}
		return new ResponseEntity<OrganizadorEventos>(organizador,HttpStatus.CONFLICT);
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestHeader String email, @RequestHeader String contrasenia){
		if (organizadorDAO.ConEmail(email) != null) {
			OrganizadorEventos organizador = organizadorDAO.autenticar(email, contrasenia);
			if (organizador != null) {
				HttpHeaders header = new HttpHeaders();
				header.set("token", organizador.getIdUsuario()+"123456");
				return new ResponseEntity<String>(header,HttpStatus.OK);
			}
			return new ResponseEntity<String>("Contrase�a incorrecta",HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<String>("Email incorrecto",HttpStatus.NO_CONTENT);
	}
}