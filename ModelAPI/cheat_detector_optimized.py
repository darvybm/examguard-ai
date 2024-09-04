import base64
import concurrent.futures
from enum import Enum
from threading import Lock
import cv2
import cvlib as cv
import mediapipe as mp
import numpy as np
import torch
from ultralytics import YOLO

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

class Event:
    def __init__(self, event_type, second, image):
        self.event_type = event_type
        self.second = second
        self.image = image

    def to_dict(self):
        return {
            'event_type': self.event_type.value,
            'second': self.second,
            'image': self.image
        }

    def __str__(self):
        return f"Event: {self.event_type.value}, Second: {self.second}, Image Base64: {self.image[:50]}..."
    
    def __repr__(self):
        return self.__str__()
    
    
class CheatDetector:

    def __init__(self, model_path):
        self.model = YOLO(model_path, task='detect')
        self.classes = ["keyboard", "laptop", "monitor", "mouse", "phone"]
        self.mp_face_mesh = mp.solutions.face_mesh
        self.face_mesh = self.mp_face_mesh.FaceMesh(static_image_mode=False, refine_landmarks=True, max_num_faces=1, min_detection_confidence=0.5)
        self.lock = Lock()
        self.width = 480
        self.height = 384

    def _detect_faces(self, frame):
        with self.lock:
            use_gpu = torch.cuda.is_available()
            return cv.detect_face(frame, threshold=0.2, enable_gpu=use_gpu)

    def detect(self, video_path):
        print("CUDA")
        print(torch.cuda.is_available())
        cap = cv2.VideoCapture(video_path)
        events = []
        second_count = 0
        frame_interval = 1
        futures = []

        with concurrent.futures.ThreadPoolExecutor(max_workers=4) as executor:
            try:
                while cap.isOpened():
                    cap.set(cv2.CAP_PROP_POS_MSEC, second_count * 1000)
                    success, frame = cap.read()
                    
                    if not success:
                        break
                    
                    frame = cv2.resize(frame, (self.width, self.height))
                    frame = cv2.flip(frame, 1)

                    # Submit frame for processing
                    futures.append(executor.submit(self._process_frame, frame, second_count))
                    
                    second_count += frame_interval

                # Wait for all futures to complete
                for future in concurrent.futures.as_completed(futures):
                    print("FUTURO COMPLETADO")
                    try:
                        result = future.result()
                        if result:
                            events.extend(result)
                    except Exception as e:
                        print("Error en el futuro:", e)

            finally:
                cap.release()
                cv2.destroyAllWindows()

        print("RETORNANDO EVENTOS")

        return events
    
    def _process_frame(self, frame, second_count):
        events = []
        # Procesar la detección de objetos
        events.extend(self._process_object_detection(frame, second_count))
            
        try:
            # Procesar la detección de caras
            faces, _ = self._detect_faces(frame)
            frame_copy = frame.copy()

            if len(faces) == 1:
                face = faces[0]
                (startX, startY), (endX, endY) = face[:2], face[2:]
                face_roi = frame[startY:endY, startX:endX]
                face_roi = cv2.resize(face_roi, (self.width, self.height))
                
                # Dibuja el recuadro alrededor de la cara detectada
                cv2.rectangle(frame_copy, (startX, startY), (endX, endY), (0, 255, 0), 2)
                
                event = self._process_face_pose(frame_copy, face_roi, second_count)
                
                if event:
                    events.append(event)

            elif len(faces) == 0:
                image_base64 = self._encode_image_to_base64(frame_copy)
                event = Event(EventType.NOT_FACE_DETECTED, second_count, image_base64)
                events.append(event)

            elif len(faces) > 1:
                for face in faces:
                    (startX, startY), (endX, endY) = face[:2], face[2:]
                    cv2.rectangle(frame_copy, (startX, startY), (endX, endY), (0, 255, 0), 2)
                image_base64 = self._encode_image_to_base64(frame_copy)
                event = Event(EventType.MULTIPLE_FACE_DETECTED, second_count, image_base64)
                events.append(event)

        except Exception as e:
            print(f"Error al procesar el frame en el segundo {second_count}: {e}")

        return events

    def _process_object_detection(self, frame, second_count):

        events = []
        device = '0' if torch.cuda.is_available() else 'cpu'
        results = self.model.predict(frame, conf=0.5, device=device)
        last_detected = {}

        for result in results:
            boxes = result.boxes
            for box_data in boxes.data:
                x1, y1, x2, y2, conf, cls = box_data[:6]
                object_class = self.classes[int(cls)]
                text = f"Class: {object_class} - Conf: {conf:.2f}"
                if (object_class, second_count) not in last_detected:
                    # Dibuja el recuadro en la imagen
                    frame_copy = frame.copy()
                    cv2.rectangle(frame_copy, (int(x1), int(y1)), (int(x2), int(y2)), (0, 255, 0), 2)
                    image_base64 = self._encode_image_to_base64(frame_copy)
                    event = Event(EventType(object_class.upper() + "_DETECTED"), second_count, image_base64)
                    events.append(event)
                    last_detected[(object_class, second_count)] = event

        return events

    def _encode_image_to_base64(self, frame):
        _, img_encoded = cv2.imencode('.jpg', frame)
        return base64.b64encode(img_encoded).decode('utf-8')

    def _process_face_pose(self, frame, face_roi, second_count):
        try:
            image = cv2.cvtColor(face_roi, cv2.COLOR_BGR2RGB)
            image.flags.writeable = False
            results = self.face_mesh.process(image)
            image.flags.writeable = True
            img_h, img_w, img_c = image.shape

            if results.multi_face_landmarks:
                face_landmarks = results.multi_face_landmarks[0]
                landmark_image = np.zeros_like(frame)
                for landmark in face_landmarks.landmark:
                    x, y = int(landmark.x * image.shape[1]), int(landmark.y * image.shape[0])

                face_2d = []
                face_3d = []
                
                for idx, lm in enumerate(face_landmarks.landmark):
                    if idx == 1:
                        nose_2d = (int(lm.x * img_w), int(lm.y * img_h))
                        nose_3d = (int(lm.x * img_w), int(lm.y * img_h), lm.z * 3000)
                    x, y = int(lm.x * img_w), int(lm.y * img_h)
                    
                    x_relative = x / img_w
                    y_relative = y / img_h
                    
                    x_absolute = x_relative * face_roi.shape[1]
                    y_absolute = y_relative * face_roi.shape[0]
                    
                    face_2d.append([x_absolute, y_absolute])
                    face_3d.append([x_absolute, y_absolute, lm.z])

                face_2d_head = np.array([
                    face_2d[1],      # Nose
                    face_2d[199],    # Chin
                    face_2d[33],     # Left eye left corner
                    face_2d[263],    # Right eye right corner
                    face_2d[61],     # Left mouth corner
                    face_2d[291]     # Right mouth corner
                ], dtype=np.float64)

                face_3d_head = np.array([
                    face_3d[1],      # Nose
                    face_3d[199],    # Chin
                    face_3d[33],     # Left eye left corner
                    face_3d[263],    # Right eye right corner
                    face_3d[61],     # Left mouth corner
                    face_3d[291]     # Right mouth corner
                ], dtype=np.float64)

                face_2d = np.asarray(face_2d)

                face_3d = np.array([
                    [0.0, 0.0, 0.0],            # Nose tip
                    [0.0, -330.0, -65.0],       # Chin
                    [-225.0, 170.0, -135.0],    # Left eye left corner
                    [225.0, 170.0, -135.0],     # Right eye right corner
                    [-150.0, -150.0, -125.0],   # Left Mouth corner
                    [150.0, -150.0, -125.0]     # Right mouth corner
                ], dtype=np.float64)

                focal_length = 1 * img_w
                cam_matrix = np.array([[focal_length, 0, img_h / 2],
                                        [0, focal_length, img_w / 2],
                                        [0, 0, 1]])
                distortion_matrix = np.zeros((4, 1), dtype=np.float64)
                
                success, rotation_vec, translation_vec = cv2.solvePnP(face_3d_head, face_2d_head, cam_matrix, distortion_matrix)
                
                rmat, jac = cv2.Rodrigues(rotation_vec)
                
                angles, mtxR, mtxQ, Qx, Qy, Qz = cv2.RQDecomp3x3(rmat)
                
                x = angles[0] * 360
                y = angles[1] * 360
                z = angles[2] * 360

                text = ""

                # Determine event type based on y and x angles
                if abs(y) > abs(x):
                    if y < -12:
                        text = "Looking Left"
                        event_type = EventType.HEAD_POSE_LEFT
                    elif y > 12:
                        text = "Looking Right"
                        event_type = EventType.HEAD_POSE_RIGHT
                    else:
                        text = "Forward"
                        event_type = EventType.HEAD_POSE_FORWARD
                else:
                    if x < -15:
                        text = "Looking Down"
                        event_type = EventType.HEAD_POSE_DOWN
                    elif x > 20:
                        text = "Looking Up"
                        event_type = EventType.HEAD_POSE_UP
                    else:
                        text = "Forward"
                        event_type = EventType.HEAD_POSE_FORWARD


                p1 = (int(nose_2d[0]), int(nose_2d[1]))
                p2 = (int(nose_2d[0] + y * 10), int(nose_2d[1] - x * 10))
                
                cv2.line(frame, p1, p2, (255, 0, 0), 3)
                
                cv2.putText(frame, text, (20, 50), cv2.FONT_HERSHEY_SIMPLEX, 2, (0, 255, 0), 2)
                cv2.putText(frame, "x: " + str(np.round(x, 2)), (500, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)
                cv2.putText(frame, "y: " + str(np.round(y, 2)), (500, 100), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)
                cv2.putText(frame, "z: " + str(np.round(z, 2)), (500, 150), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)

                # Convertir la imagen a base64
                _, img_encoded = cv2.imencode('.jpg', frame)
                image_base64 = base64.b64encode(img_encoded).decode('utf-8')
                print(f"PROCESS HEAD POSE ENDED: {second_count}")
                return Event(event_type, second_count, image_base64)

        except Exception as e:
            print("Error:", e)
