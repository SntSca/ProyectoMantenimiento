Para poder realizar análisis de calidad de nuestro código tendremos que usar `sonar-scanner`, `sonarqube` y los informes de cobertura de código proporcionados por `jacoco`.

# Backend

### Uso de `Jacoco` para hacer reportes

Como tal, con tener añadida la dependencia de jacoco en el `pom.xml` es suficiente para poder ejecutar sus informes:
```XML
<build>
	<plugins>
		<plugin>
			<groupId>org.jacoco</groupId>
			<artifactId>jacoco-maven-plugin</artifactId>
			<version>0.8.11</version>
			<executions>
				<execution>
					<goals>
						<goal>prepare-agent</goal>
					</goals>
				</execution>
				<execution>
					<id>report</id>
					<phase>test</phase>
					<goals>
						<goal>report</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

Ahora, para realizarlos, existen dos opciones:
- Si queremos realizar el informe sin tener en cuenta si se han pasado las pruebas o no:
`mvn jacoco:report`

- Si nuestro código pasa todas las pruebas, el informe se generará automáticamente con:
`mvn clean verify`

>[!warning] Cuidado con la dirección en la que se genera
>Por defecto las guardará en la carpeta `/target/site`, aunque puedes definir una específicica cambiando el atributo de generación al esperado

Al generarse el informe, podremos consultarlo desde el ordenador (genera una página web con el informe de las clases y su cobertura).

### Uso de `Sonar-scanner`
Para esto necesitaremos tener instalado `sonar-scanner` (la guía está en la asignatura). Si lo tuviésemos instalado correctamente, podremos ejecutar el análisis de sonarqube con el siguiente comando:
`sonar-scanner -D sonar.token=<token_Id>`

![imagen.png](/.attachments/imagen-81cf3955-af41-4cc3-b948-efc5a279a615.png)
# Frontend

En el frontend necesitaremos otro archivo distinto `sonar-project.properties`:
```sonar-project.properties
# Must be unique in a given SonarQube instance
sonar.host.url=http://seralu4.esi.uclm.es:1521/
sonar.projectKey=2025-g07-frontend

# This is the name and version displayed in the SonarQube UI.
# Was mandatory prior to SonarQube 6.1.
sonar.projectName=2025-g07-frontend
sonar.projectVersion=0.3.0

# Encoding of the source code. Default is default system encoding
sonar.sourceEncoding=UTF-8
sonar.sources=src

# Excluir clases de testing en las pruebas
sonar.exclusions=**/src/test/**/*
```

Y otro comando que use una clave distinta a la de los backends (creada en el perfil):
```Shell
sonar-scanner -D sonar.token=<token_Id>
```