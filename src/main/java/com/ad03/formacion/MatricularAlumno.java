package com.ad03.formacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class MatricularAlumno {

	public static void main(String[] args) {

		// ### 3.- Ejercicio ###

		// pedir por consola e DNI
		System.out.print("Introduce el DNI del alumno: ");
		Scanner sc = new Scanner(System.in);
		String dni = sc.nextLine();

		// validar que el dni tenga 8 digitos
		if (!dni.matches("\\d{8}[A-Za-z]")) {
			System.out.println("DNI no válido");
			return;
		}
		// consultamos el nombre del alumno con el DNI
		String sql = "SELECT nombre " + "FROM alumnos " + "WHERE dni = ?";

		try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql);) {

			ps.setString(1, dni);

			String nombreAlumno;
			boolean alumnoNuevo;

			try (ResultSet rs = ps.executeQuery()) {

				// si alumno es nuevo - no devuelve filas
				if (!rs.next()) {
					System.out.println("Alumno no existe");
					System.out.print("Introduce nombre nuevo alumno: ");
					nombreAlumno = sc.nextLine();
					alumnoNuevo = true;

				} else {
					nombreAlumno = rs.getString("nombre");
					alumnoNuevo = false;
					System.out.println(nombreAlumno);
				}

				// mostramos el listado de cursos
				String sql2 = "SELECT c.id_curso, c.titulo, a.capacidad - COUNT(m.dni) AS plazas_libres\r\n"
						+ "FROM cursos c\r\n" + "JOIN aulas a ON c.id_aula = a.id_aula\r\n"
						+ "LEFT JOIN matriculas m ON c.id_curso = m.id_curso\r\n"
						+ "GROUP BY c.id_curso, c.titulo, a.capacidad\r\n" + "ORDER BY c.id_curso";

				try (Statement st = con.createStatement(); ResultSet rs2 = st.executeQuery(sql2)) {

					System.out.println("Cursos disponibles: ");

					while (rs2.next()) {

						int id = rs2.getInt("id_curso");
						String titulo = rs2.getString("titulo");
						int plazasLibres = rs2.getInt("plazas_libres");

						System.out.printf("%-1d. %-24s    plazas libres: %-3d%n", id, titulo, plazasLibres);

					}

				} catch (SQLException e) {
					System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
				}

				// ### FASE A: comprobaciones (curso existe, tiene plazas, no esta ya
				// matriculado) ###
				// Curso en el que vamos a matricularnos
				System.out.print("Introduce el ID del curso: ");
				int idCurso;

				try {
					idCurso = Integer.parseInt(sc.nextLine());
				} catch (NumberFormatException e) {
					System.out.println("ID de curso no valido");
					return;
				}

				// comprobamos que el curso existe y cuantas plazas libres tiene
				String sqlPlazas = "SELECT c.titulo, a.capacidad - COUNT(m.dni) AS plazas_libres " + "FROM cursos c "
						+ "JOIN aulas a ON c.id_aula = a.id_aula "
						+ "LEFT JOIN matriculas m ON c.id_curso = m.id_curso " + "WHERE c.id_curso = ? "
						+ "GROUP BY c.titulo, a.capacidad";

				int plazasLibres;
				String tituloCurso;

				try (PreparedStatement psPlazas = con.prepareStatement(sqlPlazas)) {

					psPlazas.setInt(1, idCurso);

					try (ResultSet rsPlazas = psPlazas.executeQuery()) {

						if (!rsPlazas.next()) {
							System.out.println("El curso no existe");
							return;
						}
						plazasLibres = rsPlazas.getInt("plazas_libres");
						tituloCurso = rsPlazas.getString("titulo");

					}

				} catch (SQLException e) {
					System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
					return;
				}

				if (plazasLibres <= 0) {
					System.out.println("No quedan plazas libres en ese curso");
					return;
				}

				// comprobamos que el alumno no este ya matriculado en ese curso
				String sqlYaMatriculado = "SELECT 1 FROM matriculas WHERE dni = ? AND id_curso = ?";

				try (PreparedStatement psYaMatriculado = con.prepareStatement(sqlYaMatriculado)) {

					psYaMatriculado.setString(1, dni);
					psYaMatriculado.setInt(2, idCurso);

					try (ResultSet rsYaMatriculado = psYaMatriculado.executeQuery()) {

						if (rsYaMatriculado.next()) {
							System.out.println("El alumno ya esta matriculado en ese curso");
							return;
						}

					}

				} catch (SQLException e) {
					System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
					return;
				}

				// ### FASE B: transaccion (alta del alumno si es nuevo + matricula) ###
				try {

					con.setAutoCommit(false);

					if (alumnoNuevo) {
						String sqlAltaAlumno = "INSERT INTO alumnos (dni, nombre) VALUES (?, ?)";

						try (PreparedStatement psAltaAlumno = con.prepareStatement(sqlAltaAlumno)) {
							psAltaAlumno.setString(1, dni);
							psAltaAlumno.setString(2, nombreAlumno);
							psAltaAlumno.executeUpdate();
						}
					}

					String sqlMatricula = "INSERT INTO matriculas (dni, id_curso, fecha_matricula) VALUES (?, ?, CURDATE())";

					try (PreparedStatement psMatricula = con.prepareStatement(sqlMatricula)) {
						psMatricula.setString(1, dni);
						psMatricula.setInt(2, idCurso);
						psMatricula.executeUpdate();
					}

					con.commit();
					System.out.println("Matricula registrada para " + nombreAlumno + " en " + tituloCurso);

				} catch (SQLException e) {

					try {
						con.rollback();
					} catch (SQLException ex) {
						System.out.println("Error al deshacer la transaccion: " + ex.getMessage());
					}

					System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
				}

			} catch (SQLException e) {
				System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
			}

		} catch (SQLException e) {
			System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
		}

	}

}
