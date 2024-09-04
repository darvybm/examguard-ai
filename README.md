<p align="center"> 
  <img src="https://github.com/user-attachments/assets/177812da-9b37-4fcd-a1fb-1342d8f587ac" alt="Examguard IA" width="400"/> 
</p> 

<p align="center"> 
  <img src="https://img.shields.io/badge/Language-Java%20%7C%20Python-blue" alt="Language Badge"/> 
  <img src="https://img.shields.io/badge/Framework-SpringBoot-brightgreen" alt="Spring Boot Badge"/> 
</p>
<p align="center"> 
  <img src="https://img.shields.io/badge/API-Flask-yellow" alt="API Badge"/> 
  <img src="https://img.shields.io/badge/AI-Object%20Detection%20%7C%20Face%20Recognition-red" alt="AI Badge"/> 
  <img src="https://img.shields.io/badge/Model-YOLOv8-orange" alt="YOLOv8 Badge"/>
</p>
<p align="center"> 
  <img src="https://img.shields.io/badge/Libraries-OpenCV%20%7C%20MediaPipe%20%7C%20CVLib-lightgrey" alt="Libraries Badge"/> 
  <img src="https://img.shields.io/badge/Computer%20Vision-Enabled-brightblue" alt="Computer Vision Badge"/> 
  <img src="https://img.shields.io/badge/Deep%20Learning-Powered%20by%20PyTorch-brightpurple" alt="Deep Learning Badge"/>
</p>

# Examguard IA

**Examguard IA** es una plataforma web avanzada diseñada para permitir a los usuarios trackear minuciosamente la actividad de una persona en una grabación de video con el propósito de identificar comportamientos fraudulentos a lo largo de la línea de tiempo. El sistema es capaz de trackear la posición de la cabeza, detectando cuándo la cabeza se encuentra a la derecha, izquierda, arriba, abajo, o al frente. Además, detecta la presencia del rostro y señala cuando este se encuentra ausente. Otra de sus funcionalidades es la detección de objetos, permitiendo trackear en el video la aparición de elementos como celulares o periféricos de computadora. Toda esta información es recopilada tras procesar el video, y luego se presenta en un reporte interactivo y dinámico. En este reporte, los usuarios pueden observar en el timeline los momentos específicos del video donde ocurrieron los eventos detectados, acompañados de evidencias visuales y diversas estadísticas.

La parte de inteligencia artificial en **Examguard IA** se construyó utilizando Python. Para la detección de objetos, se emplea la biblioteca **cvlib**. El tracking de la posición de la cabeza se realiza mediante el algoritmo **PnP** (Perspective-n-Point), apoyado por **MediaPipe** para obtener los puntos de referencia del rostro. El procesamiento de estos puntos y la iteración sobre los fotogramas del video se manejan con **OpenCV**, que también se utiliza para la detección de objetos. El sistema incorpora un modelo personalizado de detección de objetos entrenado con **YOLOv8**, utilizando un conjunto de imágenes extraídas de datasets de libre acceso como **COCO**, **OpenDataset**, y otros datasets disponibles en **Roboflow**. Todas estas técnicas se integran en un solo algoritmo en Python, el cual procesa los videos recibidos a través de una API construida en **Flask**, retornando los resultados analizados.

La página web de **Examguard IA** está meticulosamente construida en Java, utilizando el framework **Spring Boot**. La autenticación y autorización de la aplicación se manejan con **Spring Security**. En el frontend, se utiliza **HTML**, **CSS**, **JavaScript**, y **Bootstrap**, basándose en el dashboard libre de **CoreUI**. Cuando un usuario carga un video en la plataforma construida con Spring Boot, este se conecta con la API de Python implementada en Flask. La comunicación entre Flask y Spring se gestiona mediante **JWT** (JSON Web Tokens). La API de Flask almacena el video en un sistema de archivos local y, si se le indica, lo procesa segundo por segundo (no fotograma por fotograma, para optimizar el tiempo de procesamiento).

