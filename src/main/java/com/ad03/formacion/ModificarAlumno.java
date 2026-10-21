package com.ad03.formacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ModificarAlumno {

	public static void main(String[] args) {

		// ### 2.- Ejercicio ###

		// pedir por consola e DNI
		System.out.print("Introduce el DNI del alumno");
		Scanner sc = new Scanner(System.in);
		String dni = sc.nextLine();

		String sql2 = "SELECT nombre " + "FROM alumnos " + "WHERE dni = ?";

		// consultamos el nombre actual del DNI que nos pasan
		try (Connection con = ConexionBD.obtener(); PreparedStatement ps = con.prepareStatement(sql2);) {

			ps.setString(1, dni);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next()) {
					System.out.println("Alumno no encontrado");
					return;
				}

				String nombreAlumno = rs.getString("nombre");
				System.out.println(nombreAlumno);

				System.out.print("Nuevo nombre (deja en blanco para no modificar");
				String nuevoNombre = sc.nextLine();

				if (!nuevoNombre.isEmpty()) {

					String sqlUpdate = "UPDATE alumnos SET nombre = ? WHERE dni = ? ";

					try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {

						psUpdate.setString(1, nuevoNombre);
						psUpdate.setString(2, dni);

						int filas = psUpdate.executeUpdate();

						System.out.println("Nombre actualizado correctamente");

					} catch (SQLException e) {
						System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
					}

				} else {
					System.out.println("El nombre está en blanco, no se ha podido modificar");
				}

			} catch (SQLException e) {
				System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
			}

		} catch (SQLException e) {
			System.out.println("Error SQL (" + e.getSQLState() + "): " + e.getMessage());
		}

	} // fin del main

}
