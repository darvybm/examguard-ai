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

**Examguard IA** is an advanced web platform designed to allow users to meticulously track a person's activity in a video recording to identify fraudulent behaviors along the timeline. The system is capable of tracking head position, detecting when the head is to the right, left, up, down, or facing forward. It also detects the presence of the face and indicates when it is absent. Another feature is object detection, allowing the tracking of elements like mobile phones or computer peripherals in the video. All this information is collected after processing the video and is then presented in an interactive and dynamic report. In this report, users can view the specific moments in the video where detected events occurred, along with visual evidence and various statistics.

The AI component of **Examguard IA** was built using Python. For face detection, the **cvlib** library is used. Head position tracking is performed using the **PnP** (Perspective-n-Point) algorithm, supported by **MediaPipe** to obtain facial landmark points. The processing of these landmark points and the iteration over video frames are handled with **OpenCV**, which is also used for object detection. The system incorporates a custom object detection model trained with **YOLOv8**, using images extracted from freely available datasets like **COCO**, **OpenDataset**, and other datasets available on **Roboflow**. All these techniques are integrated into a single Python algorithm that processes the videos received through an API built in **Flask**, returning the analyzed results.

The **Examguard IA** website is meticulously built in Java, using the **Spring Boot** framework. Authentication and authorization are managed with **Spring Security**. On the frontend, **HTML**, **CSS**, **JavaScript**, and **Bootstrap** are used, based on the free dashboard from **CoreUI**. When a user uploads a video to the Spring Boot platform, it connects with the Python API implemented in Flask. Communication between Flask and Spring is managed via **JWT** (JSON Web Tokens). The Flask API stores the video in a local file system and, if instructed, processes it second by second (not frame by frame, to optimize processing time).

<p align="center">
  <img src="https://github.com/user-attachments/assets/39c9e369-9c31-482b-b446-3c0bc1c26b8f" alt="Sign In" width="1000"/>
</p>