## Tabla de Contenidos
- [Características](#características)
- [Aplicación](#aplicación)
- [Herramientas Utilizadas](#herramientas-utilizadas)
- [Cómo Instalar](#cómo-instalar)
- [Contribuidores](#contribuidores)
- [Licencia](#licencia)
- [Contáctame](#contáctame)

## Características
**Features de Examguard IA**

- **Gestión de cuentas**: Los usuarios pueden crearse una cuenta e iniciar sesión, con la opción de recordar la sesión para futuros accesos.

- **Manejo de grabaciones por carpetas**: Los usuarios pueden crear carpetas para organizar diferentes grabaciones. Por ejemplo, una carpeta puede representar una clase completa. Cada carpeta puede tener un título y una descripción, y al acceder a una, se mostrarán estadísticas generales calculadas a partir de los videos que contenga, como el Porcentaje de Fraude General y el Total de Eventos Fraudulentos.

> [!NOTE]  
> Un evento se define como cualquier acto detectado en un segundo específico de la grabación.

- **Carga y procesamiento de múltiples grabaciones**: Los usuarios pueden cargar varias grabaciones de manera simultánea en un video. Antes de procesarlas, tienen la opción de definir qué se considera fraudulento según el contexto, seleccionando entre las siguientes opciones:

    | Evento Detectado             | Descripción                                  |
    |------------------------------|----------------------------------------------|
    | PHONE_DETECTED               | Detección de teléfono móvil                  |
    | MOUSE_DETECTED               | Detección de ratón                           |
    | KEYBOARD_DETECTED            | Detección de teclado                         |
    | MONITOR_DETECTED             | Detección de monitor                         |
    | LAPTOP_DETECTED              | Detección de laptop                          |
    | NOT_FACE_DETECTED            | Rostro no detectado                          |
    | MULTIPLE_FACE_DETECTED       | Múltiples rostros detectados                 |
    | HEAD_POSE_LEFT               | Cabeza girada a la izquierda                 |
    | HEAD_POSE_RIGHT              | Cabeza girada a la derecha                   |
    | HEAD_POSE_UP                 | Cabeza mirando hacia arriba                  |
    | HEAD_POSE_DOWN               | Cabeza mirando hacia abajo                   |
    | HEAD_POSE_FORWARD            | Cabeza mirando al frente                     |
    | HEAD_POSE_UNKNOWN            | Posición de cabeza desconocida               |
    | SAFE                         | Acción considerada segura                    |

- **Análisis detallado de grabaciones**: Tras el procesamiento, los usuarios pueden acceder a un detalle exhaustivo de cada grabación, donde se visualizan estadísticas individuales, como el porcentaje de fraude, gráficos de la cantidad de fraude detectado por tipo de evento, y gráficos de tendencia de la mirada.

- **Interacción con el timeline de eventos fraudulentos**: En el detalle de la grabación, se genera un timeline que marca cada evento fraudulento detectado. Al hacer clic en un segmento del timeline, se despliega un panel que muestra evidencias visuales de lo detectado, incluidas imágenes y secuencias en formato GIF para ayudar a comprender mejor el contexto.

> [!NOTE]  
> El porcentaje de fraude detectado es una métrica basada en el cálculo del rango de tiempo en que se detectó el evento en relación con la duración total del video. Este cálculo puede ser controversial; por ejemplo, si alguien utiliza un celular durante 5 segundos en un video de una hora, el porcentaje de fraude sería extremadamente bajo.

- **Asociación de grabaciones a personas**: Los usuarios pueden asociar una grabación a un estudiante o persona específica y enviar correos electrónicos con los resultados obtenidos.

> [!IMPORTANT]  
> Asegúrese de revisar y validar los eventos detectados antes de enviar los resultados, ya que la interpretación del contexto puede variar según el caso.

### **Application**

**Vista de bienvenida**  
 Esta vista inicial presenta la marca de **Examguard IA**. La imagen principal es un búho, que simboliza el lema de la aplicación: "Con Examguard, protege tus evaluaciones como un búho vigila su territorio."

 <p align="center">
  <img src="https://github.com/user-attachments/assets/f944211d-8e68-4098-a12f-7c5a7432cb79" alt="Sign In" width="800"/>
</p>

**Vista de login/register**  
 En esta vista, los usuarios pueden iniciar sesión o registrarse en la plataforma. También se ofrece la opción de recordar la sesión para mantenerla iniciada en futuros accesos.

 > [!WARNING]  
 > No se pueden procesar videos ni realizar operaciones en la aplicación o la API sin estar registrado y autenticado. La API de Flask es solo para uso interno de Spring Boot.

<p align="center">
  <img src="https://github.com/user-attachments/assets/a363b327-872b-4355-afbc-d22af5e0dbce" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/468ca4d3-6ed6-4484-9180-8e63397b5f42" alt="Sign In" width="800"/>
</p>

**Vista de home**  
 Vista simple que da la bienvenida al usuario, mostrando su información personal y ofreciendo la opción de cambiar la contraseña.

<p align="center">
  <img src="https://github.com/user-attachments/assets/a4e942f3-46da-4004-9bb6-020981a63880" alt="Sign In" width="800"/>
</p>

**Vista de folder list**  
 Aquí se listan los folders creados por el usuario. Se muestran como tarjetas donde se puede ver el nombre del folder, la descripción, la cantidad de grabaciones contenidas y una barra de progreso que indica la cantidad de fraude detectado. Además, se puede crear un nuevo folder mediante un panel lateral desplegable.

 <p align="center">
  <img src="https://github.com/user-attachments/assets/0236da48-d190-4f75-8fb5-666dd48316a1" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/1821247c-d474-4bfe-aebf-75a9dc1ccf5e" alt="Sign In" width="800"/>
</p>



**Vista de recording list**  
 Esta vista muestra todas las grabaciones, independientemente del folder en el que se encuentren. Se presentan estadísticas detalladas, incluyendo:

 - Porcentaje de Fraude
 - Total de Eventos
 - Total de Eventos Fraudulentos
 - Total de Grabaciones
 - Grabaciones no Procesadas
 - Tiempo Total en Grabaciones
 - Tiempo Promedio por Grabación
 - Grabaciones Procesadas

<p align="center">
  <img src="https://github.com/user-attachments/assets/8fb8e5fb-467d-43d1-a4d7-c4ce3ccb2829" alt="Sign In" width="800"/>
</p>


**Vista de student list**  
 En esta vista se listan los estudiantes, o las personas que se pueden asociar a una grabación. Los usuarios tienen la opción de editar o eliminar a los estudiantes registrados.

 <p align="center">
  <img src="https://github.com/user-attachments/assets/37c2b485-4aea-4f9b-bf62-62533df32b80" alt="Sign In" width="800"/>
 </p>


**Vista del detalle de un folder**  
Esta vista presenta primero un panel con estadísticas generales del folder. A continuación, se ofrece un área de drag and drop para cargar o arrastrar grabaciones. Después, se muestran gráficos referenciales, seguidos de una tabla que lista las grabaciones con sus metadatos (duración, estado de procesamiento, estudiante asociado, porcentaje de fraude, etc.). La tabla también incluye un panel de botones para ver el detalle de la grabación, procesarla o eliminarla. Al seleccionar procesar, se abre un modal que permite al usuario elegir qué eventos desea detectar.

<p align="center">
  <img src="https://github.com/user-attachments/assets/8e99fdb9-4fb7-4607-b33f-16edb752f41c" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/1eed3e25-d761-45e4-ad4a-6fcc5af45b59" alt="Sign In" width="800"/>
</p>

**Vista del detalle de una grabación**  
Esta es una de las vistas más importantes de la plataforma. Primero se muestran las estadísticas detectadas en la grabación, seguidas de dos gráficos: uno de barras que muestra la Cantidad de Fraude Detectado por Tipo de Evento y otro de radar que presenta la Tendencia de la Mirada. Luego, se muestra un panel que indica los eventos seleccionados previamente. Finalmente, se encuentra la sección crucial de la lista de timelines, donde se genera un timeline por cada tipo de evento fraudulento detectado. Si se detecta fraude en un segmento del tiempo, este se marca con un color distinto. Al hacer clic en ese segmento, se abre un panel que muestra una lista de imágenes que corroboran lo detectado. El usuario puede navegar por estas imágenes, entre segmentos, e incluso generar GIFs con la secuencia de imágenes dentro de un segmento.


<p align="center">
  <img src="https://github.com/user-attachments/assets/f717e191-0b7d-4906-9758-c50933ceb572" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/a4670dcc-7a48-4087-8ca2-778a21451e0b" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/b85f615b-c40b-45f8-b656-40a04508da46" alt="Sign In" width="800"/>
</p>

Aquí te presento la sección de **Tools** con badges que representan las tecnologías utilizadas:

### **Tools**

- ![Java](https://img.shields.io/badge/Java-007396?logo=java&logoColor=white&style=flat-square) **Java**: Lenguaje de programación principal para el desarrollo backend de la plataforma.
  
- ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=spring-boot&logoColor=white&style=flat-square) **Spring Boot**: Framework utilizado para construir el backend de la aplicación, proporcionando una arquitectura robusta y escalable.
  
- ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?logo=spring-security&logoColor=white&style=flat-square) **Spring Security**: Implementado para la autenticación y autorización dentro de la plataforma, asegurando que solo usuarios registrados y autenticados puedan acceder a las funciones.
  
- ![JWT](https://img.shields.io/badge/JWT-000000?logo=json-web-tokens&logoColor=white&style=flat-square) **JWT (JSON Web Tokens)**: Utilizado para manejar la autenticación entre la aplicación web y la API de Flask, garantizando una comunicación segura.
  
- ![MongoDB](https://img.shields.io/badge/MongoDB-47A248?logo=mongodb&logoColor=white&style=flat-square) **MongoDB**: Base de datos NoSQL utilizada para almacenar los datos de la plataforma.
  
- ![GridFS](https://img.shields.io/badge/GridFS-47A248?logo=mongodb&logoColor=white&style=flat-square) **GridFS de MongoDB**: Empleado para almacenar las imágenes de evidencia detectadas en los videos, permitiendo un manejo eficiente de archivos binarios de gran tamaño.
  
- ![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white&style=flat-square) ![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white&style=flat-square) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black&style=flat-square) ![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?logo=bootstrap&logoColor=white&style=flat-square) **HTML, CSS, JS, Bootstrap**: Tecnologías front-end utilizadas para crear una interfaz de usuario interactiva y responsiva. Bootstrap se utilizó como base para el diseño del dashboard (usando el dashboard libre de CoreUI).
  
- ![Gif Encoder](https://img.shields.io/badge/Gif_Encoder-4285F4?logo=square&logoColor=white&style=flat-square) **Gif Encoder de Square**: Librería en Java utilizada para la creación de GIFs a partir de secuencias de imágenes detectadas en los videos. [Gif Encoder de Square](https://github.com/square/gifencoder)
  
- ![Python](https://img.shields.io/badge/Python-3776AB?logo=python&logoColor=white&style=flat-square) **Python**: Lenguaje utilizado para desarrollar la lógica de procesamiento de videos e inteligencia artificial.
  
- ![Flask](https://img.shields.io/badge/Flask-000000?logo=flask&logoColor=white&style=flat-square) **Flask**: Framework web ligero utilizado para crear la API REST que procesa los videos y devuelve los resultados a la aplicación Java.
  
- ![Local File System](https://img.shields.io/badge/File_System-4A4A4A?logo=data&logoColor=white&style=flat-square) **Sistema de archivos local**: Utilizado para almacenar temporalmente las grabaciones de video antes de su procesamiento por la API de Flask.
  
- ![OpenCV](https://img.shields.io/badge/OpenCV-5C3EE8?logo=opencv&logoColor=white&style=flat-square) **OpenCV**: Librería de visión por computadora utilizada para iterar los fotogramas de los videos y realizar el análisis necesario.
  
- ![Cvlib](https://img.shields.io/badge/Cvlib-5C3EE8?logo=opencv&logoColor=white&style=flat-square) **Cvlib**: Utilizado específicamente para la detección de rostros dentro de los videos.
  
- ![Mediapipe](https://img.shields.io/badge/Mediapipe-FF6699?logo=mediapipe&logoColor=white&style=flat-square) **Mediapipe**: Utilizado para la estimación de puntos faciales, permitiendo un seguimiento preciso de la posición de la cabeza y otros movimientos faciales.
  
- ![Yolov8](https://img.shields.io/badge/Yolov8-FFCC00?logo=yolo&logoColor=black&style=flat-square) **Yolov8**: Modelo de detección de objetos entrenado para identificar elementos específicos dentro de las grabaciones.
  
- ![SendGrid](https://img.shields.io/badge/SendGrid-00BFFF?logo=sendgrid&logoColor=white&style=flat-square) **SendGrid**: Servicio de envío de correos electrónicos utilizado para notificar a los usuarios sobre los resultados de las evaluaciones.
  
- ![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white&style=flat-square) ![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?logo=docker&logoColor=white&style=flat-square) **Docker y Docker Compose**: Herramientas utilizadas para contenerizar y orquestar la aplicación y sus servicios, facilitando su despliegue y escalabilidad.


### **How to Use**

#### **Requerimientos**

- **Base de Datos:** Configurar una base de datos local o en la nube de MongoDB.
- **Entorno de Desarrollo:** Si no usas Docker, asegúrate de tener Python 3.12 y Java 17 instalados en tu sistema.
- **Modelo de Pytorch:** Necesitarás un modelo de Pytorch que detecte objetos.

> [!IMPORTANT]  
> No se incluye el modelo preentrenado debido a restricciones de derechos.

#### **Métodos de Instalación**

Puedes instalar la aplicación de dos maneras: usando Docker Compose o configurándola manualmente en un IDE o consola.

**Paso 1: Clonar el repositorio**

Clona el repositorio en tu máquina local.

```bash
git clone https://github.com/darvybm/examguard-ai
```

**Paso 2: Configurar las Clases del Modelo**

Antes de iniciar, debes configurar las clases del modelo en el archivo `cheat_detector_optimized.py`. Dirígete al siguiente fragmento de código:

```python
def __init__(self, model_path):
        self.model = YOLO(model_path, task='detect')
        self.classes = ["keyboard", "laptop", "monitor", "mouse", "phone"]
        self.mp_face_mesh = mp.solutions.face_mesh
        self.face_mesh = self.mp_face_mesh.FaceMesh(static_image_mode=False, refine_landmarks=True, max_num_faces=1, min_detection_confidence=0.5)
        self.lock = Lock()
        self.width = 480
        self.height = 384
```

Aquí puedes agregar o quitar clases según lo que tu modelo pueda detectar.

> [!WARNING]  
> El modelo debe tener la extensión `.pt`.

**Paso 3: Ajustar los Tipos de Eventos**

Si las clases de tu modelo son diferentes a las mostradas, debes agregar o eliminar esos tipos de eventos tanto en Python como en Java.

**En Python (`EventType`):**

```python
class EventType(Enum):
    PHONE_DETECTED = "PHONE_DETECTED"
    MOUSE_DETECTED = "MOUSE_DETECTED"
    KEYBOARD_DETECTED = "KEYBOARD_DETECTED"
    LAPTOP_DETECTED = "LAPTOP_DETECTED"
    MONITOR_DETECTED = "MONITOR_DETECTED"
    NOT_FACE_DETECTED = "NOT_FACE_DETECTED"
    MULTIPLE_FACE_DETECTED = "MULTIPLE_FACE_DETECTED"
    HEAD_POSE_LEFT = "HEAD_POSE_LEFT"
    HEAD_POSE_RIGHT = "HEAD_POSE_RIGHT"
    HEAD_POSE_UP = "HEAD_POSE_UP"
    HEAD_POSE_DOWN = "HEAD_POSE_DOWN"
    HEAD_POSE_FORWARD = "HEAD_POSE_FORWARD"
    SAFE = "SAFE"
```

**En Java (`EventType`):**

```java
public enum EventType {

    //Objects
    PHONE_DETECTED,
    MOUSE_DETECTED,
    KEYBOARD_DETECTED,
    MONITOR_DETECTED,
    LAPTOP_DETECTED,

    // Faces
    NOT_FACE_DETECTED,
    MULTIPLE_FACE_DETECTED,

    // Head Pose Estimation
    HEAD_POSE_LEFT,
    HEAD_POSE_RIGHT,
    HEAD_POSE_UP,
    HEAD_POSE_DOWN,
    HEAD_POSE_FORWARD,
    HEAD_POSE_UNKNOWN,

    SAFE
}
```

#### **Método 1: Usando Docker Compose**

1. **Crear archivo `.env`:** Crea un archivo `.env` en la raíz del proyecto con las siguientes variables de entorno:

    ```plaintext
    JWT_SECRET_KEY=<tu-secreto-jwt>
    JWT_EXPIRATION_TIME=<tiempo-de-expiración-jwt>
    FLASK_API_URL=http://flask-api:5000/api
    SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/examguard
    ```

2. **Iniciar el proyecto con Docker Compose:**

    ```bash
    docker-compose up --build
    ```

#### **Método 2: Instalación Manual sin Docker**

1. **Configurar Entorno Virtual de Python:**

   - Navega a la carpeta `ModelAPI` y crea un entorno virtual de Python.

    ```bash
    python3.12 -m venv venv
    source venv/bin/activate  # En Linux/Mac
    venv\Scripts\activate  # En Windows
    ```

   - Instala las dependencias del archivo `requirements.txt`.

    ```bash
    pip install -r requirements.txt
    ```

2. **Ajustar Variables de Entorno:**

   - Configura las variables necesarias en el archivo `application.properties` de Spring Boot y en `app.py` de Flask.

> [!IMPORTANT]  
> Agrega las variables `JWT_SECRET_KEY` tanto en el archivo `app.py` como en el `application.properties`.

3. **Iniciar Servicios:**

   - Inicia la API REST de Flask:

    ```bash
    python app.py
    ```

   - Corre la aplicación de Spring Boot con Gradle:

    ```bash
    ./gradlew bootRun
    ```
    
Aquí tienes la sección de **Posibles mejoras en el futuro**:

## **Posibles mejoras en el futuro**

- **Integración de Conversión de Voz a Texto:**  
  Integrar un sistema de conversión de voz a texto que permita transcribir las interacciones verbales de los usuarios durante las evaluaciones. Esto facilitará la detección de comportamientos sospechosos, como llamadas telefónicas entre compañeros, intercambio de respuestas, u otro tipo de comunicación verbal no autorizada.

- **Optimización del Modelo de Detección de Objetos:**  
  Refinar y ajustar el modelo de detección de objetos para mejorar su precisión, especialmente en condiciones de iluminación variable o cuando los objetos estén parcialmente ocultos.

- **Mejora del Algoritmo Perspective-n-Point (PnP):**  
  Mejorar el algoritmo PnP para manejar de manera más efectiva los casos en los que los rostros están completamente de perfil, asegurando una detección más precisa de las orientaciones de la cabeza.

- **Implementación de un Gestor de Ficheros en la Nube:**  
  Implementar un gestor de ficheros en la nube de alto rendimiento para la gestión y almacenamiento de datos, facilitando el escalado y la eficiencia en el manejo de grandes volúmenes de información.


Aquí tienes la sección de **Contribuidores** actualizada según tus indicaciones:

## **Contribuidores**
<table>
  <tr>
    <td align="center">
      <a href="https://github.com/darvybm">
        <img src="https://github.com/darvybm.png" width="100px;" alt="Darvy Betances"/>
        <br />
        <sub><b>Darvy Betances</b></sub>
      </a>
      <br />
      <span>Creador del Código</span>
    </td>
    <td align="center">
      <a href="https://pucmm.edu.do/">
        <img src="https://github.com/user-attachments/assets/25cca91c-719a-4d16-b044-d279d0786f6b" width="100px;" alt="PUCMM"/>
        <br />
        <sub><b>Pontificia Universidad Católica Madre y Maestra (PUCMM)</b></sub>
      </a>
      <br />
      <span>Institución Educativa</span>
    </td>
  </tr>
</table>

---

**Agradecimientos:**  
Quiero agradecer a mi familia por su constante apoyo durante todo este proceso. Su respaldo ha sido esencial para el desarrollo de este proyecto.

También agradezco a mi asesor, **Máximo Medrano**, por su orientación y ayuda durante el proyecto. Su conocimiento y asistencia fueron clave para su éxito.

Además, agradezco a mis profesores por brindarme las herramientas y conocimientos necesarios, y a mis compañeros de promoción por su apoyo y colaboración a lo largo de estos años. Su compañerismo ha sido muy valioso para mi. ¡Gracias a todos!

## Licencia
Este proyecto está destinado exclusivamente a fines educativos y de investigación. No está autorizado para ser utilizado con fines comerciales. Todos los derechos están reservados y el uso del código o cualquier parte del proyecto con fines comerciales está prohibido.

## Contáctame

<p align="center">
  <a href="https://www.linkedin.com/in/darvybm" target="_blank">
    <img src="https://img.shields.io/badge/LinkedIn-@darvybm-blue?style=flat&logo=linkedin&logoColor=white" alt="LinkedIn Badge"/>
  </a>
  <a href="mailto:darvybm@gmail.com" target="_blank">
    <img src="https://img.shields.io/badge/Email-Contact%20Me-orange" alt="Email Badge"/>
  </a>
</p>
