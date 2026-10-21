package com.ad03.formacion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ListadoCursos {

	public static void main(String[] args) {

		// ### 1.- Ejercicio ###

		String sql = "SELECT c.id_curso, c.titulo, c.fecha, COUNT(m.dni) AS matriculados, "
				+ "a.capacidad, a.nombre AS aula, a.edificio " + "FROM cursos c "
				+ "JOIN aulas a ON c.id_aula = a.id_aula " + "LEFT JOIN matriculas m ON c.id_curso = m.id_curso "
				+ "GROUP BY c.id_curso, c.titulo, c.fecha, a.capacidad, a.nombre, a.edificio " + "ORDER BY c.fecha";

		try (Connection con = ConexionBD.obtener();
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(sql)) {

			System.out.printf("%-3s %-24s %-11s %-11s %-25s %-12s%n", "ID", "Curso", "Fecha", "Matrículas", "Aula",
					"Edificio");
			System.out.println("-".repeat(90));

			while (rs.next()) {

				int idCurso = rs.getInt("id_curso");
				String titulo = rs.getString("titulo");
				Date fecha = rs.getDate("fecha");
				int matriculados = rs.getInt("matriculados");
				int capacidad = rs.getInt("capacidad");
				String aula = rs.getString("aula");
				String edificio = rs.getString("edificio");

				String matriculasStr = matriculados + "/" + capacidad;

				System.out.printf("%-3d %-24s %-11s %s %-25s %-12s%n", idCurso, titulo, fecha,
						centrar(matriculasStr, 11), aula, edificio);

			}

		} catch (SQLException e) {
			System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
		}
	}

	private static String centrar(String texto, int ancho) {
		int espacios = ancho - texto.length();
		int izquierda = Math.max(espacios / 2, 0);
		int derecha = Math.max(espacios - izquierda, 0);
		return " ".repeat(izquierda) + texto + " ".repeat(derecha);
	}
}
