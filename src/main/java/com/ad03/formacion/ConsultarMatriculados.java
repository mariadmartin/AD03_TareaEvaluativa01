package com.ad03.formacion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Scanner;

public class ConsultarMatriculados {

	public static void main(String[] args) {

		// Listado de cursos

		String sql = "SELECT id_curso, titulo FROM cursos ORDER BY fecha";

		try (Connection con = ConexionBD.obtener();
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(sql);) {

			while (rs.next()) {

				int idCurso = rs.getInt("id_curso");
				String titulo = rs.getString("titulo");

				System.out.printf(" %-3d.   %-20s%n", idCurso, titulo);

			}

			// Pedir el idCurso por consola
			System.out.print("Introduce el ID del curso: ");
			Scanner sc = new Scanner(System.in);
			int idCurso = sc.nextInt();

			// Comprobamos que el curso exista antes de llamar a la funcion
			String sqlExiste = "SELECT 1 FROM cursos WHERE id_curso = ?";

			try (PreparedStatement psExiste = con.prepareStatement(sqlExiste)) {

				psExiste.setInt(1, idCurso);

				try (ResultSet rsExiste = psExiste.executeQuery()) {

					if (!rsExiste.next()) {
						System.out.println("El curso no existe");
						return;
					}
				}

			} catch (SQLException e) {
				System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
				return;
			}

			// Lamamos a la funcion almacenada
			String sqlFuncion = "{? = call obtener_numero_matriculados(?)}";

			try (CallableStatement cs = con.prepareCall(sqlFuncion)) {

				cs.registerOutParameter(1, Types.INTEGER);
				cs.setInt(2, idCurso);

				cs.execute();

				int resultado = cs.getInt(1);

				System.out.println("Numero de alumnos matriculados: " + resultado);

			} catch (SQLException e) {
				System.out.println("Error SQL: (" + e.getSQLState() + "):" + e.getMessage());
			}

		} catch (SQLException e) {
			System.out.println("Error SQL: (" + e.getSQLState() + "): " + e.getMessage());
		}

	}

}
