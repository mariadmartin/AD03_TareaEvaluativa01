package com.ad03.formacion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {

	private static final String URL;
	private static final String USUARIO;
	private static final String PASSWORD;

	static {
		Properties props = new Properties();
		try (InputStream in = ConexionBD.class.getResourceAsStream("/db.properties")) {
			if (in == null) {
				throw new RuntimeException(
						"No se encuentra db.properties en src/main/resources (copia db.properties.example y rellena tus credenciales)");
			}
			props.load(in);
		} catch (IOException e) {
			throw new RuntimeException("Error al leer db.properties", e);
		}

		URL = props.getProperty("url");
		USUARIO = props.getProperty("usuario");
		PASSWORD = props.getProperty("password");
	}

	private ConexionBD() {
	}

	public static Connection obtener() throws SQLException {
		return DriverManager.getConnection(URL, USUARIO, PASSWORD);
	}
}
