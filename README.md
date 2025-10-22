# **Sistema de Gestión Clínica**
Este es un sistema web para la gestión médica de un único doctor, desarrollado en Spring Boot.
Permite administrar pacientes, citas, historiales médicos y recetas, todo desde una interfaz sencilla accesible desde el navegador.

## **Características principales**
- Gestión de un único médico y sus especialidades.
- Registro, edición y eliminación de pacientes.
- Creación y cancelación de citas médicas.
- Registro de historiales médicos asociados a las citas.
- Administración de recetas médicas por consulta.

## **Tecnologías utilizadas**
- Java 17+
- Spring Boot
- Spring MVC + Thymeleaf
- PostgreSQL
- pgAdmin 4
- Maven

## **Configuración de la base de datos (PostgreSQL)**
El proyecto incluye el archivo BD.sql en el repositorio, con toda la estructura de la base de datos.

### **Cómo crear la base de datos usando pgAdmin:**
1. Abre pgAdmin 4.
2. Crea una nueva base de datos:
3. Haz clic derecho en “Databases” → Create → Database...
4. Asigna el nombre clinica_db.
5. Abre el Query Tool.
6. Copia y pega el contenido del archivo BD.sql del repositorio.
7. Ejecuta el script.
8. Verifica que las tablas se hayan creado correctamente.

## **Ejecución del proyecto**
1. Abre tu IDE de preferencia (por ejemplo, IntelliJ IDEA, Eclipse o Spring Tools Suite).
Selecciona la opción “Clone Repository” o “Get from Version Control”, y pega el siguiente enlace:
https://github.com/DanielBA05/Sistema-de-Medicos
Esto descargará el proyecto completo desde GitHub.
3. Una vez clonado, el IDE lo detectará como un proyecto Maven / Spring Boot automáticamente.
4. Asegúrate de que las dependencias se descarguen correctamente.
5. En el archivo application.properties, ajusta tus credenciales de base de datos:
spring.datasource.url=jdbc:postgresql://localhost:5432/clinica_db
spring.datasource.username=**tu_usuario**
spring.datasource.password=**tu_contraseña**
spring.jpa.hibernate.ddl-auto=none
6. Busca la clase principal llamada; ClinicaApplication.java
7. Haz clic en Run

## **Acceder al sistema**
Una vez que la aplicación esté corriendo, abre tu navegador y visita:

http://localhost:8080/

Ahí encontrarás el panel principal del sistema clínico.

## Autores
Proyecto académico desarrollado por estudiantes del Instituto Tecnológico de Costa Rica (ITCR)
para el curso de Bases de Datos I:

- Luis Daniel Barboza Alfaro
- Jose Mario Castro Cruz