## Table of Contents
- [Features](#features)
- [Algorithm Preview](#algorithm-preview)
- [Application](#application)
- [Tools Used](#tools-used)
- [Installation](#installation)
- [Areas for Improvement](#areas-for-improvement)
- [Contributors](#contributors)
- [License](#license)
- [Contact Me](#contact-me)


## Features

**Features of Examguard IA**

- **Account Management**: Users can create accounts and log in, with the option to remember the session for future access.

- **Recording Management by Folders**: Users can create folders to organize different recordings. For instance, a folder might represent an entire class. Each folder can have a title and description, and upon accessing it, general statistics calculated from the videos it contains will be displayed, such as Overall Fraud Percentage and Total Fraudulent Events.

> [!IMPORTANT]  
> An event is defined as any detected act at a specific second of the recording.

- **Upload and Processing of Multiple Recordings**: Users can upload multiple recordings simultaneously. Before processing them, they have the option to define what is considered fraudulent based on the context, selecting from the following options:

    | Detected Event               | Description                                   |
    |------------------------------|-----------------------------------------------|
    | PHONE_DETECTED               | Mobile phone detected                         |
    | MOUSE_DETECTED               | Mouse detected                                |
    | KEYBOARD_DETECTED            | Keyboard detected                             |
    | MONITOR_DETECTED             | Monitor detected                              |
    | LAPTOP_DETECTED              | Laptop detected                               |
    | NOT_FACE_DETECTED            | Face not detected                             |
    | MULTIPLE_FACE_DETECTED       | Multiple faces detected                       |
    | HEAD_POSE_LEFT               | Head turned left                             |
    | HEAD_POSE_RIGHT              | Head turned right                            |
    | HEAD_POSE_UP                 | Head looking up                              |
    | HEAD_POSE_DOWN               | Head looking down                            |
    | HEAD_POSE_FORWARD            | Head looking forward                         |
    | HEAD_POSE_UNKNOWN            | Unknown head position                        |
    | SAFE                         | Action considered safe                       |

- **Detailed Recording Analysis**: After processing, users can access an in-depth detail of each recording, where individual statistics are displayed, such as the fraud percentage, graphs of detected fraud by event type, and gaze trend graphs.

- **Interaction with the Fraudulent Events Timeline**: In the recording detail, a timeline is generated that marks each detected fraudulent event. Clicking on a segment of the timeline displays a panel showing visual evidence of what was detected, including images and GIF sequences to help better understand the context.

> [!NOTE]  
> The detected fraud percentage is a metric based on the time range when the event was detected relative to the total duration of the video. This calculation can be controversial; for example, if someone uses a phone for 5 seconds in a one-hour video, the fraud percentage would be extremely low.

- **Association of Recordings to People**: Users can associate a recording with a specific student or person and send emails with the obtained results.

## **Algorithm Preview**

This section provides an overview of how the algorithm processes a video to detect faces, estimate head rotation, and identify objects.

### **Workflow**
1. **Video Input:** The algorithm starts by receiving a video file.
2. **Frame Extraction:** The video is decomposed into frames, with one frame selected per second.
3. **Face Detection:**
   - **Single Face:** If a face is detected, the algorithm estimates the head's rotation.
   - **No Face:** It registers the absence of a face.
   - **Multiple Faces:** It detects and records the presence of multiple faces.
4. **Object Detection:** Simultaneously, the YOLO model identifies and classifies objects in the frame.

 <p align="center">
  <img src="https://github.com/user-attachments/assets/9946d381-3d9d-4e18-9e93-77beef823dc6" alt="Sign In" width="700"/>
</p>

### **Object Detection Results**
The YOLOv8 model medium effectively identified objects across various conditions, showing improvements in key metrics over 30 epochs:
- **Precision:** Increased from 0.66988 to 0.77241.
- **Recall:** Improved from 0.66753 to 0.74952.
- **mAP50:** Rose from 0.68484 to 0.79044.
- **Validation Losses:** Decreased consistently, indicating strong generalization.

<p align="center">
  <img src="https://github.com/user-attachments/assets/cef8b380-50dc-40c1-98d0-64f38f1d23a9" alt="Sign In" width="800"/>
</p>


### **Face Detection & Landmark Estimation**
Using cvlib, face detection was successful, though it faced challenges with faces too close or too far from the camera. MediaPipe provided precise and detailed landmark estimations, crucial for understanding facial geometry.

> [!IMPORTANT]  
> Ensure to review and validate the detected events before sending the results, as the interpretation of the context may vary depending on the case.

<p align="center">
  <img src="https://github.com/darvybm/examguard-ai/blob/main/algorithm-demo/algorithm-test-2.gif" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/darvybm/examguard-ai/blob/main/algorithm-demo/algorithm-test.gif" alt="Sign In" width="800"/>
</p>

## **Application**

**Welcome View**  
This initial view displays the **Examguard IA** branding. The main image is an owl, symbolizing the application's motto: "With Examguard, protect your assessments as an owl guards its territory."

 <p align="center">
  <img src="https://github.com/user-attachments/assets/f944211d-8e68-4098-a12f-7c5a7432cb79" alt="Sign In" width="800"/>
</p>

**Login/Register View**  
In this view, users can log in or register on the platform. There is also the option to remember the session for future logins.

 > [!WARNING]  
 > Videos cannot be processed or operations performed in the application or API without being registered and authenticated. The Flask API is for internal use by Spring Boot only.

<p align="center">
  <img src="https://github.com/user-attachments/assets/a363b327-872b-4355-afbc-d22af5e0dbce" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/468ca4d3-6ed6-4484-9180-8e63397b5f42" alt="Sign In" width="800"/>
</p>

**Home View**  
A simple view welcoming the user, displaying their personal information and offering the option to change the password.

<p align="center">
  <img src="https://github.com/user-attachments/assets/a4e942f3-46da-4004-9bb6-020981a63880" alt="Sign In" width="800"/>
</p>

**Folder List View**  
Here, the folders created by the user are listed. They appear as cards where you can see the folder name, description, number of recordings contained, and a progress bar indicating the amount of detected fraud. Additionally, you can create a new folder through a collapsible side panel.

 <p align="center">
  <img src="https://github.com/user-attachments/assets/0236da48-d190-4f75-8fb5-666dd48316a1" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/1821247c-d474-4bfe-aebf-75a9dc1ccf5e" alt="Sign In" width="800"/>
</p>

**Recording List View**  
This view shows all recordings, regardless of the folder they are in. It presents detailed statistics, including:

 - Fraud Percentage
 - Total Events
 - Total Fraudulent Events
 - Total Recordings
 - Unprocessed Recordings
 - Total Time in Recordings
 - Average Time per Recording
 - Processed Recordings

<p align="center">
  <img src="https://github.com/user-attachments/assets/8fb8e5fb-467d-43d1-a4d7-c4ce3ccb2829" alt="Sign In" width="800"/>
</p>

**Student List View**  
In this view, students or people that can be associated with a recording are listed. Users have the option to edit or delete registered students.

 <p align="center">
  <img src="https://github.com/user-attachments/assets/37c2b485-4aea-4f9b-bf62-62533df32b80" alt="Sign In" width="800"/>
 </p>

**Folder Detail View**  
This view first presents a panel with general statistics about the folder. Next, there is a drag-and-drop area for uploading or dragging recordings. Following this, reference graphs are displayed, followed by a table listing the recordings with their metadata (duration, processing status, associated student, fraud percentage, etc.). The table also includes a button panel to view the recording details, process it, or delete it. When selecting process, a modal opens allowing the user to choose which events to detect.

<p align="center">
  <img src="https://github.com/user-attachments/assets/8e99fdb9-4fb7-4607-b33f-16edb752f41c" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/1eed3e25-d761-45e4-ad4a-6fcc5af45b59" alt="Sign In" width="800"/>
</p>

**Recording Detail View**  
This is one of the most important views on the platform. It first displays the detected statistics for the recording, followed by two graphs: a bar chart showing the Amount of Detected Fraud by Event Type and a radar chart presenting Gaze Trend. Next, a panel indicates the previously selected events. Finally, the crucial section of the timeline list is displayed, where a timeline is generated for each type of detected fraudulent event. If fraud is detected in a time segment, it is marked with a different color. Clicking on that segment opens a panel showing a list of images corroborating the detection. Users can navigate through these images, between segments, and even generate GIFs with the sequence of images within a segment.

<p align="center">
  <img src="https://github.com/user-attachments/assets/f717e191-0b7d-4906-9758-c50933ceb572" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/a4670dcc-7a48-4087-8ca2-778a21451e0b" alt="Sign In" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/b85f615b-c40b-45f8-b656-40a04508da46" alt="Sign In" width="800"/>
</p>


## **Tools Used**

- ![Java](https://img.shields.io/badge/Java-007396?logo=java&logoColor=white&style=flat-square) **Java**: Main programming language used for backend development of the platform.
  
- ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=spring-boot&logoColor=white&style=flat-square) **Spring Boot**: Framework used to build the backend of the application, providing a robust and scalable architecture.
  
- ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?logo=spring-security&logoColor=white&style=flat-square) **Spring Security**: Implemented for authentication and authorization within the platform, ensuring that only registered and authenticated users can access the functions.
  
- ![JWT](https://img.shields.io/badge/JWT-000000?logo=json-web-tokens&logoColor=white&style=flat-square) **JWT (JSON Web Tokens)**: Used to manage authentication between the web application and the Flask API, ensuring secure communication.
  
- ![MongoDB](https://img.shields.io/badge/MongoDB-47A248?logo=mongodb&logoColor=white&style=flat-square) **MongoDB**: NoSQL database used to store the platform's data.
  
- ![GridFS](https://img.shields.io/badge/GridFS-47A248?logo=mongodb&logoColor=white&style=flat-square) **MongoDB GridFS**: Used to store detected evidence images in videos, allowing efficient management of large binary files.
  
- ![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white&style=flat-square) ![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white&style=flat-square) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black&style=flat-square) ![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?logo=bootstrap&logoColor=white&style=flat-square) **HTML, CSS, JS, Bootstrap**: Front-end technologies used to create an interactive and responsive user interface. Bootstrap was used as the base for the dashboard design (using the free CoreUI dashboard).
  
- ![Gif Encoder](https://img.shields.io/badge/Gif_Encoder-4285F4?logo=square&logoColor=white&style=flat-square) **Gif Encoder by Square**: Java library used for creating GIFs from sequences of images detected in the videos. [Gif Encoder by Square](https://github.com/square/gifencoder)
  
- ![Python](https://img.shields.io/badge/Python-3776AB?logo=python&logoColor=white&style=flat-square) **Python**: Language used to develop video processing and artificial intelligence logic.
  
- ![Flask](https://img.shields.io/badge/Flask-000000?logo=flask&logoColor=white&style=flat-square) **Flask**: Lightweight web framework used to create the REST API that processes videos and returns results to the Java application.
  
- ![Local File System](https://img.shields.io/badge/File_System-4A4A4A?logo=data&logoColor=white&style=flat-square) **Local File System**: Used to temporarily store video recordings before processing them by the Flask API.
  
- ![OpenCV](https://img.shields.io/badge/OpenCV-5C3EE8?logo=opencv&logoColor=white&style=flat-square) **OpenCV**: Computer vision library used to iterate over video frames and perform necessary analysis.
  
- ![Cvlib](https://img.shields.io/badge/Cvlib-5C3EE8?logo=opencv&logoColor=white&style=flat-square) **Cvlib**: Specifically used for face detection within videos.
  
- ![Mediapipe](https://img.shields.io/badge/Mediapipe-FF6699?logo=mediapipe&logoColor=white&style=flat-square) **Mediapipe**: Used for facial landmark estimation, allowing precise tracking of head position and other facial movements.
  
- ![Yolov8](https://img.shields.io/badge/Yolov8-FFCC00?logo=yolo&logoColor=black&style=flat-square) **Yolov8**: Object detection model trained to identify specific elements within recordings.
  
- ![SendGrid](https://img.shields.io/badge/SendGrid-00BFFF?logo=sendgrid&logoColor=white&style=flat-square) **SendGrid**: Email service used to notify users about evaluation results.
  
- ![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white&style=flat-square) ![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?logo=docker&logoColor=white&style=flat-square) **Docker and Docker Compose**: Tools used to containerize and orchestrate the application and its services, facilitating deployment and scalability.


## **Installation**

#### **Requirements**

- **Database:** Configure a local or cloud MongoDB database.
- **Development Environment:** If not using Docker, ensure Python 3.12 and Java 17 are installed on your system.
- **PyTorch Model:** You will need a PyTorch model for object detection.

> [!IMPORTANT]  
> The pre-trained model is not included due to licensing restrictions.

#### **Installation Methods**

You can install the application in two ways: using Docker Compose or setting it up manually in an IDE or console.

**Step 1: Clone the Repository**

Clone the repository to your local machine.

```bash
git clone https://github.com/darvybm/examguard-ai
```

**Step 2: Configure Model Classes**

Before starting, you need to configure the model classes in the `cheat_detector_optimized.py` file. Locate the file in `ModelAPI/cheat_detector_optimized.py` and find the following code snippet:

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

Here, you can add or remove classes based on what your model can detect.

> [!WARNING]  
> The model must have the `.pt` extension.

**Step 3: Adjust Event Types**

If your model classes differ from those shown, you need to add or remove those event types in both Python and Java.

**In Python (`EventType`):**

Edit the `EventType` class in `ModelAPI/app.py`:

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

**In Java (`EventType`):**

Edit the `EventType` enum in `ExamGuard/src/main/java/pucmm/eict/proyectofinal/examguard/model/enums/EventType.java`:

```java
public enum EventType {

    // Objects
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

#### **Method 1: Using Docker Compose**

1. **Create `.env` File:** Create a `.env` file in the root of the project with the following environment variables:

    ```plaintext
    JWT_SECRET_KEY=<your-jwt-secret>
    JWT_EXPIRATION_TIME=<jwt-expiration-time>
    FLASK_API_URL=http://flask-api:5000/api
    SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/examguard
    ```

2. **Start the Project with Docker Compose:**

    ```bash
    docker-compose up --build
    ```

#### **Method 2: Manual Installation without Docker**

1. **Configure Python Virtual Environment:**

   - Navigate to the `ModelAPI` folder and create a Python virtual environment.

    ```bash
    python3.12 -m venv venv
    source venv/bin/activate  # On Linux/Mac
    venv\Scripts\activate  # On Windows
    ```

   - Install dependencies from the `requirements.txt` file.

    ```bash
    pip install -r requirements.txt
    ```

2. **Adjust Environment Variables:**

   - Configure the necessary variables in the `application.properties` file for Spring Boot (located in `ExamGuard/src/main/resources/application.properties`) and in `app.py` for Flask (located in `ModelAPI/app.py`).

> [!IMPORTANT]  
> Add the `JWT_SECRET_KEY` variable in both `app.py` and `application.properties`.

3. **Start Services:**

   - Start the Flask REST API:

    ```bash
    python ModelAPI/app.py
    ```

   - Run the Spring Boot application with Gradle:

    ```bash
    ./gradlew bootRun
    ```

## **Areas for Improvement**

- **Voice-to-Text Integration:**  
  Integrate a voice-to-text system to transcribe verbal interactions during assessments. This will facilitate the detection of suspicious behaviors such as phone calls between peers, exchange of answers, or other unauthorized verbal communications.

- **Object Detection Model Optimization:**  
  Refine and adjust the object detection model to improve accuracy, especially under varying lighting conditions or when objects are partially obscured.

- **Perspective-n-Point (PnP) Algorithm Enhancement:**  
  Improve the PnP algorithm to handle cases where faces are completely profile, ensuring more precise detection of head orientations.

- **Cloud File Management System Implementation:**  
  Implement a high-performance cloud file management system for data management and storage, facilitating scaling and efficiency in handling large volumes of information.


## **Contributors**

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/darvybm">
        <img src="https://github.com/darvybm.png" width="100px;" alt="Darvy Betances"/>
        <br />
        <sub><b>Darvy Betances</b></sub>
      </a>
      <br />
      <span>Code Creator</span>
    </td>
    <td align="center">
      <a href="https://pucmm.edu.do/">
        <img src="https://github.com/user-attachments/assets/25cca91c-719a-4d16-b044-d279d0786f6b" width="100px;" alt="PUCMM"/>
        <br />
        <sub><b>Pontificia Universidad Católica Madre y Maestra (PUCMM)</b></sub>
      </a>
      <br />
      <span>Educational Institution</span>
    </td>
  </tr>
</table>

---

**Acknowledgments:**  
I want to thank my family for their constant support throughout this process. Their backing has been essential for the development of this project.

I also thank my advisor, **Máximo Medrano**, for his guidance and assistance throughout the project. His knowledge and help were crucial for its success.

Additionally, I appreciate my professors for providing me with the necessary tools and knowledge, and my fellow students for their support and collaboration over the years. Their camaraderie has been very valuable to me. Thank you all!

---

## **License**

This project is intended exclusively for educational and research purposes. It is not authorized for commercial use. All rights are reserved, and any commercial use of the code or any part of the project is prohibited.


## **Contact Me**

<p align="center">
  <a href="https://www.linkedin.com/in/darvybm" target="_blank">
    <img src="https://img.shields.io/badge/LinkedIn-@darvybm-blue?style=flat&logo=linkedin&logoColor=white" alt="LinkedIn Badge"/>
  </a>
  <a href="mailto:darvybm@gmail.com" target="_blank">
    <img src="https://img.shields.io/badge/Email-Contact%20Me-orange" alt="Email Badge"/>
  </a>
</p>
