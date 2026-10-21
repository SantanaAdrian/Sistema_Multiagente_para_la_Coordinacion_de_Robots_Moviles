Sistema Multiagente para la Coordinacion de Robots Moviles
Sistema multiagente desarrollado en Java utilizando el framework JADE (Java Agent Development Framework) y una interfaz grafica en Java Swing.
El proyecto simula una flota de 26 robots moviles autonomos que coordinan sus movimientos en una cuadricula para formar de forma colaborativa la palabra 'FIN'.
Caracteristicas PrincipalesArquitectura Hibrida: Combina control centralizado para la asignacion inicial de tareas y coordinacion distribuida peer-to-peer en tiempo real.
Plataforma JADE (v4.6.0): Implementacion de agentes inteligentes bajo estandares FIPA y comunicacion basada en lenguaje ACL (REQUEST, AGREE, REFUSE).
Algoritmo BFS (Busqueda en Anchura): Utilizado por cada robot para calcular de forma autonoma la ruta optima hacia su celda de destino.
Negociacion con Sistema de Prioridades: Protocolo de resolucion de conflictos dinamico basado en un gradiente de prioridades segun la columna de destino, evitando interbloqueos (deadlocks) permanentes.
Simulacion Gráfica Interactiva: Interfaz en Java Swing (12x18 celdas) con control de colisiones sincronizado (thread-safe) y colores diferenciados para cada letra de la formacion.
Tecnologias y Entorno de DesarrolloLenguaje: 
Java SE 16  
Framework Multiagente: JADE 4.6.0 (jade.jar) 
Interfaz Grafica: Java Swing  
IDE: Eclipse IDE 
Sistema Operativo: Windows 11 
Estructura del Proyecto
Plaintextsrc/
├── mainPackage/
│   ├── Main.java              # Clase de arranque y definicion de la formacion
│   ├── RobotAgent.java        # Logica del agente robot (BFS, movimiento y negociacion)
│   ├── CoordinadorAgent.java  # Agente planificador (envio de ordenes a los 10s)
│   └── SimulacionPanel.java   # Interfaz grafica (cuadricula, colisiones y renderizado)
lib/
└── jade.jar                   # Libreria de JADE (configurada en el Classpath)
Configuracion y EjecucionClonar el repositorio:Bashgit clone https://github.com/tu-usuario/tu-repositorio.git
Importar el proyecto en tu entorno de desarrollo (por ejemplo, Eclipse IDE).  
Anadir JADE al Classpath: Asegurate de incluir jade.jar dentro del Classpath del proyecto (evitando el uso de module-info.java debido a incompatibilidades con Java 9+).
Ejecutar la simulacion: Inicia la clase principal Main.java.
Durante los primeros 10 segundos, los 26 robots se desplazaran de forma aleatoria sin colisionar. 
Transcurrido ese tiempo, el coordinador activara la formacion y los agentes navegaran para dibujar la palabra 'FIN' en la cuadricula. 
Fases de la SimulacionArranque: Inicializacion de la plataforma JADE y registro de los 27 agentes (26 robots + 1 coordinador).
Movimiento Aleatorio: Desplazamiento libre y seguro por la rejilla mediante exclusion mutua (synchronized).
Activacion de Formacion: Envio masivo de mensajes REQUEST con las posiciones objetivo mediante un WakerBehaviour. 
Negociacion Peer-to-Peer: Gestion de bloqueos mediante intercambio de mensajes ACL y respeto de prioridades por columnas.
Convergencia: Visualizacion final de la palabra 'FIN' completada con exito.
